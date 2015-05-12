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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A TreeModel implementation that filters the raw data based on the records selected in the resolved data tree.
 * 
 */
public class RawDataFilteredTreeModel implements RecordCountableTreeModel {

    private static final String ROOT_NODE_LABEL = RecordTreeModel.ROOT_NODE_LABEL;

    private static final Log LOG = LogFactory.getLog(RawDataFilteredTreeModel.class);

    private RecordTreeModel rawDataTreeModel;
    private DefaultMutableTreeNode root;
    private List<TreeModelListener> listeners = new ArrayList<TreeModelListener>();
    private boolean isFiltered;

    /*
     * Create a new instance to filter the specified tree model
     * @param rawDataTreeModel the model to be filtered
     */
    RawDataFilteredTreeModel(RecordTreeModel rawDataTreeModel) {
        this.rawDataTreeModel = rawDataTreeModel;
        rawDataTreeModel.addTreeModelListener(new TreeModelListener() {

            public void treeNodesChanged(TreeModelEvent e) {
                if (!isFiltered) {
                    for (TreeModelListener l : listeners) {
                        l.treeNodesChanged(e);
                    }
                }
            }

            public void treeNodesInserted(TreeModelEvent e) {
                if (!isFiltered) {
                    for (TreeModelListener l : listeners) {
                        l.treeNodesInserted(e);
                    }
                }
            }

            public void treeNodesRemoved(TreeModelEvent e) {
                if (!isFiltered) {
                    for (TreeModelListener l : listeners) {
                        l.treeNodesRemoved(e);
                    }
                }
            }

            public void treeStructureChanged(TreeModelEvent e) {
                if (!isFiltered) {
                    for (TreeModelListener l : listeners) {
                        l.treeStructureChanged(e);
                    }
                }
            }
        });
        root = new DefaultMutableTreeNode(ROOT_NODE_LABEL);

    }

    @Override
    public void addTreeModelListener(TreeModelListener arg0) {
        listeners.add(arg0);
    }

    @Override
    public Object getChild(Object parent, int index) {
        Object ret = null;
        if (isFiltered) {
            ret = ((DefaultMutableTreeNode) parent).getChildAt(index);
        } else {
            ret = rawDataTreeModel.getChild(parent, index);
        }
        return ret;
    }

    @Override
    public int getChildCount(Object node) {
        int ret = 0;
        if (isFiltered) {
            ret = ((DefaultMutableTreeNode) node).getChildCount();
        } else {
            ret = rawDataTreeModel.getChildCount(node);
        }
        return ret;
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        int ret = 0;
        if (isFiltered) {
            ret = ((DefaultMutableTreeNode) parent).getIndex((TreeNode) child);
        } else {
            ret = rawDataTreeModel.getIndexOfChild(parent, child);
        }
        return ret;
    }

    @Override
    public Object getRoot() {
        Object ret = null;
        if (isFiltered) {
            ret = root;
        } else {
            ret = rawDataTreeModel.getRoot();
        }
        return ret;
    }

    @Override
    public boolean isLeaf(Object node) {
        boolean ret = false;
        if (isFiltered) {
            ret = ((DefaultMutableTreeNode) node).isLeaf();
        } else {
            ret = rawDataTreeModel.isLeaf(node);
        }
        return ret;
    }

    @Override
    public void removeTreeModelListener(TreeModelListener arg0) {
        listeners.remove(arg0);
    }

    @Override
    public void valueForPathChanged(TreePath arg0, Object arg1) {
        // TODO Auto-generated method stub

    }

    /**
     * Determine whether the model is currently filtering, or just passing through
     * @param isFilteredParam true if filtering
     */
    public void setFiltered(boolean isFilteredParam) {
        this.isFiltered = isFilteredParam;
        updateModel();
    }

    /**
     * Refresh the model to recognize new data, and notify any listeners
     */
    public void updateModel() {
        LOG.debug("Updating model, isFiltered=" + isFiltered);
        for (TreeModelListener l : listeners) {
            l.treeStructureChanged(new TreeModelEvent(this, new Object[] {
                getRoot(),
            }));
        }
    }

    /**
     * Determine which recordIds are filtered (displayed)
     * @param recordIds the list of ids for the records to display
     */
    public void setFilteredRecordIds(Set<String> recordIds) {
        root = new DefaultMutableTreeNode(ROOT_NODE_LABEL);
        List<DefaultMutableTreeNode> nodes = rawDataTreeModel.getNodesForRecordIds(recordIds);
        for (DefaultMutableTreeNode node : nodes) {
            LOG.debug("Adding node " + node);
            root.add((MutableTreeNode) node.clone());

        }
        updateModel();
    }

    /**
     * Get the number of filtered records
     * @return the record count
     */
    public int getRecordCount() {
        return isFiltered ? root.getChildCount() : rawDataTreeModel.getRecordCount();
    }

}
