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

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import junit.framework.TestCase;

import org.apache.camel.converter.jaxp.XmlConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class EntityResolutionNamespaceContextHelperTest extends TestCase {
	
	private static final Log LOG = LogFactory.getLog( EntityResolutionNamespaceContextHelperTest.class );
	
	/**
	 * This method tests the returnPrefixes method. The xpath expression is parsed and all the namespace
	 * prefixes are retrieved in a Set.
	 */
	@Test
	public void testReturnPrefixes()
	{
		String xpathWithoutAttributes = "ext:PersonSearchResult//ext:Person/nc:PersonName/nc:PersonGivenName";
		LOG.debug("Testing xpath: " + xpathWithoutAttributes);
		
		Set<String> prefixes = EntityResolutionNamespaceContextHelpers.returnPrefixes(xpathWithoutAttributes);
		
		for (String prefix : prefixes)
		{
			LOG.debug(prefix);
			assertTrue(prefixes.contains("ext"));
			assertTrue(prefixes.contains("nc"));
			assertEquals(2,prefixes.size());
		}	
		
		String xpathWithAttributes = "ext:PersonSearchResult/nc:Location[../nc:ResidenceAssociation/nc:LocationReference/@s:ref = @s:id and ../nc:ResidenceAssociation/nc:PersonReference/@s:ref=../jxdm:Person/@s:id]/nc:LocationAddress/nc:StructuredAddress/nc:LocationPostalCode";
		System.out.println("Testing xpath: " + xpathWithAttributes);

		prefixes = EntityResolutionNamespaceContextHelpers.returnPrefixes(xpathWithAttributes);
		
		for (String prefix : prefixes)
		{
			LOG.debug(prefix);
			assertTrue(prefixes.contains("ext"));
			assertTrue(prefixes.contains("nc"));
			assertTrue(prefixes.contains("s"));
			assertTrue(prefixes.contains("jxdm"));
			assertEquals(4,prefixes.size());
		}	

	}
	
	@Test
	public void testReturnNamespaceMapFromNode() throws Exception
	{
		//Retrieve ER xpath context
		XPath xpath;
		xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(new EntityResolutionNamespaceContext());

		//Get the test attribute parameters configuration
        InputStream attributeParametersStream = getClass().getResourceAsStream("/xml/TestAttributeParameters.xml");
        assertNotNull(attributeParametersStream);
        
        //Convert to DOM document
        XmlConverter converter = new XmlConverter();
        converter.getDocumentBuilderFactory().setNamespaceAware(true);

        Document attributeParametersDocument = converter.toDOMDocument(attributeParametersStream);
        
        NodeList parameterNodes = null;
        
        //Perform Xpath to retrieve attribute parameters
		parameterNodes = (NodeList) xpath.evaluate("er-ext:AttributeParameter",
				attributeParametersDocument.getDocumentElement(), XPathConstants.NODESET);
		
		//Loop through attribute parameters to retrieve namespace map associated with attribute xpath
		for (int i = 0; i < parameterNodes.getLength(); i++)
		{
			Node node = parameterNodes.item(i);
			
			String attributeXpathValue = xpath.evaluate("er-ext:AttributeXPath", node);
			Map<String, String> namespaceMap = EntityResolutionNamespaceContextHelpers.returnNamespaceMapFromNode (attributeXpathValue, node);
			
			for (Map.Entry<String, String> entry : namespaceMap.entrySet()) {
				LOG.debug("Namespace Map Entry, Prefix : " + entry.getKey() + " Namespace : " + entry.getValue());
			}
			
			if (attributeXpathValue.equals("ext:PersonSearchResult/ext:Person/nc:PersonName/nc:PersonGivenName"))
			{
				assertEquals("http://niem.gov/niem/niem-core/2.0", namespaceMap.get("nc"));
				assertEquals("http://local.org/IEPD/Extensions/PersonSearchResults/1.0", namespaceMap.get("ext"));
			}	

			if (attributeXpathValue.equals("ext:PersonSearchResult/ext:Person/nc:PersonName/nc:PersonSurName"))
			{
				assertEquals("http://niem.gov/niem/niem-core/2.0", namespaceMap.get("nc"));
				assertEquals("http://local.org/IEPD/Extensions/PersonSearchResults/1.0", namespaceMap.get("ext"));
			}	

		}
	}
}
