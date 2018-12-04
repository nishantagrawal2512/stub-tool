package model;

import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;
import org.apache.log4j.Logger;
import runme.WebServer;


/************************************************************
 * 
 * @author Shah Murad Hussain
 * @version 0.92
 * @since 30.08.2016
 * Accenture
 * Object used to handle xsd validation against the xsd file placed in the config/DCCAdapter_Request_Schema_v1_0.xsd
 ************************************************************/


public class XSDValidator {

	
	public static boolean validateRequestMessage(String xmlRequestFile) throws IOException{
		
		boolean xsdValidation = true;
		
		File schemaFile = new File("config/DCCAdapter_Request_Schema_v1_0.xsd");
		
		
		Source xmlFile = new StreamSource(new File(xmlRequestFile));
		
		try {
			javax.xml.validation.SchemaFactory schemaFactory = SchemaFactory
			    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(schemaFile);
			Validator validator = schema.newValidator();
		
		  validator.validate(xmlFile);
		  LoggerModel.log.debug("XSD validation passed: "+xmlFile.getSystemId());
		  
		} catch (SAXException e) {
		  xsdValidation=false;
		  LoggerModel.log.debug("XSD validation failed: "+xmlFile.getSystemId());
		  LoggerModel.log.debug("Reason: " + e.getLocalizedMessage());
		  
		  
		}
		//System.out.println("XSD Validation: "+xsdValidation);
		return xsdValidation;
		
	}
	
}
