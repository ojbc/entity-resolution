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
package gov.nij.bundles.intermediaries.ers;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

/**
 * 
 * This class implements a NamespaceContext that assist with internal Xpath queries.  These namespaces only address
 * the Entity Resolution SSP however this class is not meant to deal with namespaces for the
 * actual payloads inside of the entity container. 
 * 
 */

final class EntityResolutionNamespaceContext implements NamespaceContext
{
	static final String MERGE_NAMESPACE = "http://nij.gov/IEPD/Exchange/EntityMergeRequestMessage/1.0";
	static final String MERGE_RESULT_NAMESPACE = "http://nij.gov/IEPD/Exchange/EntityMergeResultMessage/1.0";
	static final String ER_EXT_NAMESPACE = "http://nij.gov/IEPD/Extensions/EntityResolutionExtensions/1.0";
	static final String MERGE_RESULT_EXT_NAMESPACE = "http://nij.gov/IEPD/Extensions/EntityMergeResultMessageExtensions/1.0";
    static final String STRUCTURES_NAMESPACE = "http://niem.gov/niem/structures/2.0";
    static final String NC_NAMESPACE = "http://niem.gov/niem/niem-core/2.0";
    static final String JXDM_NAMESPACE = "http://niem.gov/niem/domains/jxdm/4.1";
	
	public String getNamespaceURI(String prefix)
	{
		if ("er-ext".equals(prefix))
		{
			return EntityResolutionNamespaceContext.ER_EXT_NAMESPACE;
		} else if ("merge".equals(prefix))
		{
			return EntityResolutionNamespaceContext.MERGE_NAMESPACE;
		} else if ("merge-result".equals(prefix))
		{
			return EntityResolutionNamespaceContext.MERGE_RESULT_NAMESPACE;
		} else if ("merge-result-ext".equals(prefix))
		{
			return EntityResolutionNamespaceContext.MERGE_RESULT_EXT_NAMESPACE;
		} else if ("s".equals(prefix))
		{
			return EntityResolutionNamespaceContext.STRUCTURES_NAMESPACE;
        } else if ("nc".equals(prefix))
        {
            return EntityResolutionNamespaceContext.NC_NAMESPACE;
        } else if ("jxdm".equals(prefix))
        {
            return EntityResolutionNamespaceContext.JXDM_NAMESPACE;
        }

		return null;
	}

	public String getPrefix(String arg0)
	{
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Iterator getPrefixes(String arg0)
	{
		return null;
	}
}