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
package gov.nij.bundles.intermediaries.ers.osgi;

import java.io.Serializable;

/**
 * A class representing the configuration of entity resolution for a specified record attribute.  For example, in a typical entity resolution scenario,
 * a record may contain first name, last name, and SSN.  Each of these would be an attribute of the record, and this class represents how each of those attributes
 * will be compared when performing resolution.
 *
 */
public class AttributeParameters implements Serializable {

	private static final long serialVersionUID = -2697143897879392761L;
	private String attributeName;
	private String algorithmClassName;
	private double threshold;
	private boolean determinative;
	private SortOrderSpecification sortOrder;

	public SortOrderSpecification getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrderSpecification sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * Create an instance for an attribute with the specified name.
     * @param attributeName
     */
    public AttributeParameters(String attributeName) {
		this.attributeName = attributeName;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public String getAlgorithmClassName() {
		return algorithmClassName;
	}

	public void setAlgorithmClassName(String algorithmClassName) {
		this.algorithmClassName = algorithmClassName;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public boolean isDeterminative() {
		return determinative;
	}

	public void setDeterminative(boolean determinative) {
		this.determinative = determinative;
	}

	@Override
	public int hashCode() {
		return attributeName.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o != null && o instanceof AttributeParameters
				&& o.hashCode() == hashCode();
	}

	@Override
	public String toString() {
		return super.toString() + ": [attributeName=" + attributeName
				+ ", algorithmClassName=" + algorithmClassName + ", threshold="
				+ threshold + ", determinative=" + determinative + "]";
	}

}
