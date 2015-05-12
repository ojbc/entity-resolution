/*
 * Copyright 2013 SEARCH Group, Incorporated. 
 * 
 * See the NOTICE file distributed with  this work for additional information 
 * regarding copyright ownership.  SEARCH Group Inc. licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not use this 
 * file except in compliance with the License.  You may obtain a copy of the 
 * License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.nij.er.ui;

import gov.nij.bundles.intermediaries.ers.osgi.AttributeParameters;
import gov.nij.bundles.intermediaries.ers.osgi.EntityResolutionConversionUtils;
import gov.nij.bundles.intermediaries.ers.osgi.EntityResolutionResults;
import gov.nij.bundles.intermediaries.ers.osgi.EntityResolutionService;
import gov.nij.bundles.intermediaries.ers.osgi.ExternallyIdentifiableRecord;
import gov.nij.bundles.intermediaries.ers.osgi.RecordWrapper;
import gov.nij.er.Algorithm;

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import serf.data.Attribute;

/**
 * User interface for exploring/testing entity resolution configuration and behavior. Set a system property "mode=test" to load a small number of test records (borrowed from one of the Entity
 * Resolution Service unit tests). Without this property, it loads no data at startup and you need to load the data from an Excel spreadsheet (via menu item). Assumes the data are in the first sheet
 * in the Excel workbook, and that the first row in that sheet contains parameter names.
 * 
 * You can persist parameters (via object serialization) between sessions. THe application checks to make sure that any parameters you load are consistent with the data you've loaded.
 * 
 */
public final class EntityResolutionDemo extends JFrame {

    private static final String SINGLE_DOT = ".";

    private static final Log LOG = LogFactory.getLog(EntityResolutionDemo.class);

    private static final String PARAMETER_FILE_EXTENSION = "param";

    private static final long serialVersionUID = 1L;
    private RecordTreeModel rawDataTreeModel;
    private RecordTreeModel resolvedDataTreeModel;
    private RawDataFilteredTreeModel rawDataFilteredTreeModel;
    private ERParametersTableModel parametersTableModel;
    private JButton resolveButton;
    private JLabel rawRecordCountLabel;
    private JLabel resolvedRecordCountLabel;
    private JCheckBox filterForSelectedCheckBox;
    private TreeSelectionModel resolvedDataTreeSelectionModel;

    private JTree rawDataTree;

    private JTree resolvedDataTree;

    private JTable parametersTable;

    private JComboBox algorithmComboBox;

    private EntityResolutionDemo(boolean test) {

        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent arg0) {
                exit();
            }

        });

        setTitle("Entity Resolution Demo");

        setJMenuBar(buildMenuBar());

        createUIWidgets();

        layoutUI();
        setupWidgetModels();
        setupWidgetListeners();

        parametersTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(algorithmComboBox));

        if (test) {
            loadTestRecords();
        }

        updateUIForParameterChange();

        setSize(800, 600);
        setVisible(true);

    }

    private void layoutUI() {

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        JScrollPane rawDataTreeScrollPane = new JScrollPane(rawDataTree);
        JScrollPane parametersTableScrollPane = new JScrollPane(parametersTable);
        JScrollPane resolvedDataTreeScrollPane = new JScrollPane(resolvedDataTree);

        Insets insets = new Insets(5, 5, 5, 5);
        panel.add(new JLabel("Raw Data:"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
        panel.add(new JLabel("Resolved Data:"), new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
        panel.add(rawDataTreeScrollPane, new GridBagConstraints(0, 1, 1, 1, 0.5, 0.5, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0));
        panel.add(resolvedDataTreeScrollPane, new GridBagConstraints(1, 1, 1, 1, 0.5, 0.5, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0));
        panel.add(rawRecordCountLabel, new GridBagConstraints(0, 2, 1, 1, 0.5, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0));
        JPanel resolvedLabelPanel = new JPanel();
        resolvedLabelPanel.setLayout(new GridBagLayout());
        resolvedLabelPanel.add(resolvedRecordCountLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.EAST, GridBagConstraints.BOTH, insets, 0, 0));
        resolvedLabelPanel.add(filterForSelectedCheckBox, new GridBagConstraints(1, 0, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
        panel.add(resolvedLabelPanel, new GridBagConstraints(1, 2, 1, 1, 0.5, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0));
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridBagLayout());
        bottomPanel.add(new JLabel("Parameters:"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
        bottomPanel.add(parametersTableScrollPane, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0));
        bottomPanel.add(resolveButton, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
        panel.add(bottomPanel, new GridBagConstraints(0, 3, 2, 1, 1.0, 0.5, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0));
        this.setContentPane(panel);

    }

    private void setupWidgetListeners() {
        resolveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                runResolution();
            }
        });

        parametersTableModel.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent arg0) {
                updateUIForParameterChange();
            }
        });

        new RecordCountLabelUpdateTreeModelListener(rawDataFilteredTreeModel, rawRecordCountLabel);
        new RecordCountLabelUpdateTreeModelListener(resolvedDataTreeModel, resolvedRecordCountLabel);

        filterForSelectedCheckBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                rawDataFilteredTreeModel.setFiltered(filterForSelectedCheckBox.isSelected());
            }
        });

        resolvedDataTree.addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent e) {
                TreePath selectedPath = e.getPath();
                Set<String> recordIds = resolvedDataTreeModel.getRecordIdsForPath(selectedPath);
                rawDataFilteredTreeModel.setFilteredRecordIds(recordIds);
            }
        });

    }

    private void setupWidgetModels() {

        rawDataTreeModel = new RecordTreeModel();
        resolvedDataTreeModel = new RecordTreeModel();
        resolvedDataTreeSelectionModel = rawDataTree.getSelectionModel();
        rawDataFilteredTreeModel = new RawDataFilteredTreeModel(rawDataTreeModel);
        parametersTableModel = new ERParametersTableModel(rawDataTreeModel, resolvedDataTreeModel);

        // rawDataTree.setModel(rawDataTreeModel);
        resolvedDataTree.setModel(resolvedDataTreeModel);
        rawDataTree.setModel(rawDataFilteredTreeModel);

        resolvedDataTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        resolvedDataTreeSelectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        parametersTable.setModel(parametersTableModel);

        for (Algorithm alg : Algorithm.SUPPORTED_ALGORITHMS) {
            algorithmComboBox.addItem(alg);
        }
    }

    private void createUIWidgets() {
        resolvedDataTree = new JTree();
        rawDataTree = new JTree();
        parametersTable = new JTable();
        resolveButton = new JButton("Resolve");
        rawRecordCountLabel = new JLabel();
        resolvedRecordCountLabel = new JLabel();
        filterForSelectedCheckBox = new JCheckBox("Filter Raw Data For Selected");
        algorithmComboBox = new JComboBox();
    }

    private void runResolution() {
        new UIBlockingTask(new RunnableTask() {
            public void run() throws Exception {
                EntityResolutionService ers = new EntityResolutionService();
                EntityResolutionResults results = ers.resolveEntities(rawDataTreeModel.getRecords(), parametersTableModel.getAttributeParameters());
                resolvedDataTreeModel.init(results.getRecords());
            }
        }).execute();
        // EntityResolutionResults results =
        // ers.resolveEntities(rawDataTreeModel.getRecords(),
        // parametersTableModel.getAttributeParameters());
        // resolvedDataTreeModel.init(results.getRecords());
        // hideWaitUI();
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menu.add(new ExcelLoadAction());
        menu.add(new SaveParametersAction());
        menu.add(new LoadParametersAction());
        menu.add(new QuitAction());
        menuBar.add(menu);
        return menuBar;
    }

    private void saveParameters() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new SerializedParameterFileFilter());
        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                saveParameters(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveParameters(File file) {
        if (file.exists()) {
            int option = promptParametersFileOverwrite();
            if (option == JOptionPane.NO_OPTION || option == JOptionPane.CLOSED_OPTION || option == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }
        File enhancedFile = file;
        if (!file.getName().contains(SINGLE_DOT)) {
            enhancedFile = new File(file.getAbsolutePath() + SINGLE_DOT + PARAMETER_FILE_EXTENSION);
        }
        ObjectOutput output = null;
        try {
            output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(enhancedFile)));
            output.writeObject(parametersTableModel.getAttributeParameters());
            LOG.debug("Wrote parameters to file " + enhancedFile.getAbsolutePath());
        } catch (IOException ex) {
            error("Error writing parameters to a file.");
            ex.printStackTrace();
        } finally {
            try {
                output.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private int promptParametersFileOverwrite() {
        return JOptionPane.showConfirmDialog(this, "The file exists, overwrite?", "Existing file", JOptionPane.YES_NO_CANCEL_OPTION);
    }

    private void loadParameters() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new SerializedParameterFileFilter());
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                loadParameters(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadParameters(File file) {
        ObjectInput input = null;
        try {
            input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
            @SuppressWarnings("unchecked")
            Set<AttributeParameters> parameters = (Set<AttributeParameters>) input.readObject();
            if (!rawDataTreeModel.checkParametersConsistent(parameters)) {
                error("Loaded parameters are not consistent with current loaded dataset");
                return;
            }
            parametersTableModel.loadParameters(parameters);
            LOG.debug("Read parameters from file " + file.getAbsolutePath());
        } catch (Exception ex) {
            error("Error reading parameters from file.");
            ex.printStackTrace();
        } finally {
            try {
                input.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleLoadExcelDataCommand() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                String name = f.getName();
                return f.isDirectory() || name.endsWith(".xls") || name.endsWith(".xlsx");
            }

            @Override
            public String getDescription() {
                return "Excel Workbooks (*.xls, *.xlsx)";
            }
        });
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            final File file = fc.getSelectedFile();
            try {
                new UIBlockingTask(new RunnableTask() {
                    public void run() throws Exception {
                        loadExcelData(file);
                    }
                }).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadTestRecords() {

        List<ExternallyIdentifiableRecord> records1 = new ArrayList<ExternallyIdentifiableRecord>();

        String givenNameAttributeName = "givenName";
        String baseSIDValue = "123";
        String sidAttributeName = "sid";

        Attribute a1 = new Attribute(givenNameAttributeName, "Andrew");
        Attribute a2 = new Attribute(sidAttributeName, baseSIDValue);
        ExternallyIdentifiableRecord r1 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2), "record1");

        a2 = new Attribute(sidAttributeName, baseSIDValue);
        ExternallyIdentifiableRecord r2 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2), "record2");

        a2 = new Attribute(sidAttributeName, "124");
        ExternallyIdentifiableRecord r3 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2), "record3");

        a1 = new Attribute(givenNameAttributeName, "Yogesh");
        a2 = new Attribute(sidAttributeName, "789");

        ExternallyIdentifiableRecord r4 = new ExternallyIdentifiableRecord(makeAttributes(a1, a2), "record4");

        records1.add(r1);
        records1.add(r2);
        records1.add(r3);
        records1.add(r4);
        List<RecordWrapper> records = EntityResolutionConversionUtils.convertRecords(records1);
        rawDataTreeModel.init(records);

        parametersTableModel.addParameter(givenNameAttributeName);
        parametersTableModel.addParameter(sidAttributeName);

    }

    private void loadExcelData(File file) throws Exception {

        LOG.debug("Loading Excel data file " + file.getAbsolutePath());

        InputStream inp = new FileInputStream(file);
        Workbook wb = WorkbookFactory.create(inp);

        // note that we read all the data out of the spreadsheet first, then
        // update the models. this way if there is
        // an error, we don't wipe out what the user already has.

        Sheet sheet = wb.getSheetAt(0);
        Row parametersRow = sheet.getRow(0);
        List<String> parameterNames = new ArrayList<String>();
        for (Cell cell : parametersRow) {
            String v = cell.getStringCellValue();
            if (parameterNames.contains(v)) {
                error("Duplicate field: " + v);
                return;
            }
            parameterNames.add(v);
            LOG.debug("Adding parameter " + v);
        }

        int parameterCount = parameterNames.size();

        LOG.debug("Excel loading read " + parameterCount + " parameters");

        List<ExternallyIdentifiableRecord> records = new ArrayList<ExternallyIdentifiableRecord>();

        int rowCount = sheet.getLastRowNum();
        LOG.debug("Loading " + (rowCount - 1) + " rows from " + sheet.getSheetName());

        int digits = (int) (Math.floor(Math.log10(rowCount)) + 1);

        DataFormatter dataFormatter = new DataFormatter();

        for (int rowIndex = 1; rowIndex <= rowCount; rowIndex++) {
            List<Attribute> attributes = new ArrayList<Attribute>(parameterCount);
            Row row = sheet.getRow(rowIndex);
            for (int i = 0; i < parameterCount; i++) {
                Cell cell = row.getCell(i);
                String v = dataFormatter.formatCellValue(cell);
                String parameterName = parameterNames.get(attributes.size());
                attributes.add(new Attribute(parameterName, v));
                // LOG.debug("Adding attribute, name=" + parameterName + ", v="
                // + (v==null ? "null" : "'" + v + "'"));
            }
            records.add(new ExternallyIdentifiableRecord(makeAttributes(attributes.toArray(new Attribute[] {})), String.format("%0" + digits + "d", rowIndex)));
        }

        LOG.debug("Read " + records.size() + " records from Excel");

        List<RecordWrapper> recordWrappers = EntityResolutionConversionUtils.convertRecords(records);
        rawDataTreeModel.init(recordWrappers);

        parametersTableModel.clear();
        parametersTableModel.addParameters(parameterNames);

    }

    private static Map<String, Attribute> makeAttributes(Attribute... attributes) {
        Map<String, Attribute> ret = new HashMap<String, Attribute>();
        for (Attribute a : attributes) {
            ret.put(a.getType(), a);
        }
        return ret;
    }

    private void error(String string) {
        LOG.error(string);
        JOptionPane.showMessageDialog(this, string, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void updateUIForParameterChange() {
        resolveButton.setEnabled(parametersTableModel.readyToResolve());
    }

    private static void exit() {
        System.exit(0);
    }

    /**
     * Main method to execute this class
     * @param args arguments passed in from command line
     */
    public static void main(String[] args) {
        String mode = System.getProperty("mode");
        new EntityResolutionDemo(mode != null && "test".equals(mode));
    }

    private final class UIBlockingTask extends SwingWorker<Void, Void> {
        private final RunnableTask r;

        private UIBlockingTask(RunnableTask r) {
            this.r = r;
        }

        @Override
        protected Void doInBackground() throws Exception {
            MouseListener glassPaneBlockingMouseListener = showWaitUI();
            r.run();
            hideWaitUI(glassPaneBlockingMouseListener);
            return null;
        }

        private void hideWaitUI(MouseListener glassPaneBlockingMouseListener) {
            getGlassPane().setVisible(false);
            getGlassPane().removeMouseListener(glassPaneBlockingMouseListener);
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

        private MouseListener showWaitUI() {
            getGlassPane().setVisible(true);
            MouseListener ml = new MouseListener() {
                // this mouse listener blocks all mouse inputs while the hourglass
                // is up...
                public void mouseClicked(MouseEvent arg0) {
                }

                public void mouseEntered(MouseEvent arg0) {
                }

                public void mouseExited(MouseEvent arg0) {
                }

                public void mousePressed(MouseEvent arg0) {
                }

                public void mouseReleased(MouseEvent arg0) {
                }
            };
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            getGlassPane().addMouseListener(ml);
            return ml;
        }
    }

    private final class SerializedParameterFileFilter extends FileFilter {
        @Override
        public boolean accept(File f) {
            String name = f.getName();
            return f.isDirectory() || name.endsWith(SINGLE_DOT + PARAMETER_FILE_EXTENSION);
        }

        @Override
        public String getDescription() {
            return "Saved Parameters (*." + PARAMETER_FILE_EXTENSION + ")";
        }
    }

    private final class RecordCountLabelUpdateTreeModelListener implements TreeModelListener {
        private RecordCountableTreeModel model;
        private JLabel label;

        public RecordCountLabelUpdateTreeModelListener(RecordCountableTreeModel model, JLabel label) {
            this.model = model;
            this.label = label;
            model.addTreeModelListener(this);
        }

        public void treeNodesChanged(TreeModelEvent e) {
            handleChange(e);
        }

        public void treeNodesInserted(TreeModelEvent e) {
            handleChange(e);
        }

        public void treeNodesRemoved(TreeModelEvent e) {
            handleChange(e);
        }

        public void treeStructureChanged(TreeModelEvent e) {
            handleChange(e);
        }

        private void handleChange(TreeModelEvent e) {
            int recordCount = model.getRecordCount();
            label.setText(recordCount + " record" + (recordCount == 1 ? "" : "s"));
        }
    }

    interface RunnableTask {
        void run() throws Exception;
    }

    private final class ExcelLoadAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public ExcelLoadAction() {
            super("Load Excel Data File...");
            KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.META_DOWN_MASK);
            super.putValue(Action.ACCELERATOR_KEY, ks);
        }

        public void actionPerformed(ActionEvent e) {
            handleLoadExcelDataCommand();
        }
    }

    private final class QuitAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public QuitAction() {
            super("Quit");
            KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.META_DOWN_MASK);
            super.putValue(Action.ACCELERATOR_KEY, ks);
        }

        public void actionPerformed(ActionEvent e) {
            exit();
        }
    }

    private final class SaveParametersAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public SaveParametersAction() {
            super("Save Parameters...");
            KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.META_DOWN_MASK);
            super.putValue(Action.ACCELERATOR_KEY, ks);
        }

        public void actionPerformed(ActionEvent e) {
            saveParameters();
        }
    }

    private final class LoadParametersAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        public LoadParametersAction() {
            super("Load Parameters...");
            KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.META_DOWN_MASK);
            super.putValue(Action.ACCELERATOR_KEY, ks);
        }

        public void actionPerformed(ActionEvent e) {
            loadParameters();
        }

    }

}
