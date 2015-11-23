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

import static org.apache.cxf.ws.addressing.JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.binding.soap.SoapHeader;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.headers.Header;
import org.apache.cxf.message.Message;
import org.apache.cxf.ws.addressing.AddressingProperties;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.ws.addressing.ObjectFactory;
import org.w3c.dom.Element;

/**
 * 
 * This class has methods in to handle Camel exchanges for inbound and outbound messages
 * and to assist with dealing with WS-Addressing to correlate requests and responses.
 * 
 */

public class MessageProcessor {
	
	private static final Log log = LogFactory.getLog( MessageProcessor.class );
	
	private static final ObjectFactory WSA_OBJECT_FACTORY =  new ObjectFactory();
	
	/**
	 * This method will extract a message ID from the Soap header and set that as a GUID to correlate requests and responses
	 * It also creates a header that can be used to make a file name from the Message ID that works on all platforms.
	 * 
	 * @param exchange
	 * @throws Exception
	 */
	
	public void processRequestPayload(Exchange exchange) throws Exception
	{
		HashMap<String, String> wsAddressingHeadersMap = returnWSAddressingHeadersFromCamelSoapHeaders(exchange);
		
		String requestID = wsAddressingHeadersMap.get("MessageID");	
		
		String replyTo = wsAddressingHeadersMap.get("ReplyTo");

		if (StringUtils.isNotBlank(replyTo))
		{
			exchange.getIn().setHeader("WSAddressingReplyToInbound", replyTo);
		}	
		
		if (StringUtils.isNotBlank(requestID))
		{
			String platformSafeFileName = requestID.replace(":", "");
			exchange.getIn().setHeader("federatedQueryRequestGUID", requestID);
			exchange.getIn().setHeader("platformSafeFileName", platformSafeFileName);
		}
		else
		{
			throw new Exception("Unable to find unique ID in Soap Header.  Was the message ID set in the Soap WS Addressing header?");
		}	

	}

	
	/**
	 * This method will use an existing exchange and set the 'out' message with the WS-Addressing message ID.  This removes all the headers from the 'in'
	 * message which tend to confuse Camel.
	 * 
	 * @param exchange
	 * @throws Exception
	 */
	public void prepareNewExchangeResponseMessage(Exchange exchange) throws Exception
	{
		String requestID = (String)exchange.getIn().getHeader("federatedQueryRequestGUID");
		log.debug("Federeated Query Request ID: " + requestID);

    	//Create a new map with WS Addressing message properties that we want to override
		HashMap<String, String> wsAddressingMessageProperties = new HashMap<String, String>();
		
		if (StringUtils.isNotEmpty(requestID))
		{	
			wsAddressingMessageProperties.put("MessageID",requestID);
		}

		//This is the reply to address that we want to provide to the service we are calling
		String replyToOutbound = (String)exchange.getIn().getHeader("WSAddressingReplyToOutbound");

		if (StringUtils.isNotEmpty(replyToOutbound))
		{	
			log.debug("WS Addressing Reply To Camel Header: " + replyToOutbound);
			wsAddressingMessageProperties.put("ReplyTo",replyToOutbound);
		}
			
		//Call method to create proper request context map
		Map<String, Object> requestContext = setWSAddressingProperties(wsAddressingMessageProperties);

		//This is the reply to address of the service that called us, set this as the actual address to call
		String replyToInbound = (String)exchange.getIn().getHeader("WSAddressingReplyToInbound");
		
		if (StringUtils.isNotEmpty(replyToInbound))
		{	
			requestContext.put(Message.ENDPOINT_ADDRESS, replyToInbound);
		}
			
		exchange.getOut().setHeader(Client.REQUEST_CONTEXT , requestContext);
			
		exchange.getOut().setBody(exchange.getIn().getBody());
	}

	/**
	 * This method returns a map with the following keys to get at WS-Addressing properties "MessageID", "ReplyTo", "From", "To"
	 * We can add to this method to return additional properties as they are needed.
	 * 
	 * @param exchange
	 * @return
	 */
	
	@SuppressWarnings("unchecked")
	public static HashMap<String, String> returnWSAddressingHeadersFromCamelSoapHeaders(Exchange exchange) {
		String messageID = null;
		String replyTo = null;
		String from = null;
		String to = null;
		
		HashMap<String, String> wsAddressingMessageProperties = new HashMap<String, String>();
		
		List<SoapHeader> soapHeaders = (List<SoapHeader>) exchange.getIn()
				.getHeader(Header.HEADER_LIST);

		for (SoapHeader soapHeader : soapHeaders) {
			log.debug("Soap Header: " + soapHeader.getName());
			log.debug("Soap Direction: " + soapHeader.getDirection());

			if (soapHeader.getName().toString()
					.equals("{http://www.w3.org/2005/08/addressing}MessageID")) {
				Element element = (Element) soapHeader.getObject();

				if (element != null) {
					messageID = element.getTextContent();
				}

				log.info("WS-Addressing Message ID: " + messageID);
				
				wsAddressingMessageProperties.put("MessageID", messageID);
			}

			if (soapHeader.getName().toString()
					.equals("{http://www.w3.org/2005/08/addressing}ReplyTo")) {
				Element element = (Element) soapHeader.getObject();

				if (element != null) {
					replyTo = element.getTextContent();
				}

				log.info("WS-Addressing ReplyTo: " + replyTo);
				wsAddressingMessageProperties.put("ReplyTo", replyTo);
			}

			if (soapHeader.getName().toString()
					.equals("{http://www.w3.org/2005/08/addressing}From")) {
				Element element = (Element) soapHeader.getObject();

				if (element != null) {
					from = element.getTextContent();
				}

				log.info("WS-Addressing From: " + from);
				wsAddressingMessageProperties.put("From", from);
			}

			if (soapHeader.getName().toString()
					.equals("{http://www.w3.org/2005/08/addressing}To")) {
				Element element = (Element) soapHeader.getObject();

				if (element != null) {
					to = element.getTextContent();
				}

				log.info("WS-Addressing To: " + to);
				wsAddressingMessageProperties.put("To", to);
			}

		
		}

		return wsAddressingMessageProperties;
	}

	/**
	 * This method will set the WS-Addressing Message Properties on the exchange prior to sending an outbound CXF message.
	 * It allows for 'MessageID' and 'ReplyTo'
	 * 
	 * @param senderExchange
	 * @param requestID
	 * @return
	 * @throws Exception
	 */
	
	public static Map<String, Object> setWSAddressingProperties(Map<String, String> wsAddressingMessageProperties) throws Exception {
		
		Map<String, Object> requestContext = null;
		
		if (!wsAddressingMessageProperties.isEmpty())
		{	
			// get Message Addressing Properties instance
	        AddressingProperties maps = new AddressingProperties();
	
	        String messageID = wsAddressingMessageProperties.get("MessageID");
	        
	        if (StringUtils.isNotEmpty(messageID))
	        {
		        // set MessageID property
		        AttributedURIType messageIDAttr =
		            WSA_OBJECT_FACTORY.createAttributedURIType();
		        messageIDAttr.setValue(messageID);
		        maps.setMessageID(messageIDAttr);
	        }
	        
	        String replyToString = wsAddressingMessageProperties.get("ReplyTo");

	        if (StringUtils.isNotEmpty(replyToString))
	        {
	        	AttributedURIType replyToAttr = new AttributedURIType(); 
	        	replyToAttr.setValue(replyToString); 
	        	
	        	EndpointReferenceType replyToRef = new EndpointReferenceType();
	        	replyToRef.setAddress(replyToAttr);

	        	maps.setReplyTo(replyToRef);
	        }

	        requestContext = new HashMap<String, Object>();
	        requestContext.put(CLIENT_ADDRESSING_PROPERTIES, maps);
	        
		}
		else
		{
			throw new Exception("WS-Addressing Message Properties can not be set.  Map is empty.");
		}	
		
		return requestContext;
	}
	
	/**
	 * This method will set the WS-Addressing Message ID on the exchange prior to sending an outbound CXF message.
	 * This method only allows for a MessageID for backwards compatibility.  Use 'setWSAddressingProperties' to see additional properties
	 * 
	 * @param senderExchange
	 * @param requestID
	 * @return
	 * @throws Exception
	 */
	
	public static Map<String, Object> setWSAddressingMessageID(String requestID) throws Exception {
		
		Map<String, Object> requestContext = null;
		
		if (StringUtils.isNotEmpty(requestID))
		{	
			// get Message Addressing Properties instance
	        AddressingProperties maps = new AddressingProperties();
	
	        // set MessageID property
	        AttributedURIType messageIDAttr =
	            WSA_OBJECT_FACTORY.createAttributedURIType();
	        messageIDAttr.setValue(requestID);
	        maps.setMessageID(messageIDAttr);
			
	        requestContext = new HashMap<String, Object>();
	        requestContext.put(CLIENT_ADDRESSING_PROPERTIES, maps);
	        
		}
		else
		{
			throw new Exception("WS-Addressing Message ID can not be set.  Request ID is empty.");
		}	
		
		return requestContext;
	}	

}
