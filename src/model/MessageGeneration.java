package model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class MessageGeneration {
	DCCRequestPOJO dccRequestPojo;
	boolean dccAlertFlag=false;
		
	public String createResponseMessage(String payLoadType,String origEUI,String targEui,
			
																String origCounter, String servReference
																,String servReferenceVariant,String user, String mpxn, String DeviceIDList, String DeviceID) throws IOException
		{
			String responseFileName = "";
			org.w3c.dom.Document responseDocument = null;
			//String IsAlertAfterDeviceAlert;
			
			if(payLoadType.equalsIgnoreCase("alertbefore")||payLoadType.equalsIgnoreCase("alertafter"))
		{
				if (targEui.equals(Config.getDCCOnlyFromConfigFile()) && !servReferenceVariant.equals("11.1"))
				{
					try
					{
						responseFileName=generateDCCAlert(targEui,origCounter,origEUI,mpxn,user,servReferenceVariant,payLoadType,DeviceIDList, DeviceID);
						return responseFileName;
					} 
					
					catch (SAXException | TransformerException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				else
				{
			
			try {
				//System.err.println("message generation ServiceReference "+servReference);
				
				LoggerModel.log.debug("Generating Alert messages");
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				
				// root elements
				responseDocument = docBuilder.newDocument();
				Element responseElement = responseDocument.createElement("Response");
				responseDocument.appendChild(responseElement);
				
				Attr xmlns1 = responseDocument.createAttribute("xmlns:sr");
				String xmlnsStringVal1 = "http://www.dccinterface.co.uk/ServiceUserGateway";
				xmlns1.setValue(xmlnsStringVal1);
				responseElement.setAttributeNodeNS(xmlns1);
				
				Attr xmlns2 = responseDocument.createAttribute("xmlns:ra");
				String xmlnsStringVal2 = "http://www.dccinterface.co.uk/ResponseAndAlert";
				xmlns2.setValue(xmlnsStringVal2);
				responseElement.setAttributeNodeNS(xmlns2);
				
				Attr xmlns3 = responseDocument.createAttribute("xmlns");
				String xmlnsStringVal3 = "http://www.cgi.com/DccAdapter/core/ServiceResponse";
				xmlns3.setValue(xmlnsStringVal3);
				responseElement.setAttributeNodeNS(xmlns3);

				Attr schemaVersion = responseDocument.createAttribute("schemaVersion");
				schemaVersion.setValue("1.0");
				responseElement.setAttributeNode(schemaVersion);
				
				Element header = responseDocument.createElement("Header");
				responseElement.appendChild(header);
				if(servReferenceVariant.equalsIgnoreCase("11.1"))
				{
					Element responseID = responseDocument.createElement("ResponseID");
					String responseText = DeviceIDList+":"+origEUI+":"+origCounter;
					responseID.appendChild(responseDocument.createTextNode(responseText));
					header.appendChild(responseID);
				}
				
				else
				{
					Element responseID = responseDocument.createElement("ResponseID");
					String responseText = targEui+":"+origEUI+":"+origCounter;
					responseID.appendChild(responseDocument.createTextNode(responseText));
					header.appendChild(responseID);
				}
				
				Element responseCode = responseDocument.createElement("ResponseCode");
				responseCode.appendChild(responseDocument.createTextNode(Config.getUserResponseCode(user, servReferenceVariant,targEui, mpxn, DeviceID)));
				//responseCode.appendChild(responseDocument.createTextNode(Config.getResponseCode()));
				header.appendChild(responseCode);

				// Timestamp format 2015-09-16T00:00:00
				String messageDateStamp = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
				String messageTimeStamp ="T"+ new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) + ".00Z";


				Element responseDateTime = responseDocument.createElement("ResponseDateTime");
				responseDateTime.appendChild(responseDocument.createTextNode(messageDateStamp+messageTimeStamp));
				header.appendChild(responseDateTime);
				
				Element body = responseDocument.createElement("Body");
				responseElement.appendChild(body);

				Element deviceAlertMessage = responseDocument.createElement("DeviceAlertMessage");
				//responseDateTime.appendChild(doc.createTextNode("test"));
				body.appendChild(deviceAlertMessage);
				
				
				Element alertCode = responseDocument.createElement("AlertCode");
				alertCode.appendChild(responseDocument.createTextNode(Config.getUserAlertCode(user, servReferenceVariant,targEui, mpxn, payLoadType,DeviceID)));
				deviceAlertMessage.appendChild(alertCode);
				
				
				Element gbcsPayload = responseDocument.createElement("GBCSPayload");
				deviceAlertMessage.appendChild(gbcsPayload);
				
				Element gbcsRawData = responseDocument.createElement("GBCSRawData");
				gbcsPayload.appendChild(gbcsRawData);
				
				Element gbcsResponse = responseDocument.createElement("GBCSResponse");
				gbcsPayload.appendChild(gbcsResponse);
				
				Element gbcsHeader = responseDocument.createElement("Header");
				gbcsResponse.appendChild(gbcsHeader);
				
				if(servReferenceVariant.equalsIgnoreCase("11.1"))
				{
					Element originID = responseDocument.createElement("BusinessOriginatorID");
					originID.appendChild(responseDocument.createTextNode(DeviceIDList));
					gbcsHeader.appendChild(originID);
				}
				
				else
				{
					Element originID = responseDocument.createElement("BusinessOriginatorID");
					originID.appendChild(responseDocument.createTextNode(targEui));
					gbcsHeader.appendChild(originID);
				}
				
				Element targetID = responseDocument.createElement("BusinessTargetID");
				targetID.appendChild(responseDocument.createTextNode(origEUI));
				gbcsHeader.appendChild(targetID);
				
				Element originCounter = responseDocument.createElement("OriginatorCounter");
				originCounter.appendChild(responseDocument.createTextNode(origCounter));
				gbcsHeader.appendChild(originCounter);
						
				Element GBCSHexadecimalMessageCode = responseDocument.createElement("GBCSHexadecimalMessageCode");
				GBCSHexadecimalMessageCode.appendChild(responseDocument.createTextNode(Config.getUserGBCSMessageCode(user, servReferenceVariant,targEui, mpxn, payLoadType, DeviceID)));
				gbcsHeader.appendChild(GBCSHexadecimalMessageCode);
				
				Element gbcsBody = responseDocument.createElement("Body");
				gbcsResponse.appendChild(gbcsBody);
				
				Element gbcsDeviceAlertMessage = responseDocument.createElement("DeviceAlertMessage");
				gbcsBody.appendChild(gbcsDeviceAlertMessage);
				XMLCRUDHandler xmlCrudHandler = new XMLCRUDHandler();
				
				String customPayLoad=xmlCrudHandler.readCustomXMLPayload(servReferenceVariant,payLoadType,user,targEui, mpxn, DeviceID);
				Node customNode = responseDocument.importNode(createNodeFromXMLString(customPayLoad,docFactory,docBuilder),true);
				//Node firstDocImportedNode;
				gbcsDeviceAlertMessage.appendChild(customNode);
				
				if(payLoadType.equalsIgnoreCase("alertbefore"))
					LoggerModel.log.debug("Alert Generated before response message");
				else if(payLoadType.equalsIgnoreCase("alertafter"))
					LoggerModel.log.debug("Alert Generated after response message");
				
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(responseDocument);
				String timeStamp= new SimpleDateFormat("EEEdMMMyyyyHHmmssSS").format(Calendar.getInstance().getTime());
				//format 2015-09-16T00:00:00
				
				//responseFileName = "./Users/"+user+"/response/"+timeStamp+".xml";
					 if (payLoadType.equalsIgnoreCase("alertbefore")){
						responseFileName = "./Users/"+user+"/alert/"+servReferenceVariant+"_ALERTBEFORE_"+timeStamp+".xml";
						LoggerModel.log.debug("The alert before file name is " + responseFileName);}
					else if (payLoadType.equalsIgnoreCase("alertafter")){
						responseFileName = "./Users/"+user+"/alert/"+servReferenceVariant+"_ALERTAFTER_"+timeStamp+".xml";
						LoggerModel.log.debug("The alert after file name is " + responseFileName);}
				
				
				File responsefile = new File(responseFileName);
				//LoggerModel.log.debug("------ File "+ file.getAbsolutePath());
				StreamResult result = new StreamResult(responsefile);
				transformer.transform(source, result);
				LoggerModel.log.debug("Writing XML response to response folder, Please view file "+responseFileName);
		}
				
			
			catch (IOException e) {
				LoggerModel.log.debug(e.getMessage(),e);
				e.printStackTrace();
			} catch (DOMException dom) {
				LoggerModel.log.debug(dom.getMessage(),dom);
				dom.printStackTrace();
			} catch (SAXException sax){
				LoggerModel.log.debug(sax.getMessage(),sax);
				sax.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}}
		}
		else if(payLoadType.equalsIgnoreCase("async")||payLoadType.equalsIgnoreCase("sync")||payLoadType.equalsIgnoreCase("Ack")){
		
		try {
			//System.err.println("message generation ServiceReference "+servReference);
			
			LoggerModel.log.debug("Generating XML response messages");
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			// root elements
			responseDocument = docBuilder.newDocument();
			Element responseElement = responseDocument.createElement("Response");
			responseDocument.appendChild(responseElement);

			Attr xmlns = responseDocument.createAttribute("xmlns");
			String xmlnsStringVal = "http://www.cgi.com/DccAdapter/core/ServiceResponse";
			xmlns.setValue(xmlnsStringVal);
			responseElement.setAttributeNode(xmlns);

			Attr schemaVersion = responseDocument.createAttribute("schemaVersion");
			schemaVersion.setValue("1.0");
			responseElement.setAttributeNode(schemaVersion);

			//formatting for header tag

			Element header = responseDocument.createElement("Header");
			responseElement.appendChild(header);
			
			/*if(servReferenceVariant.equals("6.15.1")||servReferenceVariant.equals("6.23")||servReferenceVariant.equals("6.21"))
			{			
				Element requestID = responseDocument.createElement("RequestID");
				String requestText =origEUI + ":" + targEui + ":"+ origCounter;
				requestID.appendChild(responseDocument.createTextNode(requestText));
				header.appendChild(requestID);
	
				Element responseID = responseDocument.createElement("ResponseID");
				String responseText = targEui+":"+origEUI+":"+origCounter;
				responseID.appendChild(responseDocument.createTextNode(responseText));
				header.appendChild(responseID);
				
				Element responseCode = responseDocument.createElement("ResponseCode");
				responseCode.appendChild(responseDocument.createTextNode(Config.getUserResponseCode(user, servReferenceVariant,targEui, mpxn)));
				//responseCode.appendChild(responseDocument.createTextNode(Config.getResponseCode()));
				header.appendChild(responseCode);
			
			}*/
			
			
				Element requestID = responseDocument.createElement("RequestID");
				String requestText =origEUI + ":" + targEui + ":"+ origCounter;
				requestID.appendChild(responseDocument.createTextNode(requestText));
				header.appendChild(requestID);

				Element responseID = responseDocument.createElement("ResponseID");
				String responseText = targEui+":"+origEUI+":"+origCounter;
				responseID.appendChild(responseDocument.createTextNode(responseText));
				header.appendChild(responseID);
				
				Element responseCode = responseDocument.createElement("ResponseCode");
				responseCode.appendChild(responseDocument.createTextNode(Config.getUserResponseCode(user, servReferenceVariant,targEui, mpxn, DeviceID)));
				//responseCode.appendChild(responseDocument.createTextNode(Config.getResponseCode()));
				header.appendChild(responseCode);
			


			// Timestamp format 2015-09-16T00:00:00
			String messageDateStamp = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
			String messageTimeStamp ="T"+ new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())+ ".00Z";


			Element responseDateTime = responseDocument.createElement("ResponseDateTime");
			responseDateTime.appendChild(responseDocument.createTextNode(messageDateStamp+messageTimeStamp));
			header.appendChild(responseDateTime);

			//formatting for body tag
			Element body = responseDocument.createElement("Body");
			responseElement.appendChild(body);

			Element responseMessage = responseDocument.createElement("ResponseMessage");
			//responseDateTime.appendChild(doc.createTextNode("test"));
			body.appendChild(responseMessage);
			
			if(!servReference.replaceAll("\\s","").equals("")){
				//System.err.println("entering serv ");
				Element serviceReferenceVariant = responseDocument.createElement("ServiceReference");
				serviceReferenceVariant.appendChild(responseDocument.createTextNode(servReference));
				responseMessage.appendChild(serviceReferenceVariant);
				
			}

			Element serviceReferenceVariant = responseDocument.createElement("ServiceReferenceVariant");
			serviceReferenceVariant.appendChild(responseDocument.createTextNode(servReferenceVariant));
			responseMessage.appendChild(serviceReferenceVariant);
			
			if(payLoadType.equalsIgnoreCase("sync")){
				payLoadType="Sync";
				
			}
			else if (payLoadType.equalsIgnoreCase("async")){
				payLoadType="Async";
			}
			else if (payLoadType.equalsIgnoreCase("ack")){
				payLoadType="Ack";}
			
				XMLCRUDHandler xmlCrudHandler = new XMLCRUDHandler();
			
				try {
					if (payLoadType.equalsIgnoreCase("sync")||payLoadType.equalsIgnoreCase("async"))
					{
						//enters if payload type is sync or async or alert before
						
						if(payLoadType.equalsIgnoreCase("async")){
							
							LoggerModel.log.debug("SRV number is:" + servReferenceVariant);
							
							if(servReferenceVariant.equalsIgnoreCase("D.1.1")){
								//D1_1MessageGeneration messageGeneration = new D1_1MessageGeneration();
								//MessageGeneration.d_1_1Message.messageGeneration(docFactory,docBuilder);
								
								LoggerModel.log.debug("Starting D.1.1 Async request workflow");
								
								String customPayLoad=xmlCrudHandler.readCustomXMLPayload(servReferenceVariant,payLoadType,user,targEui,mpxn,DeviceID);
								Node customNode = responseDocument.importNode(createNodeFromXMLString(customPayLoad,docFactory,docBuilder),true);
								responseMessage.appendChild(customNode);
							}
							else {
								if (payLoadType.equalsIgnoreCase("async"))
									LoggerModel.log.debug("Starting Async workflow");
															
								Element GBCSPayloadElement = responseDocument.createElement("GBCSPayload");
								//GBCSPayloadElement.appendChild(responseDocument.createTextNode(" "));
								responseMessage.appendChild(GBCSPayloadElement);
								
								Element GBCSRawData = responseDocument.createElement("GBCSRawData");
								GBCSRawData.appendChild(responseDocument.createTextNode(" "));
								GBCSPayloadElement.appendChild(GBCSRawData);
								
								Element GBCSResponseElement = responseDocument.createElement("GBCSResponse");
								GBCSPayloadElement.appendChild(GBCSResponseElement);
								
								Element GBCSResponseHeader = responseDocument.createElement("Header");
								
								GBCSResponseElement.appendChild(GBCSResponseHeader);
								/*
								if(servReferenceVariant.equals("6.15.1"))
								{
									Element BusinessOriginatorIDElement = responseDocument.createElement("BusinessOriginatorID");
									BusinessOriginatorIDElement.appendChild(responseDocument.createTextNode(origEUI));
									GBCSResponseHeader.appendChild(BusinessOriginatorIDElement);
									
									Element BusinessTargetIDElement = responseDocument.createElement("BusinessTargetID");
									BusinessTargetIDElement.appendChild(responseDocument.createTextNode(targEui));
									GBCSResponseHeader.appendChild(BusinessTargetIDElement);
								}
								
								else
								{*/
									Element BusinessOriginatorIDElement = responseDocument.createElement("BusinessOriginatorID");
									BusinessOriginatorIDElement.appendChild(responseDocument.createTextNode(targEui));
									GBCSResponseHeader.appendChild(BusinessOriginatorIDElement);
									
									Element BusinessTargetIDElement = responseDocument.createElement("BusinessTargetID");
									BusinessTargetIDElement.appendChild(responseDocument.createTextNode(origEUI));
									GBCSResponseHeader.appendChild(BusinessTargetIDElement);	
								//}
								
								Element originatorCounterElement = responseDocument.createElement("OriginatorCounter");
								originatorCounterElement.appendChild(responseDocument.createTextNode(origCounter));
								GBCSResponseHeader.appendChild(originatorCounterElement);
								
								Element GBCSHexadecimalMessageCode = responseDocument.createElement("GBCSHexadecimalMessageCode");
								GBCSHexadecimalMessageCode.appendChild(responseDocument.createTextNode("0000"));
								GBCSResponseHeader.appendChild(GBCSHexadecimalMessageCode);
								
								if(!servReference.replaceAll("\\s","").equals("")){
									Element serviceReferenceElement = responseDocument.createElement("ServiceReference");
									serviceReferenceElement.appendChild(responseDocument.createTextNode(servReference));
									GBCSResponseHeader.appendChild(serviceReferenceElement);
								}
									
								Element serviceReferenceVariantElement = responseDocument.createElement("ServiceReferenceVariant");
								serviceReferenceVariantElement.appendChild(responseDocument.createTextNode(servReferenceVariant));
								GBCSResponseHeader.appendChild(serviceReferenceVariantElement);
								
								Element bodyElement = responseDocument.createElement("Body");
								bodyElement.appendChild(responseDocument.createTextNode(" "));
								GBCSResponseElement.appendChild(bodyElement);
								
								String customPayLoad=xmlCrudHandler.readCustomXMLPayload(servReferenceVariant,payLoadType,user,targEui, mpxn,DeviceID);
								Node customNode = responseDocument.importNode(createNodeFromXMLString(customPayLoad,docFactory,docBuilder),true);
								//Node firstDocImportedNode;
								bodyElement.appendChild(customNode);
								
							if(payLoadType.equalsIgnoreCase("async"))
									LoggerModel.log.debug("Asynchronous Response message");
								}
							
						}
						else if (payLoadType.equalsIgnoreCase("sync")){
							String customPayLoad=xmlCrudHandler.readCustomXMLPayload(servReferenceVariant,payLoadType,user,targEui,mpxn,DeviceID);
							Node customNode = responseDocument.importNode(createNodeFromXMLString(customPayLoad,docFactory,docBuilder),true);
							//Node firstDocImportedNode;
							responseMessage.appendChild(customNode);
							LoggerModel.log.debug("Synchronous Response message");
						}
					}	
						
				} catch (IOException e) {
					LoggerModel.log.debug(e.getMessage(),e);
					e.printStackTrace();
				} catch (DOMException dom) {
					LoggerModel.log.debug(dom.getMessage(),dom);
					dom.printStackTrace();
				} catch (SAXException sax){
					LoggerModel.log.debug(sax.getMessage(),sax);
					sax.printStackTrace();
				}
			
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(responseDocument);
			String timeStamp= new SimpleDateFormat("EEEdMMMyyyyHHmmssSS").format(Calendar.getInstance().getTime());
			//format 2015-09-16T00:00:00
			
			//responseFileName = "./Users/"+user+"/response/"+timeStamp+".xml";
			if (targEui.equals(Config.getDCCOnlyFromConfigFile() ))
			{
				
				if(servReferenceVariant.equalsIgnoreCase("8.2"))
				{
					if(mpxn!="")
					{
						LoggerModel.log.debug("Enters DCC Only's 8.2 - MPXN folder and writing the response file");
						responseFileName = "./Users/DCCONLY/8.2/MPXN/"+mpxn+"/response/"+servReferenceVariant+"_RESP_"+timeStamp+".xml";
						LoggerModel.log.debug("User response file name is " + responseFileName);
					}
					
					else
					{
						LoggerModel.log.debug("Enters DCC Only's 8.2 - Device ID folder and writing the response file");
						responseFileName = "./Users/DCCONLY/8.2/DEVICEID/"+DeviceID+"/response/"+servReferenceVariant+"_RESP_"+timeStamp+".xml";
						LoggerModel.log.debug("User response file name is " + responseFileName);
					}
				}
				
				else
				{
					// enters here if the account matches the DCC ony prefix		
					LoggerModel.log.debug("Enters DCC Only and writing the response file");
					responseFileName = "./Users/DCCONLY/response/"+servReferenceVariant+"_RESP_"+timeStamp+".xml";
					LoggerModel.log.debug("User response file name is " + responseFileName);
				}
				
				//C:\Users\smit.j.mirani\workspace\HTTPServer\Users\DCCONLY\response
			}
			else
			{	
				if(payLoadType.equalsIgnoreCase("Ack"))
				{
					responseFileName = "./Users/"+user+"/response/"+servReferenceVariant+"_ACK_"+timeStamp+".xml";
					LoggerModel.log.debug("User ACK file name is " + responseFileName);
				}
				else if (payLoadType.equalsIgnoreCase("async")||payLoadType.equalsIgnoreCase("sync"))
				{
					responseFileName = "./Users/"+user+"/response/"+servReferenceVariant+"_RESP_"+timeStamp+".xml";
					LoggerModel.log.debug("User response file name is " + responseFileName);
				}
				//C:\Users\shah.m.hussain\workspace\HTTPServer\Users\AA\response
			}
			File responsefile = new File(responseFileName);
			//LoggerModel.log.debug("------ File "+ file.getAbsolutePath());
			StreamResult result = new StreamResult(responsefile);
			transformer.transform(source, result);
			LoggerModel.log.debug("Writing XML response to response folder, Please view file "+responseFileName);
				
		
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
			LoggerModel.log.debug(pce.getMessage(),pce);
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
			LoggerModel.log.debug(tfe.getMessage(),tfe);
		}}
		return responseFileName;
		}
	
	
	private String generateDCCAlert(String targEui,String origCounter,String origEUI,String mpxn, String user, String servReferenceVariant, String payLoadType, String DeviceIDList, String DeviceID) throws IOException, SAXException, TransformerConfigurationException, TransformerException 
	{
		
		LoggerModel.log.debug("Generating DCC Only Alert messages");
		String responseFileName="";
		/*String deviceIDList=dccRequestPojo.getDeviceIDList();
		if(servReferenceVariant.equalsIgnoreCase("11.1"))
		{
			targEui=deviceIDList;
		}*/
		LoggerModel.log.debug("Device ID List: "+DeviceIDList);
		XMLCRUDHandler xmlCrudHandler = new XMLCRUDHandler();
		org.w3c.dom.Document responseDocument = null;
		
		try
		{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			// root elements
			responseDocument = docBuilder.newDocument();
			Element responseElement = responseDocument.createElement("Response");
			responseDocument.appendChild(responseElement);
			
			Attr xmlns1 = responseDocument.createAttribute("xmlns:sr");
			String xmlnsStringVal1 = "http://www.dccinterface.co.uk/ServiceUserGateway";
			xmlns1.setValue(xmlnsStringVal1);
			responseElement.setAttributeNodeNS(xmlns1);
			
			Attr xmlns2 = responseDocument.createAttribute("xmlns:ra");
			String xmlnsStringVal2 = "http://www.dccinterface.co.uk/ResponseAndAlert";
			xmlns2.setValue(xmlnsStringVal2);
			responseElement.setAttributeNodeNS(xmlns2);
			
			Attr xmlns3 = responseDocument.createAttribute("xmlns");
			String xmlnsStringVal3 = "http://www.cgi.com/DccAdapter/core/ServiceResponse";
			xmlns3.setValue(xmlnsStringVal3);
			responseElement.setAttributeNodeNS(xmlns3);
	
			Attr schemaVersion = responseDocument.createAttribute("schemaVersion");
			schemaVersion.setValue("1.0");
			responseElement.setAttributeNode(schemaVersion);
			
			Element header = responseDocument.createElement("Header");
			responseElement.appendChild(header);
			
			if(servReferenceVariant.equalsIgnoreCase("11.1"))
			{
				Element responseID = responseDocument.createElement("ResponseID");
				String responseText = DeviceIDList+":"+origEUI+":"+origCounter;
				responseID.appendChild(responseDocument.createTextNode(responseText));
				header.appendChild(responseID);
			}
			else
			{
				Element responseID = responseDocument.createElement("ResponseID");
				String responseText = Config.getDCCOnlyFromConfigFile()+":"+origEUI+":"+origCounter;
				responseID.appendChild(responseDocument.createTextNode(responseText));
				header.appendChild(responseID);
			}
			
			Element responseCode = responseDocument.createElement("ResponseCode");
			responseCode.appendChild(responseDocument.createTextNode(Config.getUserResponseCode(user, servReferenceVariant,targEui, mpxn, DeviceID)));
			//responseCode.appendChild(responseDocument.createTextNode(Config.getResponseCode()));
			header.appendChild(responseCode);

			// Timestamp format 2015-09-16T00:00:00
			String messageDateStamp = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
			String messageTimeStamp ="T"+ new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())+ ".00Z";


			Element responseDateTime = responseDocument.createElement("ResponseDateTime");
			responseDateTime.appendChild(responseDocument.createTextNode(messageDateStamp+messageTimeStamp));
			header.appendChild(responseDateTime);
			
			Element body = responseDocument.createElement("Body");
			responseElement.appendChild(body);
			
			Element dccAlertMessage = responseDocument.createElement("DCCAlertMessage");
			//responseDateTime.appendChild(doc.createTextNode("test"));
			body.appendChild(dccAlertMessage);
			
			
			Element alertCode = responseDocument.createElement("DCCAlertCode");
			alertCode.appendChild(responseDocument.createTextNode(Config.getUserAlertCode(user, servReferenceVariant,targEui, mpxn, payLoadType,DeviceID)));
			dccAlertMessage.appendChild(alertCode);
			
			String customPayLoad=xmlCrudHandler.readCustomXMLPayload(servReferenceVariant,payLoadType,user,targEui, mpxn, DeviceID);
			Node customNode = responseDocument.importNode(createNodeFromXMLString(customPayLoad,docFactory,docBuilder),true);
			//Node firstDocImportedNode;
			dccAlertMessage.appendChild(customNode);
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(responseDocument);
			String timeStamp= new SimpleDateFormat("EEEdMMMyyyyHHmmssSS").format(Calendar.getInstance().getTime());
			
			if(servReferenceVariant.equalsIgnoreCase("8.2"))
			{
			// enters here if the account matches the 8.2 DCC Only Prefix or DCC Broker ID.
				if(mpxn!="")
				{
					LoggerModel.log.debug("Enters DCC Only's MPXN number "+mpxn+" and writing the alert file");
					if (payLoadType.equalsIgnoreCase("alertbefore"))
					{
						responseFileName  = "./Users/DCCONLY/8.2/MPXN/"+mpxn+"/alert/"+"8.2_ALERTBEFORE_"+timeStamp+".xml";
						LoggerModel.log.debug("The alert before file name is " + responseFileName);
					}
					else if (payLoadType.equalsIgnoreCase("alertafter"))
					{
						responseFileName = "./Users/DCCONLY/8.2/MPXN/"+mpxn+"/alert/"+"8.2_ALERTAFTER_"+timeStamp+".xml";;
						LoggerModel.log.debug("The alert after file name is " + responseFileName);
					}
					
				}
				
				else
				{
					LoggerModel.log.debug("Enters DCC Only's MPXN number "+DeviceID+" and writing the alert file");
					if (payLoadType.equalsIgnoreCase("alertbefore"))
					{
						responseFileName  = "./Users/DCCONLY/8.2/DEVICEID/"+DeviceID+"/alert/"+"8.2_ALERTBEFORE_"+timeStamp+".xml";
						LoggerModel.log.debug("The alert before file name is " + responseFileName);
					}
					else if (payLoadType.equalsIgnoreCase("alertafter"))
					{
						responseFileName = "./Users/DCCONLY/8.2/DEVICEID/"+DeviceID+"/alert/"+"8.2_ALERTAFTER_"+timeStamp+".xml";;
						LoggerModel.log.debug("The alert after file name is " + responseFileName);
					}
				}
			}
			
			else
			{
				LoggerModel.log.debug("Enters DCC Only's SRV number "+servReferenceVariant+" and writing the alert file");
				if (payLoadType.equalsIgnoreCase("alertbefore"))
				{
					responseFileName = "./Users/DCCONLY/alert/"+servReferenceVariant+"_ALERTBEFORE_"+timeStamp+".xml";
					LoggerModel.log.debug("The alert before file name is " + responseFileName);
				}
				else if (payLoadType.equalsIgnoreCase("alertafter"))
				{
					responseFileName = "./Users/DCCONLY/alert/"+servReferenceVariant+"_ALERTAFTER_"+timeStamp+".xml";
					LoggerModel.log.debug("The alert after file name is " + responseFileName);
				}
			}
			
			File responsefile = new File(responseFileName);
			//LoggerModel.log.debug("------ File "+ file.getAbsolutePath());
			StreamResult result = new StreamResult(responsefile);
			transformer.transform(source, result);
			LoggerModel.log.debug("Writing DCC Alert in DCCOnly/Alert folder, Please view file "+responseFileName);
			
		}
		
		catch (DOMException dom) {
			LoggerModel.log.debug(dom.getMessage(),dom);
			dom.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return responseFileName;

	}
	public Node createNodeFromXMLString(String xml,DocumentBuilderFactory dbf,DocumentBuilder db) throws SAXException,
    IOException, ParserConfigurationException {
		return db.parse(new ByteArrayInputStream(xml.getBytes()))
        .getDocumentElement();
	}
	
	public String generateErrorMessage(String payLoadType,String origEUI,String targEui,
			String origCounter, String servReference
			,String servReferenceVariant,String user, String DeviceID) {
		
		//returns the response file name 
		XMLCRUDHandler xmlCrudHandler = new XMLCRUDHandler();
		
		String responseFileName = "";
		org.w3c.dom.Document responseDocument = null;
		
		try {
		LoggerModel.log.debug("Generating XML error message");
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		
		// root elements
		responseDocument = docBuilder.newDocument();
		Element responseElement = responseDocument.createElement("Response");
		responseDocument.appendChild(responseElement);
		
		Attr xmlns = responseDocument.createAttribute("xmlns");
		String xmlnsStringVal = "http://www.cgi.com/DccAdapter/core/ServiceResponse";
		xmlns.setValue(xmlnsStringVal);
		responseElement.setAttributeNode(xmlns);
		
		Attr schemaVersion = responseDocument.createAttribute("schemaVersion");
		schemaVersion.setValue("1.0");
		responseElement.setAttributeNode(schemaVersion);
		
		//formatting for header tag
		
		Element header = responseDocument.createElement("Header");
		responseElement.appendChild(header);
		
		Element requestID = responseDocument.createElement("RequestID");
		String requestText =origEUI + ":" + targEui + ":"+ origCounter;
		requestID.appendChild(responseDocument.createTextNode(requestText));
		header.appendChild(requestID);
		
		Element responseID = responseDocument.createElement("ResponseID");
		String responseText = targEui+":"+origEUI+":"+origCounter;
		responseID.appendChild(responseDocument.createTextNode(responseText));
		header.appendChild(responseID);
		
		final String ERROR_CODE = "DE200";
		Element responseCode = responseDocument.createElement("ResponseCode");
		responseCode.appendChild(responseDocument.createTextNode(ERROR_CODE));
		header.appendChild(responseCode);
		
		
		// Timestamp format 2015-09-16T00:00:00
		String messageDateStamp = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
		String messageTimeStamp ="T"+ new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime())+ ".00Z";
		
		Element responseDateTime = responseDocument.createElement("ResponseDateTime");
		responseDateTime.appendChild(responseDocument.createTextNode(messageDateStamp+messageTimeStamp));
		header.appendChild(responseDateTime);
		
		//formatting for body tag
		Element body = responseDocument.createElement("Body");
		responseElement.appendChild(body);
		
		Element responseMessage = responseDocument.createElement("ResponseMessage");
		//responseDateTime.appendChild(doc.createTextNode("test"));
		body.appendChild(responseMessage);
		
		if(!servReference.replaceAll("\\s","").equals("")){
		//System.err.println("entering serv ");
		Element serviceReferenceVariant = responseDocument.createElement("ServiceReference");
		serviceReferenceVariant.appendChild(responseDocument.createTextNode(servReference));
		responseMessage.appendChild(serviceReferenceVariant);
		}
		
		Element serviceReferenceVariant = responseDocument.createElement("ServiceReferenceVariant");
		serviceReferenceVariant.appendChild(responseDocument.createTextNode(servReferenceVariant));
		responseMessage.appendChild(serviceReferenceVariant);
		
		if(payLoadType.equalsIgnoreCase("sync")){
			payLoadType="Sync";
		
		}
		else if (payLoadType.equalsIgnoreCase("async")){
			payLoadType="Async";
		}
		
		try {
		if (payLoadType.equalsIgnoreCase("Sync")||payLoadType.equalsIgnoreCase("async")){
		//enters if payload type is sync or async
		String mpxn = "not used";
		String customPayLoad=xmlCrudHandler.readCustomXMLPayload(servReferenceVariant,payLoadType,user,targEui,mpxn,DeviceID);
		Node customNode = responseDocument.importNode(createNodeFromXMLString(customPayLoad,docFactory,docBuilder),true);
		//Node firstDocImportedNode;
		responseMessage.appendChild(customNode);
		}	
		
		} catch (IOException e) {
		e.printStackTrace();
		System.out.println("IO Exception");
		LoggerModel.log.debug(e.getMessage(),e);
		} catch (DOMException dom) {
		dom.printStackTrace();
		LoggerModel.log.debug(dom.getMessage(),dom);
		} catch (SAXException sax){
		sax.printStackTrace();
		LoggerModel.log.debug(sax.getMessage(),sax);
		}
		
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(responseDocument);
		String timeStamp= new SimpleDateFormat("EEEdMMMyyyyHHmmssSS").format(Calendar.getInstance().getTime());
		//format 2015-09-16T00:00:00

		if (targEui.equals(Config.getDCCOnlyFromConfigFile()))
		{
			if(servReferenceVariant.equalsIgnoreCase("8.2"))
			{
				if(payLoadType.equalsIgnoreCase("sync")||payLoadType.equalsIgnoreCase("async"))
				{
					String mpxn = dccRequestPojo.getMpxn();
					LoggerModel.log.debug("Enters DCC Only and writing the error response file");
					responseFileName = "./Users/DCCONLY/8.2/MPXN/"+mpxn+"/response/"+servReferenceVariant+"_RESP_"+timeStamp+".xml";
					LoggerModel.log.debug("User response file name is " + responseFileName);
				}
				else if(payLoadType.equalsIgnoreCase("alertbefore")||payLoadType.equalsIgnoreCase("alertafter"))
				{
					String mpxn=dccRequestPojo.getMpxn();
					LoggerModel.log.debug("Enters DCC Only's MPXN "+mpxn+" folder and writing the response file");
					responseFileName = "./Users/DCCONLY/8.2/MPXN/"+mpxn+"/alert"+payLoadType+timeStamp+".xml";
					LoggerModel.log.debug("User alert file name is" + responseFileName);
				}
			}
			
			else 
			{
				// enters here if the account matches the DCC ony prefix
				if(payLoadType.equalsIgnoreCase("sync")||payLoadType.equalsIgnoreCase("async"))
				{
					LoggerModel.log.debug("Enters DCC Only and writing the error response file");
					responseFileName = "./Users/DCCONLY/response/"+servReferenceVariant+"_RESP_"+timeStamp+".xml";
					LoggerModel.log.debug("User response file name is " + responseFileName);
				}
				else if(payLoadType.equalsIgnoreCase("alertbefore")||payLoadType.equalsIgnoreCase("alertafter"))
				{
					String mpxn=dccRequestPojo.getMpxn();
					LoggerModel.log.debug("Enters DCC Only's MPXN "+mpxn+"folder and writing the response file");
					responseFileName = "./Users/DCCONLY/alert"+payLoadType+"_"+timeStamp+".xml";
					LoggerModel.log.debug("User alert file name is" + responseFileName);
				}
				//C:\Users\smit.j.mirani\workspace\HTTPServer\Users\DCCONLY\response	
			}
			
			
		}
		else
		{	
			if(payLoadType.equalsIgnoreCase("ack"))
			{
				if(servReferenceVariant.equalsIgnoreCase("8.2"))
				{
					String mpxn = dccRequestPojo.getMpxn();
					responseFileName = "./Users/DCCONLY/8.2/MPXN/"+mpxn+"/response/"+servReferenceVariant+"_ACK_"+timeStamp+".xml";
					LoggerModel.log.debug("User ACK file name is " + responseFileName);
				}
				else
				{
					responseFileName = "./Users/DCCONLY/response/"+servReferenceVariant+"_ACK_"+timeStamp+".xml";
					LoggerModel.log.debug("User ACK file name is " + responseFileName);
				}
			}
			
			else
			{
				responseFileName = "./Users/"+user+"/response/"+servReferenceVariant+"_resp_"+timeStamp+".xml";
			}
		}
		StreamResult result = new StreamResult(new File(responseFileName));
		transformer.transform(source, result);
		LoggerModel.log.debug("Set response code as "+Config.getResponseCode());
		LoggerModel.log.debug("Writing XML response to response folder, Please view file "+timeStamp);
		//System.out.println("Done");
		
		
		} catch (ParserConfigurationException pce) {
		pce.printStackTrace();
		LoggerModel.log.debug(pce.getMessage(),pce);
		} catch (TransformerException tfe) {
		tfe.printStackTrace();
		LoggerModel.log.debug(tfe.getMessage(),tfe);
		}
		
		return responseFileName;		
		}	
}
