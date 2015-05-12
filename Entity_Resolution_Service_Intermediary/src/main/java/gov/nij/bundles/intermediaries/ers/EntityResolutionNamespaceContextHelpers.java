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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;

/**
 * This class has helper methods to assist with parsing the attribute parameters.
 * 
 */

public class EntityResolutionNamespaceContextHelpers {

	private static final Log LOG = LogFactory.getLog( EntityResolutionNamespaceContextHelpers.class );
	
	/**
	 * 
	 * This method will return a namespace map from an attribute parameter node.  Each attribute parameter
	 * will have its own namespace map to facilitate xpath processing.
	 * 
	 * @param attributeXpathValue
	 * @param node
	 * @return
	 * @throws Exception
	 */
	public static Map<String, String> returnNamespaceMapFromNode (String attributeXpathValue, Node node) throws Exception
	{
		LOG.debug("Attribute parameter xpath value: " + attributeXpathValue);

		Map<String, String> namespaceMap = new HashMap<String, String>();
		
		//Get all prefixes from the xpath statement
		Set<String> prefixes = returnPrefixes(attributeXpathValue);
		
		//Lookup prefixes and add to namespace map
		for (String prefix : prefixes)
		{
			LOG.debug("Prefix returned from 'returnPrefixes' method : " + prefix);
			
			String namespace = node.lookupNamespaceURI(prefix);
			
			LOG.debug("Namespace Prefix: " + prefix + ", Namespace: " + namespace);
			
			namespaceMap.put(prefix, namespace);
			
		}	
			
		return namespaceMap;
	}
	
	/**
	 * This method returns all prefixes that are in an xpath expression
	 * 
	 * @param xpath
	 * @return
	 */
	public static Set<String> returnPrefixes (String xpath)
	{
		//Return all the strings by splitting on the regular expression that will find '/' and '//'
		//TODO: improve this regular expression, it will return empty strings in some use cases.
		String[] xpathValue = xpath.split("/|//");
		
		//Use a hashset for prefixes so we don't repeat namespaces
		Set<String> prefixes = new HashSet<String>();
		
		for (int i=0; i<xpathValue.length; i++)
		{
			
			//Check to see if string is empty, regular expression will return empty strings in some cases
			if (StringUtils.isNotEmpty(xpathValue[i]))
			{
				String prefixAndElement = xpathValue[i];
				
				//If we have an '@', we need to get that prefix as well
				if (prefixAndElement.contains("@"))
				{
					String[] prefixesFromAttributes = StringUtils.substringsBetween(prefixAndElement, "@", ":");
					
					for (int j=0; j<prefixesFromAttributes.length; j++)
					{
						prefixes.add(prefixesFromAttributes[j]);
					}	
					
				}	
				else
				{	
					//If there are no attributes, just get the namespace prefix which will appear before the ':'
					String prefix = StringUtils.substringBefore(prefixAndElement, ":");
					prefixes.add(prefix);
				}	
			}
		}	

		return prefixes;
		
	}
}
