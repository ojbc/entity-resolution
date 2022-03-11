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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.camel.converter.jaxp.XmlConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gov.nij.bundles.intermediaries.ers.osgi.AttributeParameters;
import gov.nij.bundles.intermediaries.ers.osgi.EntityResolutionConversionUtils;
import gov.nij.bundles.intermediaries.ers.osgi.ExternallyIdentifiableRecord;
import gov.nij.bundles.intermediaries.ers.osgi.RecordWrapper;
import gov.nij.processor.AttributeParametersXpathSupport;
import junit.framework.TestCase;
import serf.data.Attribute;

public class EntityResolutionMessageHandlerTest extends TestCase {

    private static final Log LOG = LogFactory.getLog(EntityResolutionMessageHandlerTest.class);

    private EntityResolutionMessageHandler entityResolutionMessageHandler;
    private InputStream testRequestMessageInputStream;
    private InputStream testAttributeParametersMessageInputStream;
    private NamespaceContext testNamespaceContext;

    @Before
    public void setUp() throws Exception {
        final NamespaceContext baseEntityResolutionNamespaceContext = new EntityResolutionNamespaceContext();
        testNamespaceContext = new NamespaceContext() {

            @Override
            public String getNamespaceURI(String prefix) {
                if ("ext".equals(prefix)) {
                    return "http://local.org/IEPD/Extensions/PersonSearchResults/1.0";
                }
                return baseEntityResolutionNamespaceContext.getNamespaceURI(prefix);
            }

            @Override
            public String getPrefix(String arg0) {
                return baseEntityResolutionNamespaceContext.getPrefix(arg0);
            }

            @SuppressWarnings("rawtypes")
            @Override
            public Iterator getPrefixes(String arg0) {
                return baseEntityResolutionNamespaceContext.getPrefixes(arg0);
            }

        };
        entityResolutionMessageHandler = new EntityResolutionMessageHandler();
        testAttributeParametersMessageInputStream = getClass().getResourceAsStream("/xml/TestAttributeParameters.xml");
        assertNotNull(testAttributeParametersMessageInputStream);
        entityResolutionMessageHandler.setAttributeParametersStream(testAttributeParametersMessageInputStream);
        testRequestMessageInputStream = getClass().getResourceAsStream("/xml/EntityMergeRequestMessage.xml");
        assertNotNull(testRequestMessageInputStream);
    }

    @Test
    public void testPerformEntityResolutionWithDetermFactors() throws Exception {
        XmlConverter converter = new XmlConverter();
        converter.getDocumentBuilderFactory().setNamespaceAware(true);
        InputStream attributeParametersStream = getClass().getResourceAsStream("/xml/TestAttributeParametersWithDeterm.xml");
        entityResolutionMessageHandler.setAttributeParametersStream(attributeParametersStream);
        testRequestMessageInputStream = getClass().getResourceAsStream("/xml/EntityMergeRequestMessageForDeterm.xml");
        Document testRequestMessage = converter.toDOMDocument(testRequestMessageInputStream, null);

        Node entityContainerNode = testRequestMessage.getElementsByTagNameNS(EntityResolutionNamespaceContext.ER_EXT_NAMESPACE, "EntityContainer").item(0);
        assertNotNull(entityContainerNode);
        Document resultDocument = entityResolutionMessageHandler.performEntityResolution(entityContainerNode, null, null);

        resultDocument.normalizeDocument();
        // LOG.info(converter.toString(resultDocument));
        XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new EntityResolutionNamespaceContext());
        NodeList entityNodes = (NodeList) xp.evaluate("//merge-result:EntityContainer/merge-result-ext:Entity", resultDocument, XPathConstants.NODESET);
        assertEquals(2, entityNodes.getLength());
        entityNodes = (NodeList) xp.evaluate("//merge-result-ext:MergedRecord", resultDocument, XPathConstants.NODESET);
        assertEquals(2, entityNodes.getLength());
        entityNodes = (NodeList) xp.evaluate("//merge-result-ext:OriginalRecordReference", resultDocument, XPathConstants.NODESET);
        assertEquals(2, entityNodes.getLength());
        for (int i = 0; i < entityNodes.getLength(); i++) {
            Element e = (Element) entityNodes.item(i);
            String entityIdRef = e.getAttributeNS(EntityResolutionNamespaceContext.STRUCTURES_NAMESPACE, "ref");
            assertNotNull(entityIdRef);
            assertNotNull(xp.evaluate("//merge-result-ext:Entity[@s:id='" + entityIdRef + "']", resultDocument, XPathConstants.NODE));
        }
    }

    @Test
    public void testRecordLimit() throws Exception {

        XmlConverter converter = new XmlConverter();
        converter.getDocumentBuilderFactory().setNamespaceAware(true);
        Document testRequestMessage = converter.toDOMDocument(testRequestMessageInputStream, null);

        Node entityContainerNode = testRequestMessage.getElementsByTagNameNS(EntityResolutionNamespaceContext.ER_EXT_NAMESPACE, "EntityContainer").item(0);
        assertNotNull(entityContainerNode);

        Node entityResolutionConfigurationNode = makeEntityResolutionConfigurationNode(String.valueOf(Integer.MAX_VALUE));

        Document resultDocument = entityResolutionMessageHandler.performEntityResolution(entityContainerNode, null, entityResolutionConfigurationNode);

        XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new EntityResolutionNamespaceContext());
        NodeList entityNodes = (NodeList) xp.evaluate("//merge-result:EntityContainer/merge-result-ext:Entity", resultDocument, XPathConstants.NODESET);
        int inputEntityNodeCount = 6;
        assertEquals(inputEntityNodeCount, entityNodes.getLength());
        entityNodes = (NodeList) xp.evaluate("//merge-result-ext:MergedRecord", resultDocument, XPathConstants.NODESET);
        assertEquals(3, entityNodes.getLength());
        String recordLimitExceeded = xp.evaluate("/merge-result:EntityMergeResultMessage/merge-result:RecordLimitExceededIndicator", resultDocument);
        assertEquals("false", recordLimitExceeded);

        entityResolutionConfigurationNode = makeEntityResolutionConfigurationNode(6 + "");

        resultDocument = entityResolutionMessageHandler.performEntityResolution(entityContainerNode, null, entityResolutionConfigurationNode);

        xp.setNamespaceContext(new EntityResolutionNamespaceContext());
        entityNodes = (NodeList) xp.evaluate("//merge-result:EntityContainer/merge-result-ext:Entity", resultDocument, XPathConstants.NODESET);
        inputEntityNodeCount = 6;
        assertEquals(inputEntityNodeCount, entityNodes.getLength());
        entityNodes = (NodeList) xp.evaluate("//merge-result-ext:MergedRecord", resultDocument, XPathConstants.NODESET);
        assertEquals(3, entityNodes.getLength());
        recordLimitExceeded = xp.evaluate("/merge-result:EntityMergeResultMessage/merge-result:RecordLimitExceededIndicator", resultDocument);
        assertEquals("false", recordLimitExceeded);

        entityResolutionConfigurationNode = makeEntityResolutionConfigurationNode(null);

        resultDocument = entityResolutionMessageHandler.performEntityResolution(entityContainerNode, null, entityResolutionConfigurationNode);

        xp.setNamespaceContext(new EntityResolutionNamespaceContext());
        entityNodes = (NodeList) xp.evaluate("//merge-result:EntityContainer/merge-result-ext:Entity", resultDocument, XPathConstants.NODESET);
        inputEntityNodeCount = 6;
        assertEquals(inputEntityNodeCount, entityNodes.getLength());
        entityNodes = (NodeList) xp.evaluate("//merge-result-ext:MergedRecord", resultDocument, XPathConstants.NODESET);
        assertEquals(3, entityNodes.getLength());
        recordLimitExceeded = xp.evaluate("/merge-result:EntityMergeResultMessage/merge-result:RecordLimitExceededIndicator", resultDocument);
        assertEquals("false", recordLimitExceeded);

        entityResolutionConfigurationNode = makeEntityResolutionConfigurationNode("not an int");

        resultDocument = entityResolutionMessageHandler.performEntityResolution(entityContainerNode, null, entityResolutionConfigurationNode);

        xp.setNamespaceContext(new EntityResolutionNamespaceContext());
        entityNodes = (NodeList) xp.evaluate("//merge-result:EntityContainer/merge-result-ext:Entity", resultDocument, XPathConstants.NODESET);
        inputEntityNodeCount = 6;
        assertEquals(inputEntityNodeCount, entityNodes.getLength());
        entityNodes = (NodeList) xp.evaluate("//merge-result-ext:MergedRecord", resultDocument, XPathConstants.NODESET);
        assertEquals(3, entityNodes.getLength());
        recordLimitExceeded = xp.evaluate("/merge-result:EntityMergeResultMessage/merge-result:RecordLimitExceededIndicator", resultDocument);
        assertEquals("false", recordLimitExceeded);

        entityResolutionConfigurationNode = makeEntityResolutionConfigurationNode(2 + "");

        Document attributeParametersDocument = entityResolutionMessageHandler.getAttributeParametersDocument();
        resultDocument = entityResolutionMessageHandler.performEntityResolution(entityContainerNode, attributeParametersDocument.getDocumentElement(), entityResolutionConfigurationNode);

        xp.setNamespaceContext(new EntityResolutionNamespaceContext());
        entityNodes = (NodeList) xp.evaluate("//merge-result:EntityContainer/merge-result-ext:Entity", resultDocument, XPathConstants.NODESET);
        inputEntityNodeCount = 6;
        assertEquals(inputEntityNodeCount, entityNodes.getLength());
        entityNodes = (NodeList) xp.evaluate("//merge-result-ext:MergedRecord", resultDocument, XPathConstants.NODESET);
        assertEquals(6, entityNodes.getLength());
        recordLimitExceeded = xp.evaluate("/merge-result:EntityMergeResultMessage/merge-result:RecordLimitExceededIndicator", resultDocument);
        assertEquals("true", recordLimitExceeded);

        // LOG.info(new XmlConverter().toString(resultDocument));
        NodeList statNodes = (NodeList) xp.evaluate("//merge-result-ext:MergedRecord/merge-result-ext:MergeQuality", resultDocument, XPathConstants.NODESET);
        assertEquals(6, statNodes.getLength());
        statNodes = (NodeList) xp.evaluate("//merge-result-ext:MergedRecord/merge-result-ext:MergeQuality/merge-result-ext:StringDistanceStatistics", resultDocument, XPathConstants.NODESET);
        assertEquals(12, statNodes.getLength());
        statNodes = (NodeList) xp.evaluate("//merge-result-ext:MergedRecord/merge-result-ext:MergeQuality/merge-result-ext:StringDistanceStatistics/merge-result-ext:AttributeXPath", resultDocument,
                XPathConstants.NODESET);
        assertEquals(12, statNodes.getLength());
        statNodes = (NodeList) xp.evaluate("//merge-result-ext:MergedRecord/merge-result-ext:MergeQuality/merge-result-ext:StringDistanceStatistics/merge-result-ext:StringDistanceMeanInRecord",
                resultDocument, XPathConstants.NODESET);
        assertEquals(12, statNodes.getLength());
        statNodes = (NodeList) xp.evaluate(
                "//merge-result-ext:MergedRecord/merge-result-ext:MergeQuality/merge-result-ext:StringDistanceStatistics/merge-result-ext:StringDistanceStandardDeviationInRecord", resultDocument,
                XPathConstants.NODESET);
        assertEquals(12, statNodes.getLength());
    }

    /**
     * This unit test will read a entity merge request document that has given and sur names in mixed case.
     * It will then set an ER threshold of 3 and pass in six entities so ER is skipped.
     * However, the results should still be sorted and passed back in order.
     * 
     * @throws Exception
     */
    
    @Test
    public void testRecordLimitSorting() throws Exception {

        XmlConverter converter = new XmlConverter();
        converter.getDocumentBuilderFactory().setNamespaceAware(true);
        Document testRequestMessage = converter.toDOMDocument(getClass().getResourceAsStream("/xml/EntityMergeRequestMessageMixedCase.xml"), null);

        Node entityContainerNode = testRequestMessage.getElementsByTagNameNS(EntityResolutionNamespaceContext.ER_EXT_NAMESPACE, "EntityContainer").item(0);
        assertNotNull(entityContainerNode);

        Node entityResolutionConfigurationNode = makeEntityResolutionConfigurationNode("2");

        Document attributeParametersDocument = entityResolutionMessageHandler.getAttributeParametersDocument();
        
        Document resultDocument = entityResolutionMessageHandler.performEntityResolution(entityContainerNode, attributeParametersDocument.getDocumentElement(), entityResolutionConfigurationNode);

        XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(testNamespaceContext);
        NodeList entityNodes = (NodeList) xp.evaluate("//merge-result:EntityContainer/merge-result-ext:Entity", resultDocument, XPathConstants.NODESET);
        int inputEntityNodeCount = 6;
        assertEquals(inputEntityNodeCount, entityNodes.getLength());
        entityNodes = (NodeList) xp.evaluate("//merge-result-ext:MergedRecord", resultDocument, XPathConstants.NODESET);
        assertEquals(6, entityNodes.getLength());
        String recordLimitExceeded = xp.evaluate("/merge-result:EntityMergeResultMessage/merge-result:RecordLimitExceededIndicator", resultDocument);
        assertEquals("true", recordLimitExceeded);

        assertEquals("DUCK", xp.evaluate("//merge-result:EntityContainer/merge-result-ext:Entity[1]/ext:PersonSearchResult/ext:Person/nc:PersonName/nc:PersonSurName", resultDocument));
        assertEquals("Donald", xp.evaluate("//merge-result:EntityContainer/merge-result-ext:Entity[1]/ext:PersonSearchResult/ext:Person/nc:PersonName/nc:PersonGivenName", resultDocument));
        
        assertEquals("MOUSE", xp.evaluate("//merge-result:EntityContainer/merge-result-ext:Entity[2]/ext:PersonSearchResult/ext:Person/nc:PersonName/nc:PersonSurName", resultDocument));
        assertEquals("FRANK", xp.evaluate("//merge-result:EntityContainer/merge-result-ext:Entity[2]/ext:PersonSearchResult/ext:Person/nc:PersonName/nc:PersonGivenName", resultDocument));

        assertEquals("mouse", xp.evaluate("//merge-result:EntityContainer/merge-result-ext:Entity[3]/ext:PersonSearchResult/ext:Person/nc:PersonName/nc:PersonSurName", resultDocument));
        assertEquals("macky", xp.evaluate("//merge-result:EntityContainer/merge-result-ext:Entity[3]/ext:PersonSearchResult/ext:Person/nc:PersonName/nc:PersonGivenName", resultDocument));

        assertEquals("MouSe", xp.evaluate("//merge-result:EntityContainer/merge-result-ext:Entity[4]/ext:PersonSearchResult/ext:Person/nc:PersonName/nc:PersonSurName", resultDocument));
        assertEquals("Mickey", xp.evaluate("//merge-result:EntityContainer/merge-result-ext:Entity[4]/ext:PersonSearchResult/ext:Person/nc:PersonName/nc:PersonGivenName", resultDocument));

        assertEquals("MOUSE", xp.evaluate("//merge-result:EntityContainer/merge-result-ext:Entity[5]/ext:PersonSearchResult/ext:Person/nc:PersonName/nc:PersonSurName", resultDocument));
        assertEquals("Minn", xp.evaluate("//merge-result:EntityContainer/merge-result-ext:Entity[5]/ext:PersonSearchResult/ext:Person/nc:PersonName/nc:PersonGivenName", resultDocument));
        
        assertEquals("Mouse", xp.evaluate("//merge-result:EntityContainer/merge-result-ext:Entity[6]/ext:PersonSearchResult/ext:Person/nc:PersonName/nc:PersonSurName", resultDocument));
        assertEquals("MINNY", xp.evaluate("//merge-result:EntityContainer/merge-result-ext:Entity[6]/ext:PersonSearchResult/ext:Person/nc:PersonName/nc:PersonGivenName", resultDocument));

        //LOG.info(new XmlConverter().toString(resultDocument));

    }

    
    
    private Node makeEntityResolutionConfigurationNode(String limit) throws Exception {
        try {
            if (Integer.parseInt(limit) == Integer.MAX_VALUE) {
                return null;
            }
        } catch (NumberFormatException nfe) {
            return null;
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document d = db.newDocument();
        Element ret = d.createElementNS(EntityResolutionNamespaceContext.ER_EXT_NAMESPACE, "EntityResolutionConfiguration");
        d.appendChild(ret);
        Element e = d.createElementNS(EntityResolutionNamespaceContext.ER_EXT_NAMESPACE, "RecordLimit");
        ret.appendChild(e);
        e.setTextContent(limit);
        return ret;
    }

    @Test
    public void testPerformEntityResolution() throws Exception {

        XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(testNamespaceContext);

        XmlConverter converter = new XmlConverter();
        converter.getDocumentBuilderFactory().setNamespaceAware(true);
        Document testRequestMessage = converter.toDOMDocument(testRequestMessageInputStream, null);
        // LOG.info(converter.toString(testRequestMessage));

        Node entityContainerNode = testRequestMessage.getElementsByTagNameNS(EntityResolutionNamespaceContext.ER_EXT_NAMESPACE, "EntityContainer").item(0);
        assertNotNull(entityContainerNode);

        List<String> lastNames = new ArrayList<String>();
        List<String> firstNames = new ArrayList<String>();
        List<String> sids = new ArrayList<String>();

        NodeList inputEntityNodes = (NodeList) testRequestMessage.getElementsByTagNameNS(EntityResolutionNamespaceContext.ER_EXT_NAMESPACE, "Entity");
        int inputEntityNodeCount = 6;
        assertEquals(inputEntityNodeCount, inputEntityNodes.getLength());

        for (int i = 0; i < inputEntityNodeCount; i++) {
            Node entityNode = inputEntityNodes.item(i);
            lastNames.add(xp.evaluate("ext:PersonSearchResult/ext:Person/nc:PersonName/nc:PersonSurName", entityNode));
            firstNames.add(xp.evaluate("ext:PersonSearchResult/ext:Person/nc:PersonName/nc:PersonGivenName", entityNode));
            sids.add(xp.evaluate("ext:PersonSearchResult/ext:Person/jxdm:PersonAugmentation/jxdm:PersonStateFingerprintIdentification/nc:IdentificationID", entityNode));
        }

        Map<String, Integer> lastNameCountMap = new HashMap<String, Integer>();
        for (String lastName : lastNames) {
            Integer count = lastNameCountMap.get(lastName);
            if (count == null) {
                count = 0;
            }
            lastNameCountMap.put(lastName, ++count);
        }
        Map<String, Integer> firstNameCountMap = new HashMap<String, Integer>();
        for (String firstName : firstNames) {
            Integer count = firstNameCountMap.get(firstName);
            if (count == null) {
                count = 0;
            }
            firstNameCountMap.put(firstName, ++count);
        }
        Map<String, Integer> sidCountMap = new HashMap<String, Integer>();
        for (String sid : sids) {
            Integer count = sidCountMap.get(sid);
            if (count == null) {
                count = 0;
            }
            sidCountMap.put(sid, ++count);
        }

        Document resultDocument = entityResolutionMessageHandler.performEntityResolution(entityContainerNode, null, null);

        resultDocument.normalizeDocument();
        // LOG.info(converter.toString(resultDocument));
        NodeList entityNodes = (NodeList) xp.evaluate("//merge-result:EntityContainer/merge-result-ext:Entity", resultDocument, XPathConstants.NODESET);
        assertEquals(inputEntityNodeCount, entityNodes.getLength());
        entityNodes = (NodeList) xp.evaluate("//merge-result-ext:MergedRecord", resultDocument, XPathConstants.NODESET);
        assertEquals(3, entityNodes.getLength());
        entityNodes = (NodeList) xp.evaluate("//merge-result-ext:OriginalRecordReference", resultDocument, XPathConstants.NODESET);
        assertEquals(inputEntityNodeCount, entityNodes.getLength());
        for (int i = 0; i < entityNodes.getLength(); i++) {
            Element e = (Element) entityNodes.item(i);
            String entityIdRef = e.getAttributeNS(EntityResolutionNamespaceContext.STRUCTURES_NAMESPACE, "ref");
            assertNotNull(entityIdRef);
            assertNotNull(xp.evaluate("//merge-result-ext:Entity[@s:id='" + entityIdRef + "']", resultDocument, XPathConstants.NODE));
        }
        for (String lastName : lastNameCountMap.keySet()) {
            assertEquals(lastNameCountMap.get(lastName).intValue(), ((Number) xp.evaluate("count(//merge-result-ext:Entity[ext:PersonSearchResult/ext:Person/nc:PersonName/nc:PersonSurName='" + lastName + "'])", resultDocument, XPathConstants.NUMBER)).intValue());
        }
        for (String firstName : firstNames) {
            assertEquals(firstNameCountMap.get(firstName).intValue(), ((Number) xp.evaluate("count(//merge-result-ext:Entity[ext:PersonSearchResult/ext:Person/nc:PersonName/nc:PersonGivenName='" + firstName + "'])", resultDocument, XPathConstants.NUMBER)).intValue());
        }
        for (String sid : sids) {
            assertEquals(sidCountMap.get(sid).intValue(), ((Number) xp.evaluate("count(//merge-result-ext:Entity[ext:PersonSearchResult/ext:Person/jxdm:PersonAugmentation/jxdm:PersonStateFingerprintIdentification/nc:IdentificationID='" + sid
                    + "'])", resultDocument, XPathConstants.NUMBER)).intValue());
        }

        String recordLimitExceeded = xp.evaluate("/merge-result:EntityMergeResultMessage/merge-result:RecordLimitExceededIndicator", resultDocument);
        assertEquals("false", recordLimitExceeded);

    }

    @Test
    public void testAttributeParametersSetup() throws Exception {
        Set<AttributeParametersXpathSupport> attributeParameters = entityResolutionMessageHandler.getAttributeParameters(null);
        assertEquals(2, attributeParameters.size());
        boolean givenNameFound = false;
        boolean surNameFound = false;
        for (AttributeParameters ap : attributeParameters) {
            if ("ext:PersonSearchResult/ext:Person/nc:PersonName/nc:PersonGivenName".equals(ap.getAttributeName())) {
                givenNameFound = true;
                assertEquals(0.8, ap.getThreshold());
            } else if ("ext:PersonSearchResult/ext:Person/nc:PersonName/nc:PersonSurName".equals(ap.getAttributeName())) {
                surNameFound = true;
                assertEquals(0.5, ap.getThreshold());
                assertEquals(1, ap.getSortOrder().getSortOrderRank());
                assertEquals("ascending", ap.getSortOrder().getSortOrder());
            }
            assertEquals("com.wcohen.ss.Jaro", ap.getAlgorithmClassName());
            assertFalse(ap.isDeterminative());
        }
        assertTrue(givenNameFound && surNameFound);
    }

    @Test
    public void testCreateRecords() throws Exception {
        XmlConverter converter = new XmlConverter();
        converter.getDocumentBuilderFactory().setNamespaceAware(true);
        Document testRequestMessage = converter.toDOMDocument(testRequestMessageInputStream, null);
        assertNotNull(testRequestMessage);

        Node entityContainerNode = testRequestMessage.getElementsByTagNameNS(EntityResolutionNamespaceContext.ER_EXT_NAMESPACE, "EntityContainer").item(0);
        NodeList entityNodeList = ((Element) entityContainerNode).getElementsByTagNameNS(EntityResolutionNamespaceContext.ER_EXT_NAMESPACE, "Entity");
        assertNotNull(entityContainerNode);
        assertNotNull(entityNodeList);

        List<ExternallyIdentifiableRecord> records = EntityResolutionConversionUtils.convertRecordWrappers(entityResolutionMessageHandler.createRecordsFromRequestMessage(entityNodeList, null));

        assertNotNull(records);
        assertEquals(6, records.size());
        boolean mickeyFound = false;
        boolean minnieFound = false;
        boolean minnyFound = false;
        for (ExternallyIdentifiableRecord record : records) {
            Attribute a = record.getAttribute("ext:PersonSearchResult/ext:Person/nc:PersonName/nc:PersonGivenName");
            assertNotNull(a);
            assertEquals(1, a.getValuesCount());
            String value = a.iterator().next();
            if ("Mickey".equals(value)) {
                mickeyFound = true;
            } else if ("Minnie".equals(value)) {
                minnieFound = true;
            } else if ("Minny".equals(value)) {
                minnyFound = true;
            }
        }
        assertTrue(mickeyFound);
        assertTrue(minnieFound);
        assertTrue(minnyFound);
    }

    @Ignore // only use this test to explore performance issues
    @Test
    public void testSortLargeRecordSet() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document entityMergeRequestDocument = dbf.newDocumentBuilder().parse(new File("src/test/resources/xml/EntityMergeRequestMessageSortTest.xml"));
        Document attributeParametersDocument = dbf.newDocumentBuilder().parse(new File("src/test/resources/xml/TestAttributeParameters.xml"));
        XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new EntityResolutionNamespaceContext());
        Element entityContainerElement = (Element) xp.evaluate("/merge:EntityMergeRequestMessage/merge:MergeParameters/er-ext:EntityContainer", entityMergeRequestDocument, XPathConstants.NODE);
        NodeList entityNodes = (NodeList) xp.evaluate("er-ext:Entity", entityContainerElement, XPathConstants.NODESET);
        int newEntityGroupCount = 100;
        for (int i = 0; i < newEntityGroupCount; i++) {
            for (int j = 0; j < entityNodes.getLength(); j++) {
                Element newEntityElement = (Element) entityNodes.item(j).cloneNode(true);
                entityContainerElement.appendChild(newEntityElement);
            }
        }
        NodeList newEntityNodes = (NodeList) xp.evaluate("er-ext:Entity", entityContainerElement, XPathConstants.NODESET);
        assertEquals((newEntityGroupCount + 1) * entityNodes.getLength(), newEntityNodes.getLength());
        LOG.info("Large Record Set sort test, record count=" + newEntityNodes.getLength());
        // instrument the following line as needed for performance testing
        @SuppressWarnings("unused")
        Document resultDocument = entityResolutionMessageHandler.performEntityResolution(entityContainerElement, attributeParametersDocument.getDocumentElement(),
                makeEntityResolutionConfigurationNode(1 + ""));
        // LOG.info(new XmlConverter().toString(resultDocument));
    }

    @Test
    public void testCreateLargeRecordSet() throws Exception {
        XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new EntityResolutionNamespaceContext());
        XmlConverter converter = new XmlConverter();
        converter.getDocumentBuilderFactory().setNamespaceAware(true);
        Document testRequestMessage = converter.toDOMDocument(testRequestMessageInputStream, null);
        Element entityContainerElement = (Element) xp.evaluate("/merge:EntityMergeRequestMessage/merge:MergeParameters/er-ext:EntityContainer", testRequestMessage, XPathConstants.NODE);
        assertNotNull(entityContainerElement);
        Element entityElement = (Element) xp.evaluate("er-ext:Entity[1]", entityContainerElement, XPathConstants.NODE);
        assertNotNull(entityElement);
        int entityCount = ((NodeList) xp.evaluate("er-ext:Entity", entityContainerElement, XPathConstants.NODESET)).getLength();
        int expectedInitialEntityCount = 6;
        assertEquals(expectedInitialEntityCount, entityCount);
        int recordIncrement = 500;
        for (int i = 0; i < recordIncrement; i++) {
            Element newEntityElement = (Element) entityElement.cloneNode(true);
            entityContainerElement.appendChild(newEntityElement);
        }
        entityCount = ((NodeList) xp.evaluate("er-ext:Entity", entityContainerElement, XPathConstants.NODESET)).getLength();
        assertEquals(expectedInitialEntityCount + recordIncrement, entityCount);

        Node entityContainerNode = testRequestMessage.getElementsByTagNameNS(EntityResolutionNamespaceContext.ER_EXT_NAMESPACE, "EntityContainer").item(0);
        assertNotNull(entityContainerNode);

        NodeList entityNodeList = ((Element) entityContainerNode).getElementsByTagNameNS(EntityResolutionNamespaceContext.ER_EXT_NAMESPACE, "Entity");
        List<RecordWrapper> records = entityResolutionMessageHandler.createRecordsFromRequestMessage(entityNodeList, null);
        assertEquals(expectedInitialEntityCount + recordIncrement, records.size());
    }
}
