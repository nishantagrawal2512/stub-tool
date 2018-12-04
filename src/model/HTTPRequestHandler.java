package model;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;


/************************************************************
 * 
 * @author Shah Murad Hussain
 * @version 0.100
 * @since 09.09.2016
 * Accenture
 * 
 ************************************************************/

public class HTTPRequestHandler implements Runnable {
	
	private static String username = Config.getMDMuserName();
	private static String password = Config.getMDMpassword();
	private static String AUTHENTICATION =username+":"+ password;
	private String url;
	private String responseFileName;
	
	
	public HTTPRequestHandler (String u,String async){
		url = u;
		responseFileName=async;
	}
	
	public  void sendPost() {
		
		
		try{
			
			LoggerModel.log.debug("Sending message to the url: "+url);
			
			URL urlConnection = new URL(url);
			HttpURLConnection httpConnection = (HttpURLConnection) urlConnection.openConnection();
			LoggerModel.log.debug("Established the URL Connection");
			httpConnection.setDoOutput(true);
			String encoded = Base64.encode((AUTHENTICATION).getBytes("UTF-8"));
			httpConnection.setRequestProperty("Authorization", "Basic "+ encoded);
			httpConnection.setRequestProperty("Accept", "*/*");
			httpConnection.setRequestProperty("Content-type", "application/xml");
			
			//java.security.Permission permissions =con.getPermission();
			//System.out.println("--------Describing permisions----------");
			//System.out.println(permissions.toString());
			
			//add request header
			
			// --Displays header file
			
			httpConnection.setRequestMethod("POST");
			httpConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			//String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
			
			BufferedReader bufferedReader = null;
			String messageOutput = "";
			String currentLine;
			try {
				bufferedReader = new BufferedReader(new FileReader(responseFileName));
				while ((currentLine = bufferedReader.readLine()) != null) {
					messageOutput+= currentLine;
				}

			} catch (IOException e) {
				e.printStackTrace();
				LoggerModel.log.debug(e.getMessage(),e);
			} finally {
				try {
					if (bufferedReader != null)bufferedReader.close();
				} catch (IOException ex) {
					ex.printStackTrace();
					LoggerModel.log.debug(ex.getMessage(),ex);
				}
			}
			//LoggerModel.log.debug("Creating dataoutputstream");

			// Send post request
			DataOutputStream dataOutputStream = new DataOutputStream(httpConnection.getOutputStream());
			//wr.writeBytes(urlParameters);
			
			LoggerModel.log.debug("\nSending 'POST' request to URL : " + url);
			dataOutputStream.write(messageOutput.getBytes());
			//System.out.println("Output stream: "+httpConnection.getOutputStream());
			dataOutputStream.flush();
			dataOutputStream.close();
			
			int responseCode = httpConnection.getResponseCode();
			//System.out.println("Post parameters : " + urlParameters);
			LoggerModel.log.debug("Response Code : " + responseCode);
			
			//print result
			LoggerModel.log.debug("Finished executing Asynchronous HTTP Messages");
			//System.out.println(response.toString());
			
		}
		catch (IOException ie){
			LoggerModel.log.debug(ie.getMessage(),ie);
			ie.printStackTrace();
		}

	}

	@Override
	public void run() {
		sendPost();
		
	}

}
