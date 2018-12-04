package model;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class InternetConfig {
	

/************************************************************
 * 
 * @author Shah Murad Hussain
 * @version 0.100
 * @since 30.08.2016
 * Accenture
 * Object created to get the meta data of the system hosted SMG/DCC Stub 
 ************************************************************/
	
	public static void getInternetConfigInfo(){
		
			try {
				InetAddress inetAddr = InetAddress.getLocalHost();
				byte[] addr = inetAddr.getAddress();
				String ipAddress = "";
				for (int i = 0; i < addr.length; i++) {
					if (i > 0) {
						ipAddress += ".";
					}
					ipAddress += addr[i] & 0xFF;
				}
				
				String hostname = inetAddr.getHostName();
				
				LoggerModel.log.debug("IP Address: " + ipAddress);
				LoggerModel.log.debug("Hostname: " + hostname);
				//System.out.println("IP Address: " + ipAddress);
				//System.out.println("Hostname: " + hostname);
			    
			}
			catch (UnknownHostException e) {
				LoggerModel.log.debug("Host not found: " + e.getMessage());
				//System.out.println("Host not found: " + e.getMessage());
			}
			
	}
	
}
