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
package gov.nij.processor;

import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

import gov.nij.bundles.intermediaries.ers.EntityResolutionNamespaceContextHelpers;
import gov.nij.bundles.intermediaries.ers.EntityResolutionNamespaceContextMapImpl;
import gov.nij.bundles.intermediaries.ers.osgi.AttributeParameters;

/**
 * While retrieving the attribute parameters, it is necessary to have each attribute parameter have its own
 * namespace context so we can run the xpath query to obtain the entity value.
 * 
 * Rather than modify the AttributeParameters class in the OSGi bundle to support Xpath, it is extended here
 * for use in the web service context. Entity Resolution can be run outside of a web service context using
 * java objects so this class is specific to a Web Service or XML implementation. 
 * 
 * 
 */
public class AttributeParametersXpathSupport extends AttributeParameters{

	private static final long serialVersionUID = 1234234232444433L;
	
	private XPath xpath;
	
	public AttributeParametersXpathSupport(String attributeName, Node parameterNode) throws Exception {
		super(attributeName);

		//Parse string to retrieve namespace prefixes for elements and attributes, put into map
		Map<String, String> namespaceMap = EntityResolutionNamespaceContextHelpers.returnNamespaceMapFromNode(attributeName, parameterNode);
		
		//Create namespace context here with the map above
		xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(new EntityResolutionNamespaceContextMapImpl(namespaceMap));

	}

	public XPath getXpath() {
		return xpath;
	}

	public void setXpath(XPath xpath) {
		this.xpath = xpath;
	}

}
