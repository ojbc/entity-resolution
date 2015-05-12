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

import serf.data.Attribute;
import serf.data.Record;

/**
 * An extension of the SERF record structure that attaches an identifier to each record.  The identifier will generally be used
 * to link the record object to its representation in the original system (e.g., an XML file or database).  This allows the user of
 * the service to link the merged entities back to their origins.
 *
 */
public class ExternallyIdentifiableRecord extends Record {
	
	private String externalId;
	private Set<String> relatedIds;
	
	/**
	 * Create a record object from the specified set of attributes, with the specified external record ID
	 * @param attributes
	 * @param externalId
	 */
	public ExternallyIdentifiableRecord(Map<String, Attribute> attributes, String externalId)
	{
		super(1.0, attributes);
		this.externalId = externalId;
		relatedIds = new HashSet<String>();
	}
	
	/**
	 * Related the specified record to this one
	 * @param relatedRecord
	 */
	public void relateRecord(ExternallyIdentifiableRecord relatedRecord)
	{
		relatedIds.addAll(relatedRecord.relatedIds);
		relatedIds.add(relatedRecord.externalId);
		relatedIds.remove(externalId); // we never want our own id to be in the related list
	}

	public String getExternalId() {
		return externalId;
	}

	/**
	 * Get the external IDs of all the records related to this one
	 * @return the IDs
	 */
	public Set<String> getRelatedIds() {
		return Collections.unmodifiableSet(relatedIds);
	}

	
	void setRelatedIds(Set<String> relatedIds)
	{
		this.relatedIds = new HashSet<String>();
		this.relatedIds.addAll(relatedIds);
	}
	
	public String toString()
	{
		StringBuffer ret = new StringBuffer(64);
		ret.append("[").append(getClass().getName()).append(":").append(hashCode());
		ret.append(";; externalId=").append(externalId).append(";; relatedIds=").append(relatedIds);
		ret.append(";; attributes=[").append(super.getAttributes()).append("]]");
		return ret.toString();
	}
	
	public int hashCode()
	{
		return 17*super.hashCode() + (externalId == null ? 0 : externalId.hashCode());
	}
	
	public boolean equals(Object o)
	{
		return o != null && o instanceof ExternallyIdentifiableRecord && o.hashCode() == hashCode();
	}

}
