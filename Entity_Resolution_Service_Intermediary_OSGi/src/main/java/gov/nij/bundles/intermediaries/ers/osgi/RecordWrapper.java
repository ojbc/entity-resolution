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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A class that provides a SERF-independent representation of a SERF record (specifically, an externally identifiable one).
 *
 */
public class RecordWrapper
{
	
	private String externalId;
	private Set<String> relatedIds;
	private Map<String, AttributeWrapper> attributes;
	
	/**
	 * Create a SERF-independent equivalent to the record with the specified set of attributes and the specified external ID
	 * @param attributes
	 * @param externalId
	 */
	public RecordWrapper(Map<String, AttributeWrapper> attributes, String externalId)
	{
		this.attributes = attributes;
		this.externalId = externalId;
		relatedIds = new HashSet<String>();
	}
	
	/**
	 * Get the external ID of the wrapped record
	 * @return
	 */
	public String getExternalId() {
		return externalId;
	}

	/**
	 * Get the IDs of the records related to this one
	 * @return
	 */
	public Set<String> getRelatedIds() {
		return Collections.unmodifiableSet(relatedIds);
	}

	/**
	 * Get the record's attributes
	 * @return
	 */
	public Map<String, AttributeWrapper> getAttributes()
	{
		return attributes;
	}

	void setRelatedIds(Set<String> relatedIds)
	{
		this.relatedIds = new HashSet<String>();
		this.relatedIds.addAll(relatedIds);
	}
	
	public int hashCode()
	{
		return 17*(
				   17*(
				       17*super.hashCode() + (externalId == null ? 0 : externalId.hashCode())
				      ) + 
				 	   (relatedIds == null ? 0 : relatedIds.hashCode())
				  ) + (attributes == null ? 0 : attributes.hashCode());
	}
	
	public boolean equals(Object o)
	{
		return o != null && o instanceof RecordWrapper && o.hashCode() == hashCode();
	}
	
	public String toString()
	{
		return super.hashCode() + " [externalId=" + externalId + ", relatedIds=" + relatedIds + ", attributes=" + attributes + "]";
	}

}
