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
import gov.nij.er.Algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A Java Swing table model that supports displaying and editing entity resolution configuration parameters in a JTable.
 * 
 */
public class ERParametersTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final Log LOG = LogFactory.getLog(ERParametersTableModel.class);

    // note: if you change the position of the "use as node label" column, you need to change the getter method for the index
    private String[] columnNames = {
        "Attribute", "Algorithm", "Threshold", "Determinative?", "Use as node label",
    };

    private Class<?>[] columnClasses = {
        String.class, Algorithm.class, Double.class, Boolean.class, Boolean.class,
    };

    private List<Object[]> data;

    private List<RecordTreeModel> dependentTreeModels;
    
    /**
     * Create an instance of the model from a set of RecordTreeModels
     * @param models the models with which to construct this instance
     */
    public ERParametersTableModel(RecordTreeModel... models) {
        clear();
        dependentTreeModels = Arrays.asList(models);
    }

    /**
     * Remove the data from the model
     */
    public void clear() {
        data = new ArrayList<Object[]>();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        return data.get(row)[col];
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Class<?> getColumnClass(int c) {
        return columnClasses[c];
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return col > 0;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        Object[] currentValue = data.get(row);
        currentValue[col] = value;
        this.fireTableCellUpdated(row, col);
        if (col == getUseAsNodeLabelColumnIndex()) {
            handleUseAsNodeLabelChange(value, row);
        }
    }

    private void handleUseAsNodeLabelChange(Object value, int row) {
        // first make sure only one row is checked in this column (but allow for none to be selected, as well)
        if (Boolean.TRUE.equals(value)) {
            int rowCount = getRowCount();
            for (int r = 0; r < rowCount; r++) {
                if (r != row) {
                    setValueAt(Boolean.FALSE, r, getUseAsNodeLabelColumnIndex());
                }
            }
        }
        // now set the value on the tree model
        for (RecordTreeModel model : dependentTreeModels) {
            model.setNodeLabelAttributeName(getNodeLabelParameterName());
        }
    }

    private int getUseAsNodeLabelColumnIndex() {
        return 4;
    }

    private String getNodeLabelParameterName() {
        int rowCount = getRowCount();
        for (int r = 0; r < rowCount; r++) {
            Boolean bv = (Boolean) getValueAt(r, 4);
            boolean checked = bv != null && bv.booleanValue();
            if (checked) {
                return (String) getValueAt(r, 0);
            }
        }
        return null;
    }

    /**
     * Add a supported parameter with a default algorithm (the first in the Algorithm interface), with a threshold of .8, and non-determinative
     * @param parameter the parameter
     */
    public void addParameter(String parameter) {
        addParameter(parameter, Algorithm.SUPPORTED_ALGORITHMS[0], 0.8, false);
    }

    /**
     * Add a supported parameter with the characteristics of the provided params object
     * @param params the parameter
     */
    public void addParameter(AttributeParameters params) {
        addParameter(params.getAttributeName(), Algorithm.forClassName(params.getAlgorithmClassName()), params.getThreshold(), params.isDeterminative());
    }

    /**
     * Add a supported parameter
     * @param attributeName the name of the attribute
     * @param algorithm the algorithm to use for the attribute
     * @param threshold the threshold for entity resolution
     * @param isDeterminative whether the attribute is determinative or not
     */
    public void addParameter(String attributeName, Algorithm algorithm, double threshold, boolean isDeterminative) {
        Object[] rowData = new Object[getColumnCount()];
        data.add(rowData);
        int newRowIndex = getRowCount() - 1;
        setValueAt(attributeName, newRowIndex, 0);
        setValueAt(algorithm, newRowIndex, 1);
        setValueAt(new Double(threshold), newRowIndex, 2);
        setValueAt(isDeterminative, newRowIndex, 3);
    }

    /**
     * Get the attributes configured for the model
     * @return the attributes
     */
    public Set<AttributeParameters> getAttributeParameters() {
        Set<AttributeParameters> ret = new HashSet<AttributeParameters>();
        for (int i = 0; i < getRowCount(); i++) {
            AttributeParameters ap = new AttributeParameters((String) getValueAt(i, 0));
            ap.setAlgorithmClassName(((Algorithm) getValueAt(i, 1)).getClassName());
            ap.setThreshold(((Double) getValueAt(i, 2)).doubleValue());
            ap.setDeterminative(((Boolean) getValueAt(i, 3)).booleanValue());
            ret.add(ap);
        }
        return ret;
    }

    /**
     * Load the attribute parameters for the model
     * @param parameters the parameters
     */
    public void loadParameters(Set<AttributeParameters> parameters) {
        clear();
        for (AttributeParameters params : parameters) {
            addParameter(params);
        }
    }

    /**
     * Determine if the model is ready to compute resolution
     * @return true of the model is ready
     */
    public boolean readyToResolve() {
        boolean ret = true;
        for (int i = 0; i < getRowCount() && ret; i++) {
            if (getValueAt(i, 1) == null || getValueAt(i, 2) == null) {
                ret = false;
            }
        }
        return ret;
    }

    /**
     * Add default parameters with the specified list of names
     * @param parameterNames the names
     */
    public void addParameters(List<String> parameterNames) {
        for (String p : parameterNames) {
            addParameter(p);
        }
        super.fireTableDataChanged();
    }

}
