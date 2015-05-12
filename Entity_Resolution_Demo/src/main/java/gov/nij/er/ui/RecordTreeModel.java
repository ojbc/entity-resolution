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
import gov.nij.bundles.intermediaries.ers.osgi.AttributeWrapper;
import gov.nij.bundles.intermediaries.ers.osgi.RecordWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A Java Swing tree model that supports displaying entity resolution record sets in a JTree.
 * 
 */
public class RecordTreeModel implements RecordCountableTreeModel {

    static final String ROOT_NODE_LABEL = "Records";

    private static final Log LOG = LogFactory.getLog(RecordTreeModel.class);

    private List<TreeModelListener> listeners = new ArrayList<TreeModelListener>();
    private DefaultMutableTreeNode root;
    private List<RecordWrapper> records;
    private String displayAttributeName;

    private final Comparator<RecordWrapper> recordSorter = new Comparator<RecordWrapper>() {

        public int compare(RecordWrapper o1, RecordWrapper o2) {
            if (displayAttributeName == null) {
                // LOG.debug("Sorting by external ID");
                return o1.getExternalId().compareTo(o2.getExternalId());
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            } else {
                Map<String, AttributeWrapper> a1 = o1.getAttributes();
                AttributeWrapper aw1 = a1.get(displayAttributeName);
                String sv1 = getAttributeWrapperStringValue(aw1);
                Map<String, AttributeWrapper> a2 = o2.getAttributes();
                AttributeWrapper aw2 = a2.get(displayAttributeName);
                String sv2 = getAttributeWrapperStringValue(aw2);
                int comparison = sv1.compareTo(sv2);
                // LOG.debug("Comparing " + sv1 + " to " + sv2 + "=" +
                // comparison);
                return comparison;
            }
        }

    };

    /**
     * Initialize the model with a set of records
     * 
     * @param recordsParam
     *            the records to display
     */
    public void init(List<RecordWrapper> recordsParam) {
        this.records = new ArrayList<RecordWrapper>(recordsParam);
        updateModel();
    }

    /**
     * Update the model with data and notify listeners
     */
    public void updateModel() {
        root = new DefaultMutableTreeNode(ROOT_NODE_LABEL);
        if (records != null) {
            Collections.sort(records, recordSorter);
            for (RecordWrapper record : records) {

                DefaultMutableTreeNode node = new RecordTreeNode(record);
                root.add(node);
            }
            LOG.debug("Initializing tree model with new records");
            for (TreeModelListener l : listeners) {
                l.treeStructureChanged(new TreeModelEvent(this, new Object[] {
                    root,
                }));
            }
        }
    }

    private Object getNodeLabelForRecord(RecordWrapper record) {
        String ret = record.getExternalId();
        if (displayAttributeName != null) {
            Map<String, AttributeWrapper> attributes = record.getAttributes();
            AttributeWrapper aw = attributes.get(displayAttributeName);
            ret = getAttributeWrapperStringValue(aw);
        }
        return ret;
    }

    private String getAttributeWrapperStringValue(AttributeWrapper aw) {
        String ret;
        String nullValueLabel = "<null>";
        if (aw == null) {
            ret = nullValueLabel;
        } else {
            StringBuffer sb = new StringBuffer(128);
            String attributeValueSeparator = "/";
            for (String value : aw.getValues()) {
                if (value == null) {
                    sb.append(nullValueLabel);
                } else {
                    sb.append(value);
                }
                sb.append(attributeValueSeparator);
            }
            ret = sb.toString();
            if (ret.contains(attributeValueSeparator)) {
                ret = sb.toString().substring(0, sb.length() - 1);
            }
        }
        return ret;
    }

    @Override
    public void addTreeModelListener(TreeModelListener arg0) {
        listeners.add(arg0);
    }

    @Override
    public Object getChild(Object parent, int index) {
        return ((DefaultMutableTreeNode) parent).getChildAt(index);
    }

    @Override
    public int getChildCount(Object node) {
        return ((DefaultMutableTreeNode) node).getChildCount();
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return ((DefaultMutableTreeNode) parent).getIndex((TreeNode) child);
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public boolean isLeaf(Object node) {
        return ((DefaultMutableTreeNode) node).isLeaf();
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
     * Get the records currently in the model
     * @return the records
     */
    public List<RecordWrapper> getRecords() {
        List<RecordWrapper> ret = new ArrayList<RecordWrapper>();
        ret.addAll(records);
        return ret;
    }

    /**
     * Set the attribute for the node label
     * @param name the name of the attribute to be used to label nodes
     */
    public void setNodeLabelAttributeName(String name) {
        displayAttributeName = name;
        updateModel();
    }

    /**
     * Get the number of records in the model
     * @return the record count
     */
    public int getRecordCount() {
        return records.size();
    }

    /**
     * Determine if the specified set of parameters match the records in the model
     * @param parameters the parameters to check
     * @return whether those parameters are consistent with the records loaded in the model (i.e., whether the names match)
     */
    public boolean checkParametersConsistent(Set<AttributeParameters> parameters) {
        if (records != null) {
            RecordWrapper rw = records.get(0);
            Set<String> attributeNames = rw.getAttributes().keySet();
            for (AttributeParameters params : parameters) {
                if (!attributeNames.contains(params.getAttributeName())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Get the nodes in the model for the specified set of record IDs
     * @param recordIds the ids to search for
     * @return the matching set of nodes in the tree
     */
    public List<DefaultMutableTreeNode> getNodesForRecordIds(Set<String> recordIds) {
        List<DefaultMutableTreeNode> ret = new ArrayList<DefaultMutableTreeNode>();
        LOG.debug("root.children()=" + root.children());
        @SuppressWarnings("unchecked")
        Enumeration<DefaultMutableTreeNode> recordNodeEnumeration = root.children();
        while (recordNodeEnumeration.hasMoreElements()) {
            RecordTreeNode child = (RecordTreeNode) recordNodeEnumeration.nextElement();
            LOG.debug("Checking if child id=" + child.getId() + " is in set=" + recordIds);
            if (recordIds.contains(child.getId())) {
                ret.add(child);
            }
        }
        LOG.debug("Returning ret=" + ret);
        return ret;
    }

    /**
     * Get the IDs of the records represented by the specified path
     * @param selectedPath the path to search
     * @return the ids of the records at that path
     */
    public Set<String> getRecordIdsForPath(TreePath selectedPath) {
        Set<String> ret = new HashSet<String>();

        Object[] pathArray = selectedPath.getPath();
        if (pathArray.length > 1) {

            DefaultMutableTreeNode recordNode = (DefaultMutableTreeNode) pathArray[1];
            @SuppressWarnings("unchecked")
            Enumeration<TreeNode> descendants = recordNode.breadthFirstEnumeration();

            while (descendants.hasMoreElements()) {
                TreeNode node = descendants.nextElement();
                if (node instanceof IdNode) {
                    ret.add(((IdNode) node).getId());
                }
            }
        }
        LOG.debug("Record ids for path: " + ret);
        return ret;
    }

    private final class RelatedIdNode extends DefaultMutableTreeNode implements IdNode {
        private static final long serialVersionUID = -5058548631194540606L;

        RelatedIdNode(String id) {
            super(id);
        }

        public String getId() {
            return (String) super.getUserObject();
        }
    }

    private final class RecordTreeNode extends DefaultMutableTreeNode implements IdNode {
        private static final long serialVersionUID = 1L;
        private RecordWrapper rw;

        RecordTreeNode(RecordWrapper rw) {
            super(rw);
            this.rw = rw;
            Map<String, AttributeWrapper> attributes = rw.getAttributes();
            DefaultMutableTreeNode attributesContainerNode = new DefaultMutableTreeNode("Attributes");
            add(attributesContainerNode);
            for (String key : attributes.keySet()) {
                attributesContainerNode.add(new DefaultMutableTreeNode(key + ": " + attributes.get(key).getValues()));
            }
            if (rw.getRelatedIds().size() > 0) {
                DefaultMutableTreeNode relativesContainerNode = new DefaultMutableTreeNode("Related Records");
                add(relativesContainerNode);
                for (String rr : rw.getRelatedIds()) {
                    relativesContainerNode.add(new RelatedIdNode(rr));
                }
            }
        }

        public String toString() {
            return getNodeLabelForRecord(rw).toString();
        }

        public String getId() {
            return rw.getExternalId();
        }

        public Object clone() {
            super.clone();
            return new RecordTreeNode(rw);
        }

    }

    private interface IdNode extends TreeNode {
        public String getId();
    }

}
