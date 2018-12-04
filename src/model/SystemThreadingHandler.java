package model;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

public class SystemThreadingHandler {
	

/************************************************************
 * 
 * @author Shah Murad Hussain
 * @version 0.92
 * @since 30.08.2016
 * Accenture
 * This class allows system threads to be handler across classes
 ************************************************************/
	
	public SystemThreadingHandler(){
		
	}
	
	
	public static int findThreadCount(){
		
		int maximumThreads =1;
		Properties properties = new Properties();
		InputStream input = null;

		try {
			
			String filename = "system.properties";
			String filePath = "./config/";
			properties.load(new FileInputStream(filePath+filename));
			Enumeration<?> e = properties.propertyNames();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				String value = properties.getProperty(key);
				if(key.equalsIgnoreCase("MAX_THREADS")){
					maximumThreads = Integer.parseInt(value);
					LoggerModel.log.info("Setting Number of Threads to "+maximumThreads);
				}
				
				
			}

		} catch (IOException ex) {
			ex.printStackTrace();
			LoggerModel.log.debug("system.properties was file was not found in Config folder"+ "");
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
					LoggerModel.log.debug(e.getMessage(),e);
				}
			}
		}
		LoggerModel.log.debug("Set Number of Threads to "+maximumThreads);
		return maximumThreads;
		
	} 

}
