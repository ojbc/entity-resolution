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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.converter.jaxp.XmlConverter;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.binding.soap.SoapHeader;
import org.apache.cxf.headers.Header;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gov.nij.EntityResolutionService;

/**
 * This is the test framework to test the Entity Resolution Service.
 * 
 */

@CamelSpringBootTest
@SpringBootTest(classes=EntityResolutionService.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@UseAdviceWith
public class EntityResolutionServiceIntermediaryTest {

    private static final Log log = LogFactory.getLog(EntityResolutionServiceIntermediaryTest.class);

    public static final String CXF_OPERATION_NAME = "Submit-Entity-Merge";
    public static final String CXF_OPERATION_NAMESPACE = "http://nij.gov/Services/WSDL/EntityResolutionService/1.0";

    @Resource
    private ModelCamelContext context;

    @Produce
    protected ProducerTemplate template;

    @EndpointInject(value = "mock:EntityResolutionResponseEndpoint")
    protected MockEndpoint entityResolutionResponseMock;

    private Exchange senderExchange;
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

        // Advise the person search results endpoint and replace it with a mock endpoint.
        // We then will test this mock endpoint to see if it gets the proper payload.
    	AdviceWith.adviceWith(context, "entityResolutionRoute", 
			route ->  {
    			route.weaveByToString("To[EntityResolutionResponseEndpoint]").replace().to(entityResolutionResponseMock).stop(); 
    			route.replaceFromWith("direct:entityResolutionRequestServiceEndpoint");
			}
		);
        	
        context.start();

        // We should get one message
        entityResolutionResponseMock.expectedMessageCount(1);

        // Create a new exchange
        senderExchange = new DefaultExchange(context);

        Document doc = createDocument();
        List<SoapHeader> soapHeaders = new ArrayList<SoapHeader>();
        soapHeaders.add(makeSoapHeader(doc, "http://www.w3.org/2005/08/addressing", "MessageID", "12345"));
        soapHeaders.add(makeSoapHeader(doc, "http://www.w3.org/2005/08/addressing", "ReplyTo", "https://reply.to"));
        senderExchange.getIn().setHeader(Header.HEADER_LIST, soapHeaders);

        senderExchange.getIn().setHeader(CxfConstants.OPERATION_NAME, CXF_OPERATION_NAME);
        senderExchange.getIn().setHeader(CxfConstants.OPERATION_NAMESPACE, CXF_OPERATION_NAMESPACE);
    }

    @After
    public void tearDown() throws Exception {
        context.stop();
    }

    @Test
    @DirtiesContext
    public void testSortAscending() throws Exception {
        performSortTest(new File("src/test/resources/xml/EntityMergeRequestMessageSortTest.xml"), 1, 1, 1);
    }

    @Test
    @DirtiesContext
    public void testSortDescending() throws Exception {
        performSortTest(new File("src/test/resources/xml/EntityMergeRequestMessageDescendSortTest.xml"), -1, -1, -1);
    }

    @Test
    @DirtiesContext
    public void testSortMixed() throws Exception {
        performSortTest(new File("src/test/resources/xml/EntityMergeRequestMessageMixedSortTest.xml"), -1, 1, 1);
    }

    @Test
    @DirtiesContext
    public void testSortNullValue() throws Exception {
        performSortTest(new File("src/test/resources/xml/EntityMergeRequestMessageNullValueSortTest.xml"), 1, 1, 1);
    }

    @Test
    @DirtiesContext
    public void testSortWithMissingAttributeValue() throws Exception {
        performSortTest(new File("src/test/resources/xml/EntityMergeRequestMessageSortWithNoSortAttributeTest.xml"), 1, 0, 0);
    }

    private void performSortTest(File inputMessageFile, int factor1, int factor2, int factor3) throws Exception {
        
        senderExchange.getIn().setBody(inputMessageFile);
        Exchange returnExchange = template.send("direct:entityResolutionRequestServiceEndpoint", senderExchange);

        if (returnExchange.getException() != null) {
            throw new Exception(returnExchange.getException());
        }

        Thread.sleep(3000);

        entityResolutionResponseMock.assertIsSatisfied();
        entityResolutionResponseMock.expectedMessageCount(1);

        Exchange ex = entityResolutionResponseMock.getExchanges().get(0);
        Document responseDocument = ex.getIn().getBody(Document.class);
//        String docString = new XmlConverter().toString(responseDocument, ex);
//        log.info("\n" + docString + "\n");

        XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(testNamespaceContext);

        NodeList personNodes = (NodeList) xp.evaluate("/merge-result:EntityMergeResultMessage/merge-result:EntityContainer/merge-result-ext:Entity/ext:PersonSearchResult/ext:Person",
                responseDocument, XPathConstants.NODESET);
        assertEquals(5, personNodes.getLength());

        List<Node> lastNameNodes = new ArrayList<Node>();
        List<Node> firstNameNodes = new ArrayList<Node>();
        List<Node> idNodes = new ArrayList<Node>();

        for (int i = 0; i < personNodes.getLength(); i++) {
            Node personNode = personNodes.item(i);
            Node lastNameNode = (Node) xp.evaluate("nc:PersonName/nc:PersonSurName", personNode, XPathConstants.NODE);
            lastNameNodes.add(lastNameNode);
            Node firstNameNode = (Node) xp.evaluate("nc:PersonName/nc:PersonGivenName", personNode, XPathConstants.NODE);
            firstNameNodes.add(firstNameNode);
            Node idNode = (Node) xp.evaluate("jxdm:PersonAugmentation/jxdm:PersonStateFingerprintIdentification/nc:IdentificationID", personNode, XPathConstants.NODE);
            idNodes.add(idNode);
        }

        assertTrue(compareTextNodeLists(personNodes.getLength(), lastNameNodes, firstNameNodes, idNodes, factor1, factor2, factor3));
    }

    private boolean compareTextNodeLists(int personNodeCount, List<Node> nodeList1, List<Node> nodeList2, List<Node> nodeList3, int factor1, int factor2, int factor3) {
        boolean sorted = true;
        String priorNode1 = nodeList1.get(0).getTextContent();
        String priorNode2 = nodeList2.get(0).getTextContent();
        String priorNode3 = nodeList3.get(0).getTextContent();
        for (int i = 1; sorted && i < personNodeCount; i++) {
            Node node1 = nodeList1.get(i);
            String node1S = node1 == null ? null : node1.getTextContent();
            Node node2 = nodeList2.get(i);
            String node2S = node2 == null ? null : node2.getTextContent();
            Node node3 = nodeList3.get(i);
            String node3S = node3 == null ? null : node3.getTextContent();
            int node1Compare = compareWithNull(priorNode1, node1S) * factor1;
            priorNode1 = node1S;
            if (node1Compare > 0) {
                sorted = false;
                //log.info("Sort fails on last name compare " + priorNode1 + " " + node1S);
            } else if (node1Compare == 0) {
                int node2Compare = compareWithNull(priorNode2, node2S) * factor2;
                priorNode2 = node2S;
                if (node2Compare > 0) {
                    sorted = false;
                    //log.info("Sort fails on first name compare " + priorNode2 + " " + node2S);
                } else if (node2Compare == 0) {
                    int node3Compare = compareWithNull(priorNode3, node3S) * factor3;
                    priorNode3 = node3S;
                    if (node3Compare > 0) {
                        sorted = false;
                        //log.info("Sort fails on id compare " + priorNode3 + " " + node3S);
                    }
                }
            }
        }
        return sorted;
    }

    private int compareWithNull(String s1, String s2) {
        // assumes nulls sort first
        if (s1 == null) {
            if (s2 == null) {
                return 0;
            }
            return 1;
        } else if (s2 == null) {
            return -1;
        }
        //log.info("Returning compare of s1=" + s1 + " to s2=" + s2 + ", return=" + s1.compareTo(s2));
        return s1.compareTo(s2);
    }

    /**
     * Test the entity resolution service.
     * 
     * @throws Exception
     */
    @Test
    @DirtiesContext
    public void testEntityResolution() throws Exception {

        // Read the er search request file from the file system
        File inputFile = new File("src/test/resources/xml/EntityMergeRequestMessageWithAttributeParameters.xml");
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document inputDocument = dbf.newDocumentBuilder().parse(inputFile);

        // Set it as the message message body
        senderExchange.getIn().setBody(inputDocument);

        // Send the one-way exchange. Using template.send will send an one way message
        Exchange returnExchange = template.send("direct:entityResolutionRequestServiceEndpoint", senderExchange);

        // Use getException to see if we received an exception
        if (returnExchange.getException() != null) {
            throw new Exception(returnExchange.getException());
        }

        // Sleep while a response is generated
        Thread.sleep(3000);

        // Assert that the mock endpoint is satisfied
        entityResolutionResponseMock.assertIsSatisfied();

        // We should get one message
        entityResolutionResponseMock.expectedMessageCount(1);

        // Get the first exchange (the only one)
        Exchange ex = entityResolutionResponseMock.getExchanges().get(0);

        // Get the actual response
        Document actualResponse = ex.getIn().getBody(Document.class);
        log.info("Input document: " + new XmlConverter().toStringFromDocument(inputDocument, null));
        log.info("Body recieved by Mock: " + new XmlConverter().toStringFromDocument(actualResponse, null));

        XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(testNamespaceContext);
        // note:  slash-slash xpaths are ok here because in tests we don't really care about performance...
        int inputEntityNodeCount = ((NodeList) xp.evaluate("//er-ext:Entity", inputDocument, XPathConstants.NODESET)).getLength();
        int outputEntityNodeCount = ((NodeList) xp.evaluate("//merge-result-ext:Entity", actualResponse, XPathConstants.NODESET)).getLength();
        NodeList outputOriginalRecordReferenceNodeList = (NodeList) xp.evaluate("//merge-result-ext:OriginalRecordReference", actualResponse, XPathConstants.NODESET);
        int outputOriginalRecordReferenceNodeCount = outputOriginalRecordReferenceNodeList.getLength();
        assertEquals(inputEntityNodeCount, outputEntityNodeCount);
        assertEquals(inputEntityNodeCount, outputOriginalRecordReferenceNodeCount);
        
        NodeList inputPersonNodes = (NodeList) xp.evaluate("//ext:Person", inputDocument, XPathConstants.NODESET);
        for (int i=0;i < inputPersonNodes.getLength();i++) {
            String inputLastName = xp.evaluate("nc:PersonName/nc:PersonSurName/text()", inputPersonNodes.item(i));
            if (inputLastName != null) {
                String xpathExpression = "//ext:Person[nc:PersonName/nc:PersonSurName/text()='" + inputLastName + "']";
                assertNotNull(xp.evaluate(xpathExpression, actualResponse, XPathConstants.NODE));
            }
            String inputFirstName = xp.evaluate("nc:PersonName/nc:PersonGivenName/text()", inputPersonNodes.item(i));
            if (inputFirstName != null) {
                String xpathExpression = "//ext:Person[nc:PersonName/nc:PersonGivenName/text()='" + inputFirstName + "']";
                log.info("xpathExpression=" + xpathExpression);
                assertNotNull(xp.evaluate(xpathExpression, actualResponse, XPathConstants.NODE));
            }
            String inputId = xp.evaluate("jxdm:PersonAugmentation/jxdm:PersonStateFingerprintIdentification/nc:IdentificationID", inputPersonNodes.item(i));
            if (inputId != null) {
                String xpathExpression = "//ext:Person[jxdm:PersonAugmentation/jxdm:PersonStateFingerprintIdentification/nc:IdentificationID/text()='" + inputId + "']";
                assertNotNull(xp.evaluate(xpathExpression, actualResponse, XPathConstants.NODE));
            }
        }

        for (int i=0;i < outputOriginalRecordReferenceNodeCount;i++) {
            String nodeRef = ((Element) outputOriginalRecordReferenceNodeList.item(i)).getAttributeNS("http://niem.gov/niem/structures/2.0", "ref");
            assertNotNull(xp.evaluate("//merge-result-ext:Entity[@s:id='" + nodeRef + "']", actualResponse, XPathConstants.NODE));
        }

    }

    private SoapHeader makeSoapHeader(Document doc, String namespace, String localName, String value) {
        Element messageId = doc.createElementNS(namespace, localName);
        messageId.setTextContent(value);
        SoapHeader soapHeader = new SoapHeader(new QName(namespace, localName), messageId);
        return soapHeader;
    }

    private Document createDocument() throws Exception {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().newDocument();

        return doc;
    }

}
