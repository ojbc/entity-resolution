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

/**
 * A class representing statistics about a match for a specific attribute.
 *
 */
public class AttributeStatistics {
	
	private String attributeName;
	private double averageStringDistance;
	private double standardDeviationStringDistance;
	
	public AttributeStatistics(String attributeName)
	{
		this.attributeName = attributeName;
	}

	public double getAverageStringDistance() {
		return averageStringDistance;
	}

	public void setAverageStringDistance(double averageStringDistance) {
		this.averageStringDistance = averageStringDistance;
	}

	public double getStandardDeviationStringDistance() {
		return standardDeviationStringDistance;
	}

	public void setStandardDeviationStringDistance(
			double standardDeviationStringDistance) {
		this.standardDeviationStringDistance = standardDeviationStringDistance;
	}

	public String getAttributeName() {
		return attributeName;
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
				+ ", averageStringDistance=" + averageStringDistance + ", standardDeviationStringDistance="
				+ standardDeviationStringDistance + "]";
	}

}
