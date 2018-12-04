package model;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
//import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;


/************************************************************
 * 
 * @author Shah Murad Hussain
 * @version 0.100
 * @since 09.09.2016
 * Accenture
 * 
 * 
 * @author Smit Mirani
 * @version 106.1
 * @since 06.04.2017
 * This class is used to get both information of config.properties and system  
 * Added exception clause for sending response and requests to the 'MPxN' folder
 * for DCCOnly requests
 * 
 * Added new configuration for personalized AlertBefore and AlertAfter Delays
 ************************************************************/

public class Config {
	
	private static String responseCode="IO"; 
	private static int asyncDelay=1;
	private static int alertBeforeDelay=1;
	private static int alertAfterDelay=1;
	private static String DCCONLY="";
	private static String mdmURL="";
	private static String loadBalancerURL="";
	private static String isAlertBeforeDeviceAlert="";
	private static String isAlertAfterDeviceAlert="";

	private static ConcurrentHashMap <String,String> configPropertiesList = new ConcurrentHashMap<String,String>();
	
	public Config(){
		
	}
	
	public static void printConfigPropertiesList(){
	
		for (Map.Entry<String, String> entry: configPropertiesList.entrySet()){
			System.out.println("Key is "+ entry.getKey() + " Value is : "+ entry.getValue());
		}
	}
	
	
	public static void storeConfigListInMemory()
	{
		
		Properties properties = new Properties();
		InputStream input = null;

		try {
			
			String filename = "config.properties";
			String filePath = "./config/";
			properties.load(new FileInputStream(filePath+filename));
			Enumeration<?> e = properties.propertyNames();
			
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				String value = properties.getProperty(key);
				configPropertiesList.put(key, value);
				
			}

		} catch (IOException ex) {
			ex.printStackTrace();
			LoggerModel.log.debug(ex.getMessage(),ex);
		} 
		finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
					LoggerModel.log.debug(e.getMessage(),e);
				}
			}
		}
		
		//printConfigPropertiesList();
		LoggerModel.log.debug("Saving config.properties information in memory");
		LoggerModel.log.debug("Note - When updating config.properties file, remember to restart the server");
		
		
	} 
	public static boolean checkLoadBalancerURLExists (String loadBalancerURL){
		boolean loadBalancerExists =false;
		if (Config.getLoadBalancerURL().equalsIgnoreCase("loadBalancerURL was not set in config.properties")){
			loadBalancerExists=false;
		}
		else{
			loadBalancerExists=true;
		}
		return loadBalancerExists;
		
	}
	
	public static boolean checkLoadBalancerURLExists (){
		boolean loadBalancerExists =false;
		if (Config.getLoadBalancerURL().equalsIgnoreCase("loadBalancerURL was not set in config.properties")){
			loadBalancerExists=false;
		}
		else{
			loadBalancerExists=true;
		}
		return loadBalancerExists;
		
	}
	
	
	public static String getLoadBalancerURL(){
		String url ="loadBalancerURL was not set in config.properties";
		
		for (Map.Entry<String, String>entry: configPropertiesList.entrySet()){
			
			if (entry.getKey().equalsIgnoreCase("loadBalancerURL")){
				url = entry.getValue();
				Config.setLoadBalancerURL(url);
				LoggerModel.log.debug("Setting Loadbalancer URL");
			}
			
			
		}
		return url;
	}
	
	public static void setLoadBalancerURL(String lURL){
		Config.loadBalancerURL = lURL;
		
	}
	
	
	public static int findUsersPortNumber(){
		/*This method find the user Port number
		 * AND sets the DCC Only user profile
		 */
		
		int portNumber=8080;
		//System.out.println("entering find user port number ");
		for (Map.Entry<String, String> entry: configPropertiesList.entrySet()){
			
			if (entry.getKey().equalsIgnoreCase("portNumber")){
				portNumber = Integer.parseInt(entry.getValue());
				LoggerModel.log.debug("Port number was set to "+portNumber);
				
			}
			//set DCC only variable
			if(entry.getKey().equalsIgnoreCase("DCCBrokerID")){
				Config.setDCCOnly(entry.getValue());
			}
			
		}
		return portNumber;
	}
	
	
	public static ConcurrentHashMap<String, String> findServerVariantsConfig(String serviceVariant){
		
		ConcurrentHashMap<String,String> settingsList = new ConcurrentHashMap<String, String>();
				
		for(Map.Entry<String, String> entry: configPropertiesList.entrySet()){
			if(serviceVariant.equals(findServiceVariant(entry.getKey()))){
				settingsList.put(findMessageType(entry.getKey()),entry.getValue().replaceAll("\\s",""));	
				
			}
			//Sets asynchronous delay
			if(entry.getKey().equalsIgnoreCase("asyncDelay")){
				Config.setAsyncDelay(Integer.parseInt(entry.getValue()));
			}
			
			if(entry.getKey().equalsIgnoreCase("alertBeforeDelay")){
				Config.setAlertBeforeDelay(Integer.parseInt(entry.getValue()));
			}
			
			if(entry.getKey().equalsIgnoreCase("alertAfterDelay")){
				Config.setAlertAfterDelay(Integer.parseInt(entry.getValue()));
			}
			
			
		}
		return settingsList;
			
	}
	
	
	
	private static String findServiceVariant(String key){
		//gets the value before the underscore e.g. 8.2_async will return 8.2
		String serviceVariant="";
		for (String variant: key.split("_")){
	        serviceVariant = variant;
	        break;
	    }
		return serviceVariant;
	}
	
	private static String findMessageType(String key){
		//get the value after the underscore e.g. 8.2_async will return async
		String messageType ="";
		int num=0;
		for(String variant:key.split("_")){
			if(num==1){
				messageType=variant;
				break;
			}
			else{
			  num++;
			}
			
			
		}
		return messageType;
		
	}
	
	public static String getResponseCode(){
		return Config.responseCode;
	}
	
	public static void setResponseCode(String config){
		Config.responseCode = config;
	}
		public static int getAsyncDelay() {
		LoggerModel.log.debug("Pausing the asynchronous workflow for "+Config.asyncDelay+ " seconds");
		return Config.asyncDelay;
	}

	public static void setAsyncDelay(int asyncDelay) {
		Config.asyncDelay = asyncDelay;
	}
	
	public static int getAlertBeforeDelay() {
		LoggerModel.log.debug("Pausing the Alert Before for "+Config.alertBeforeDelay+ " seconds");
		return Config.alertBeforeDelay;
	}

	public static void setAlertBeforeDelay(int alertBeforeDelay) {
		Config.alertBeforeDelay = alertBeforeDelay;
	}
	
	public static int getAlertAfterDelay() {
		LoggerModel.log.debug("Pausing the Alert After for "+Config.alertAfterDelay+ " seconds");
		return Config.alertAfterDelay;
	}

	public static void setAlertAfterDelay(int alertAfterDelay) {
		Config.alertAfterDelay = alertAfterDelay;
	}
	
	public static String getDCCOnly(){
		return Config.DCCONLY;
	}
	
	public static String getGuid(){
		String temp = Config.DCCONLY.substring(0,1);
		//LoggerModel.log.debug("DCCONLY SUBSTRING VALUE IS :"+temp);
		return temp;
	}
	
	public static void setDCCOnly(String dccOnly){
		Config.DCCONLY=dccOnly;
	}
	
	public static String getMDMuserName(){
		
		String username="";
		for (Map.Entry<String,String> entry: configPropertiesList.entrySet()){
			if (entry.getKey().equalsIgnoreCase("mdmUsername")){
				username = entry.getValue();
				break;
			}
			
		} 
		return username;
		
	}
	
	public static String getMdmURL() {
		
		for (Map.Entry<String, String> entry : configPropertiesList.entrySet()){
			if (entry.getKey().equals("mdmURL")){
				Config.mdmURL = entry.getValue();
				break;
			}
			
		}
		return Config.mdmURL; 

	}

	

	public static String getMDMpassword(){
		String password="";
		for (Map.Entry<String, String> entry: configPropertiesList.entrySet()){
			if (entry.getKey().equalsIgnoreCase("mdmPassword")){
				password = entry.getValue();
				break;
			}
			
		}
		
		return password;
		
		
	}
	
	public static String getUserResponseCode(String user,String usrServiceVariant,String targetEUI, String mpxn,String DeviceID ){
		Properties prop = new Properties();
		InputStream input = null;
		String responseCode = "I0";
		
		try {
			
			String filename = "response_code.properties";
			String filePath;
			//System.err.println(user + Config.getDCCOnly().substring(0, 2));
			//System.out.println("file location" +   System.getProperty("user.dir"));
			if (targetEUI.equals(Config.getDCCOnly()))
			{
				if(usrServiceVariant.equalsIgnoreCase("8.2"))
				{
					if(mpxn!="")
					{
					LoggerModel.log.debug("Observing DCC only's 8.2 config.properties file");
					filePath = "./Users/DCCONLY/8.2/MPXN/"+mpxn+"/";
					}
					
					else
					{
						LoggerModel.log.debug("Observing DCC only's 8.2 config.properties file");
						filePath = "./Users/DCCONLY/8.2/DEVICEID/"+DeviceID+"/";
					}
				}
				
				else
				{
				LoggerModel.log.debug("Observing DCC only's config.properties file");
				filePath = "./Users/DCCONLY/";
				}
			}
			
			else
			{
				filePath = "./Users/"+user+"/";	
				LoggerModel.log.debug("Reading "+user+ "s response_code.properties file");
			}
			
			prop.load(new FileInputStream(filePath+filename));
			Enumeration<?> e = prop.propertyNames();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				String value = prop.getProperty(key);
				if(key.equalsIgnoreCase(usrServiceVariant)){
					responseCode = value;
					
				}
				
			}

		} catch (IOException ex) {
			LoggerModel.log.debug("File not found for user directory");
			LoggerModel.log.debug("File not found for user directory setting response code to IO");
			responseCode="I0";
			ex.printStackTrace();
			LoggerModel.log.debug(ex.getMessage(),ex);
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
		
		return responseCode;
		
	}
	
	public static String getUserAlertCode(String user,String usrServiceVariant,String targetEUI, String mpxn, String payLoadType,String DeviceID ){
		Properties prop = new Properties();
		InputStream input = null;
		String alertCode = "8F66";
		
		try {
			
			String filename = "response_code.properties";
			String filePath;
			//System.err.println(user + Config.getDCCOnly().substring(0, 2));
			//System.out.println("file location" +   System.getProperty("user.dir"));
			if (targetEUI.equals(Config.getDCCOnly()) && !usrServiceVariant.equalsIgnoreCase("11.1"))
			{
				if(usrServiceVariant.equalsIgnoreCase("8.2") )
				{
					if(mpxn!="")
					{
					LoggerModel.log.debug("Observing DCC only's 8.2 config.properties file");
					filePath = "./Users/DCCONLY/8.2/MPXN/"+mpxn+"/";
					}
					
					else
					{
						LoggerModel.log.debug("Observing DCC only's 8.2 config.properties file");
						filePath = "./Users/DCCONLY/8.2/DEVICEID/"+DeviceID+"/";
					}
				}
				
				else
				{
				LoggerModel.log.debug("Observing DCC only's config.properties file");
				filePath = "./Users/DCCONLY/";
				}	
			}
			
			else
			{
				filePath = "./Users/"+user+"/";	
				LoggerModel.log.debug("Reading "+user+ "s response_code.properties file");
			}
			
			prop.load(new FileInputStream(filePath+filename));
			Enumeration<?> e = prop.propertyNames();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				String value = prop.getProperty(key);
				if(key.equalsIgnoreCase(usrServiceVariant+"_"+payLoadType+"_alertcode")){
					alertCode = value;
										
				}
				/*else
				{
					LoggerModel.log.debug("Since alert code was not set, setting default alert code to 8F66.");
					alertCode="8F66";
				}*/
				
			}

		} catch (IOException ex) {
			LoggerModel.log.debug("File not found for user directory");
			LoggerModel.log.debug("File not found for user directory setting response code to IO");
			responseCode="I0";
			ex.printStackTrace();
			LoggerModel.log.debug(ex.getMessage(),ex);
		} finally {
			if (input != null) {
				try 
				{
					input.close();
				} 
				
				catch (IOException e)
				{
					e.printStackTrace();
					LoggerModel.log.debug(e.getMessage(),e);
				}
			}
		}
		
		return alertCode;
		
	}
	
	public static String getUserGBCSMessageCode(String user,String usrServiceVariant,String targetEUI, String mpxn, String payLoadType, String DeviceID ){
		Properties prop = new Properties();
		InputStream input = null;
		String gbcscode = "2000";
		
		try {
			
			String filename = "response_code.properties";
			String filePath;
			//System.err.println(user + Config.getDCCOnly().substring(0, 2));
			//System.out.println("file location" +   System.getProperty("user.dir"));
			if (targetEUI.equals(Config.getDCCOnly()) && !usrServiceVariant.equalsIgnoreCase("11.1"))
			{
				if(usrServiceVariant.equalsIgnoreCase("8.2"))
				{
					if(mpxn!="")
					{
					LoggerModel.log.debug("Observing DCC only's 8.2 config.properties file");
					filePath = "./Users/DCCONLY/8.2/MPXN/"+mpxn+"/";
					}
					
					else
					{
						LoggerModel.log.debug("Observing DCC only's 8.2 config.properties file");
						filePath = "./Users/DCCONLY/8.2/DEVICEID/"+DeviceID+"/";
					}
				}
				
				else
				{
				LoggerModel.log.debug("Observing DCC only's config.properties file");
				filePath = "./Users/DCCONLY/";
				}
			}
			else
			{
				filePath = "./Users/"+user+"/";	
				LoggerModel.log.debug("Reading "+user+ "s response_code.properties file");
			}
			
			prop.load(new FileInputStream(filePath+filename));
			Enumeration<?> e = prop.propertyNames();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				String value = prop.getProperty(key);
				//System.out.println(key+":"+value);
				if(key.equalsIgnoreCase(usrServiceVariant+"_"+payLoadType+"_gbcscode"))
				{
					gbcscode = value;
					
				}
				/*else
				{
					LoggerModel.log.debug("Since GBCS Hexadecimal message code was not set, setting it to 1000.");
					gbcscode="1000";
				}*/
				
			}

		} catch (IOException ex) {
			LoggerModel.log.debug("File not found for user directory");
			LoggerModel.log.debug("File not found for user directory setting response code to IO");
			responseCode="I0";
			ex.printStackTrace();
			LoggerModel.log.debug(ex.getMessage(),ex);
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
		
		return gbcscode;
		
	}
	
	public static String getDCCOnlyFromConfigFile() {
		Properties prop = new Properties();
		InputStream input = null;
		String dccOnlyFromConfig = "";
		
		
		try {
			
			String filename = "config.properties";
			String filePath = "./config/";
			prop.load(new FileInputStream(filePath+filename));


			Enumeration<?> e = prop.propertyNames();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				String value = prop.getProperty(key);
				if(key.equalsIgnoreCase("DCCBrokerID")){
					dccOnlyFromConfig = value;
				}
				
			}

		} catch (IOException ex) {
			ex.printStackTrace();
			LoggerModel.log.debug(ex.getMessage(),ex);
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
		return dccOnlyFromConfig;
	}

	
	
	

}
