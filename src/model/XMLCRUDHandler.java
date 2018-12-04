package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLCRUDHandler {
	
	/************************************************************
	 * 
	 * @author Shah Murad Hussain
	 * @version 0.100
	 * @since 09.09.2016
	 * Accenture
	 * Class created for logical seperation of stub code
	 * 
	 * @author Smit Mirani
	 * @version 106.1
	 * @since 06.04.2017
	 * Added exception clause for 8.2 request response, and alerts path.
	 * Proper renaming of response files + Ack file seperate generation + alerts files.
	 * 
	 * @author Smit Mirani
	 * @version 107.5
	 * @since 04.05.2017
	 * STUB will pick up either MPxN or DeviceID value from 8.2 request
	 * 
	 ************************************************************/
	String MPXN = "100";
	public synchronized String writeRequestToXMLFile(String content, String servReferenceVariant){
		DCCRequestPOJO requestPOJO = new DCCRequestPOJO();
		//saves xml to file, decoupling this will stop xml generation
		
		try {
			String timeStamp= new SimpleDateFormat("EEEdMMMyyyyHHmmssSS").format(Calendar.getInstance().getTime());
			servReferenceVariant = requestPOJO.getServiceReferenceVariant();
			//System.out.println("Service reference variant is:" +servReferenceVariant);
			String requestFileName ="./request/"+servReferenceVariant+"_REQ_"+timeStamp+".xml";
			File requestFile = new File(requestFileName);
			//System.err.println("request file name "+requestFileName );
			LoggerModel.log.debug("Writing XML request on server, please look for the request file at "+ requestFile.getPath());
			
			if (!requestFile.exists()) {
				requestFile.createNewFile();
			}

			FileWriter fileWriter = new FileWriter(requestFile.getAbsoluteFile());
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(content);
			bufferedWriter.close();
			
			return requestFileName;

		} catch (IOException e) {
			LoggerModel.log.debug(e.getMessage(),e);
		}
		return "Error";
	}
	
	public synchronized void writeRequestToXMLUserFile(String targetEui,String requestFileName,String userProfile,String content,String servReferenceVariant, String mpxn, String DeviceIDList, String DeviceID){
		
		try {
			
			String timeStamp= new SimpleDateFormat("EEEdMMMyyyyHHmmssSS").format(Calendar.getInstance().getTime());
			LoggerModel.log.debug("Target EUI "+targetEui);
			String DCCOnlyTargetEUIValue =Config.getDCCOnlyFromConfigFile();
			if (targetEui.equalsIgnoreCase(DCCOnlyTargetEUIValue))
			{
				if(servReferenceVariant.equalsIgnoreCase("8.2"))
				{
					if(!mpxn.equalsIgnoreCase(""))
					{
						LoggerModel.log.debug("entering DCC only's 8.2 folder with MPxN");
						requestFileName ="./Users/DCCONLY/8.2/MPXN/"+mpxn+"/request/"+servReferenceVariant+"_REQ_"+timeStamp+".xml";
						LoggerModel.log.debug("User request file name is " + requestFileName);
					}
					
					else
					{
						LoggerModel.log.debug("entering DCC only's 8.2 folder with DeviceID");
						requestFileName ="./Users/DCCONLY/8.2/DEVICEID/"+DeviceID+"/request/"+servReferenceVariant+"_REQ_"+timeStamp+".xml";
						LoggerModel.log.debug("User request file name is " + requestFileName);
					}
					
				}
				
				else
				{
					// check if targetEUI is DCC Only
					LoggerModel.log.debug("entering DCC only folder");
					requestFileName ="./Users/DCCONLY/request/"+servReferenceVariant+"_REQ_"+timeStamp+".xml";
					LoggerModel.log.debug("User request file name is " + requestFileName);	
				}
			}
			else
			{	
				requestFileName ="./Users/"+userProfile+"/request/"+servReferenceVariant+"_REQ_"+timeStamp+".xml";
				//LoggerModel.log.debug("User request file name is " + requestFileName);				
			}
			File requestFile = new File(requestFileName);
			
			LoggerModel.log.debug("User selected: "+userProfile);
			LoggerModel.log.debug("Request file placed at: "+ requestFile.getPath());
			
			if (!requestFile.exists()) {
				requestFile.createNewFile();
			}

			FileWriter fileWriter = new FileWriter(requestFile.getAbsoluteFile());
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(content);
			bufferedWriter.close();

		} catch (IOException e) {
			LoggerModel.log.debug("This is where I am failing");
			LoggerModel.log.debug(e.getMessage(),e);
		}
		
		
		
	}



	public synchronized DCCRequestPOJO readXMLRequestValues(String requestFileName,DCCRequestPOJO dccRequestPojo){
		/*System.err.println("Request file name "+requestFileName+ " originatorEui "+originatorEui+ " targetEui "+targetEui +" "
		+" originatorCounter "+originatorCounter+ " "
		+ " serviceReference "+serviceReference
		+serviceReferenceVariant + "serviceReferenceVariant"
		+" userProfile "+userProfile+" "+
		"REQUEST_NAMESPACE_PREFIX"+REQUEST_NAMESPACE_PREFIX+ " requestData "+
		requestData+"");
		*/
		//System.err.println("requestFileName................."+requestFileName);
		
		LoggerModel.log.debug("Reading XML Values from request file");
		File requestFile = new File(requestFileName);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware(true);

		try{
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			org.w3c.dom.Document doc = dBuilder.parse(requestFile);
			
			doc.getDocumentElement().normalize();
			
			//System.out.println("Root element : " + doc.getDocumentElement().getNodeName());
			//NodeList nList = doc.getElementsByTagNameNS("*","Header");
			
			//define namespace prefix
			
			String rootElementValue = doc.getDocumentElement().getNodeName();
			if (rootElementValue.contains(":")){
				dccRequestPojo.setREQUEST_NAMESPACE_PREFIX(rootElementValue.substring(0, rootElementValue.indexOf(":")+1));
				LoggerModel.log.debug("Setting request namespace prefix to " +dccRequestPojo.getREQUEST_NAMESPACE_PREFIX());
				
			}else{
				LoggerModel.log.debug("Setting namespace prefix to blank");
				dccRequestPojo.setREQUEST_NAMESPACE_PREFIX("");
			}
			
			LoggerModel.log.debug("Request namespace prefix was set to " +dccRequestPojo.getREQUEST_NAMESPACE_PREFIX());
			
			
			NodeList nList = doc.getDocumentElement().getElementsByTagName(dccRequestPojo.getREQUEST_NAMESPACE_PREFIX()+"Header");
			//System.out.println("Node List Length "+nList.getLength());
			for (int i = 0; i < nList.getLength(); i++)
			{

				Node nNode = nList.item(i);
				
				Element eElement = (Element) nNode;
				

				dccRequestPojo.setOriginatorEui(eElement.getElementsByTagName(dccRequestPojo.getREQUEST_NAMESPACE_PREFIX()+"OriginatorEui").item(0).getTextContent());
				dccRequestPojo.setTargetEui(eElement.getElementsByTagName(dccRequestPojo.getREQUEST_NAMESPACE_PREFIX()+"TargetEui").item(0).getTextContent());
				
				//extract first two characters from targetEUI to create a user profile
						
				//System.out.println("User profile "+userProfile);
				dccRequestPojo.setOriginatorCounter (eElement.getElementsByTagName(dccRequestPojo.getREQUEST_NAMESPACE_PREFIX()+"OriginatorCounter").item(0).getTextContent());
				
				
				//If statement checks if ServiceReference exists
				if(eElement.getElementsByTagName(dccRequestPojo.getREQUEST_NAMESPACE_PREFIX()+"ServiceReference").getLength()>0)
				{
					//System.err.println(eElement.getElementsByTagName(REQUEST_NAMESPACE_PREFIX+"ServiceReference").item(0).getTextContent());
					dccRequestPojo.setServiceReference (eElement.getElementsByTagName(dccRequestPojo.getREQUEST_NAMESPACE_PREFIX()+"ServiceReference").item(0).getTextContent());
				}
				
				dccRequestPojo.setServiceReferenceVariant(eElement.getElementsByTagName(dccRequestPojo.getREQUEST_NAMESPACE_PREFIX()+"ServiceReferenceVariant").item(0).getTextContent());
				 if (dccRequestPojo.getServiceReferenceVariant().equals("8.2"))
			        {
			          LoggerModel.log.debug("Finding 8.2 MPXN Value");
			          NodeList nBodyList = doc.getDocumentElement().getElementsByTagName(dccRequestPojo.getREQUEST_NAMESPACE_PREFIX() + "Body");
			          for (int j = 0; j < nBodyList.getLength(); j++)
			          {
			            Node nNodeBodyEl = nBodyList.item(j);
			            
			            Element eBodyElement = (Element)nNodeBodyEl;
			            
			            NodeList nl = doc.getDocumentElement().getElementsByTagName(dccRequestPojo.getREQUEST_NAMESPACE_PREFIX() + "MPxN");
			            for (int k = 0; k < nBodyList.getLength(); k++)
			            {
				            if (nl.getLength() > 0)
				            {
				            	dccRequestPojo.setMpxn(eBodyElement.getElementsByTagName(dccRequestPojo.getREQUEST_NAMESPACE_PREFIX() + "MPxN").item(0).getTextContent());
				            }
			            }
			            
			            NodeList n2 = doc.getDocumentElement().getElementsByTagName(dccRequestPojo.getREQUEST_NAMESPACE_PREFIX() + "DeviceID");
			            for (int k = 0; k < nBodyList.getLength(); k++)
			            {
			            
			            if (n2.getLength() > 0) {
			            	dccRequestPojo.setDeviceID(eBodyElement.getElementsByTagName(dccRequestPojo.getREQUEST_NAMESPACE_PREFIX() + "DeviceID").item(0).getTextContent());
			            }
			            }
			            
			            			            
			          }
			        }
				 
				 else if (dccRequestPojo.getServiceReferenceVariant().equals("11.1"))
			        {
			          LoggerModel.log.debug("Finding 11.1 DeviceIDList");
			          NodeList nBodyList1 = doc.getDocumentElement().getElementsByTagName(dccRequestPojo.getREQUEST_NAMESPACE_PREFIX() + "Body");
			          for (int k = 0; k < nBodyList1.getLength(); k++)
			          {
			            Node nNodeBodyE2 = nBodyList1.item(k);
			            
			            Element eBodyElement = (Element)nNodeBodyE2;
			            dccRequestPojo.setDeviceIDList(eBodyElement.getElementsByTagName(dccRequestPojo.getREQUEST_NAMESPACE_PREFIX() + "DeviceIDList").item(0).getTextContent());
			            LoggerModel.log.debug("DeviceIDList is "+ dccRequestPojo.getDeviceIDList());
			          }
			        }
				 
				 else if (dccRequestPojo.getServiceReferenceVariant().equals("8.3"))
			        {
			          LoggerModel.log.debug("Finding 8.3 DeviceID");
			          NodeList nBodyList1 = doc.getDocumentElement().getElementsByTagName(dccRequestPojo.getREQUEST_NAMESPACE_PREFIX() + "Body");
			          for (int k = 0; k < nBodyList1.getLength(); k++)
			          {
			            Node nNodeBodyE2 = nBodyList1.item(k);
			            
			            Element eBodyElement = (Element)nNodeBodyE2;
			            dccRequestPojo.setDeviceID(eBodyElement.getElementsByTagName(dccRequestPojo.getREQUEST_NAMESPACE_PREFIX() + "DeviceID").item(0).getTextContent());
			            LoggerModel.log.debug("DeviceID is "+ dccRequestPojo.getDeviceIDList());
			          }
			        }
				 if(dccRequestPojo.getServiceReferenceVariant().equalsIgnoreCase("11.1"))
				 {
					 dccRequestPojo.setUserProfile(dccRequestPojo.getDeviceIDList().substring(0, 2));
				 }
				 else
				 {
					 dccRequestPojo.setUserProfile(dccRequestPojo.getTargetEui().substring(0, 2));
				 }
				 
			}
		}
		catch (ParserConfigurationException pce) {
			pce.printStackTrace();
			LoggerModel.log.debug(pce.getMessage(),pce);
		} 
		catch (IOException ie){
			ie.printStackTrace();
			LoggerModel.log.debug(ie.getMessage(),ie);
		}
		catch (SAXException sax){
			LoggerModel.log.debug("SAX Exception in readXMLRequestValues");
			LoggerModel.log.debug(sax.getMessage(),sax);
			
		}
		catch (Exception e)
	    {
	      LoggerModel.log.debug(e.getMessage(), e);
	    }
		return dccRequestPojo;
		
	}
	
	public String readCustomXMLPayload(String servReferenceVariant,String messageType,String userProfile,String targetEUI, String mpxn, String DeviceID) throws IOException{
		final String DCCONLYTARGETEUI= Config.getDCCOnly();
		//create readXMLmethod (to read xml) pass servicevariant, sync/async as parameters
		
		if(messageType.replaceAll("\\s","").equalsIgnoreCase("")){
			LoggerModel.log.debug("No File found no custom payload used in" +System.getProperty("user.dir"));
			return "";
		}
		
		String xmlPayload;
		String fileName=servReferenceVariant+"_"+messageType+".txt";
		String fileDirectory="";
		
		if(targetEUI.equalsIgnoreCase(DCCONLYTARGETEUI) && !servReferenceVariant.equalsIgnoreCase("11.1"))
		{
			 if (servReferenceVariant.equalsIgnoreCase("8.2"))
		      {
				 if(mpxn!="")
				 {
					LoggerModel.log.debug("Reading from Users/DCCONLY/8.2/MPXN");
			        LoggerModel.log.debug("MPXN Value has been set to " + mpxn);
			        fileDirectory = "./Users/DCCONLY/8.2/MPXN/"+mpxn+"/"+ fileName;
				 }
				 
				 else
				 {
					 LoggerModel.log.debug("Reading from Users/DCCONLY/8.2/DEVICEID");
				     LoggerModel.log.debug("DeviceID Value has been set to " + DeviceID);
				     fileDirectory = "./Users/DCCONLY/8.2/DEVICEID/"+DeviceID+"/"+ fileName;
				 }
		      }
		      else
		      {
		        LoggerModel.log.debug("Entering DCC Only folder");
		        fileDirectory = "./Users/DCCONLY/" + fileName;
		      }
		}
		else{
			LoggerModel.log.debug("Entering "+userProfile+"'s user account, and reading the file " +fileName);
			fileDirectory="./Users/"+userProfile+"/"+fileName;
		}
		
		
		LoggerModel.log.debug("Reading from "+fileDirectory);
		LoggerModel.log.debug(" ");
		BufferedReader bufferedReader;
		bufferedReader = new BufferedReader(new FileReader(fileDirectory));
		
		
		try {
		    StringBuilder stringBuilder = new StringBuilder();
		    String line = bufferedReader.readLine();

		    while (line != null) {
		        stringBuilder.append(line);
		        stringBuilder.append(System.lineSeparator());
		        line = bufferedReader.readLine();
		    }
		    
		    //saves xml payload in string
		    xmlPayload = stringBuilder.toString();
		    
		    LoggerModel.log.debug("Custom XML has been set to "+xmlPayload);
		    return xmlPayload;
		} 
		
		catch (IOException e) {
			// TODO Auto-generated catch block
			LoggerModel.log.debug("The file "+fileName+" was not found at the desired location.");
		} 

		
		finally {
		    bufferedReader.close();
		}
		return "";
	}
}
