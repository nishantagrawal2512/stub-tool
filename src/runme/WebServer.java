package runme;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import model.Config;
import model.DCCRequestPOJO;
import model.HTTPRequestHandler;
import model.InternetConfig;
import model.LoggerModel;
import model.MessageGeneration;
import model.SystemThreadingHandler;
import model.XMLCRUDHandler;
import model.XSDValidator;
import performanceTest.LoadBalancerHTTPRequestHandler;


/************************************************************
 * 
 * @author Shah Murad Hussain
 * @version 0.100
 * @since 09.09.2016
 * Accenture
 * 
 * MAIN METHOD FOR HOSTING THE STUB, HANDLES ALL WORKFLOWS WITHIN  THE CLASS
 * TEST CLASS IS ALSO AVAILABLE FOR SEEING IF STUB SERVICE IS AVAILABLE (TestWebServiceHandler)
 * 
 * @author Smit J Mirani
 * @version 106.1
 * @since 06.04.2017
 * Alerts can be now dynamically generated.
 * Configurable delay between alerts and responses.
 * Separate methods for alerts generation.
 * Proper renaming of response files.
 * originator and target IDs in response files are now proper.
 * More refined logging.
 * 
 * @author Smit J Mirani
 * @version 106.2
 * @since 07.04.2017
 * Changes in alerts name space
 * alerts, request and response files for 8.2 DUIS will be stored in the Users/DCCONLY/8.2/MPXN/(POD number) folder.
 * 
 * @author Smit J Mirani
 * @version 106.3
 * @since 12.04.2017
 * Exception for 11.1 alert. Added Deviceidlist in TargEUI
 * 
 * @author Smit J Mirani
 * @version 107
 * @since 14.04.2017
 * DCC Only alerts and normal alerts differentiation. They will be generated differently.
 *  
 * @author Smit J Mirani
 * @version 107.1
 * @since 18.04.2017
 * Device Alert for 11.1, fetching all contents from user's foldere 
 **************************************************************/

public class WebServer {
	
	
	public static void main(String[] args) throws Exception {
		HttpServer server;
		
		//allows configuration to be in memory 
		Config.storeConfigListInMemory();
		final int portNumber = Config.findUsersPortNumber();
		InternetConfig.getInternetConfigInfo();
		server = HttpServer.create(new InetSocketAddress(portNumber), 0);
		//OuterClass.InnerClass innerObject = outerObject.new InnerClass();
		//Instantiate web server object to allow creation of inner class instance
		WebServer webServer = new WebServer();
		//creating handler for test URL
		WebServer.TestWebServiceHandler testURLHandler= webServer.new TestWebServiceHandler();
		server.createContext("/test", testURLHandler);
		server.createContext("/testServer", testURLHandler);
		server.createContext("/", testURLHandler);
		//creating DCCHandler for mockServer URL
		WebServer.DCCHandler webServerDCCHandler = webServer.new DCCHandler();
		server.createContext("/mockServer", webServerDCCHandler);
 
		//ExecutorService object handles multithreading
		ExecutorService executorService = Executors.newFixedThreadPool(SystemThreadingHandler.findThreadCount());
		server.setExecutor(executorService);
		
		//server.setExecutor(null); // creates a default executor
		server.start();
		LoggerModel.log.debug("Creating server to port "+portNumber);
		LoggerModel.log.debug("Press Enter on command line to stop the server.");

		try{
			System.in.read();
		}
		catch (IOException e){
			LoggerModel.log.debug(e.getMessage(),e);
			
		}
		
		LoggerModel.log.debug("Stopping Server.....");
		server.removeContext("/test");
		server.removeContext("/testServer");
		server.createContext("/", testURLHandler);
		server.removeContext("/mockServer");
		executorService.shutdown();
		server.stop(0);
		LoggerModel.log.info("Accenture - Smit Mirani");
		LoggerModel.log.debug("Stopped server");
	}

	
	class TestWebServiceHandler implements HttpHandler {
		
		@Override
		public void handle(HttpExchange httpExchange) throws IOException {
			/**
			 * Class used to test
			 */
			String response = "<html><body><h1>SMG Stub Server is Running </h1></body> </html>";
			httpExchange.sendResponseHeaders(200, response.length());
			OutputStream outputStream = httpExchange.getResponseBody();
			outputStream.write(response.getBytes());
			outputStream.close();
			String requestMethod = httpExchange.getRequestMethod();
			LoggerModel.log.debug("Server is running");
			LoggerModel.log.debug("Recieved "+requestMethod+ " request");
			
		}



	}

	class DCCHandler implements HttpHandler {
		 
		
		@Override
		public void handle(HttpExchange he) throws IOException {
			DCCRequestPOJO requestPOJO = new DCCRequestPOJO();
			
			InputStreamReader inputStreamReader = new InputStreamReader(he.getRequestBody(), "utf-8");
			requestPOJO.setRequestData(saveRequestPayLoadToString(inputStreamReader));
			
			XMLCRUDHandler xmlCrudHandler = new XMLCRUDHandler();
			//must be a way of decoupling this
			requestPOJO.setRequestFileName(xmlCrudHandler.writeRequestToXMLFile(requestPOJO.getRequestData(), requestPOJO.getServiceReferenceVariant()));
			xmlCrudHandler.readXMLRequestValues(requestPOJO.getRequestFileName(),requestPOJO);
			String mpan = requestPOJO.getMpxn();
			System.out.println("MPXN : "+mpan);
			//saves xml to file
			if(requestPOJO.getUserProfile()!=""){
				xmlCrudHandler.writeRequestToXMLUserFile(requestPOJO.getTargetEui(), requestPOJO.getRequestFileName(), requestPOJO.getUserProfile(), requestPOJO.getRequestData(),requestPOJO.getServiceReferenceVariant(),requestPOJO.getMpxn(), requestPOJO.getDeviceIDList(), requestPOJO.getDeviceID());
			}
			
			boolean passedXSDValidation = XSDValidator.validateRequestMessage(requestPOJO.getRequestFileName());
			
			ConcurrentHashMap<String,String> serviceReferenceConfigList = Config.findServerVariantsConfig(requestPOJO.getServiceReferenceVariant());
			//Async Workflow
		
			if(serviceReferenceConfigList.get("ack").equalsIgnoreCase("yes"))
			{
				//checking for ack flag
				String ackFileName;
				LoggerModel.log.debug("Creating Message Acknowledgement (ack)");
				
				MessageGeneration messageGeneration = new MessageGeneration();
				
				if (passedXSDValidation){
					ackFileName = messageGeneration.createResponseMessage("ack",
							requestPOJO.getOriginatorEui(),
							requestPOJO.getTargetEui(),
							requestPOJO.getOriginatorCounter(),
							requestPOJO.getServiceReference(),
							requestPOJO.getServiceReferenceVariant(),
							requestPOJO.getUserProfile(),
							requestPOJO.getMpxn(),
							requestPOJO.getDeviceIDList(),
							requestPOJO.getDeviceID());
				}
				else{
					ackFileName = messageGeneration.generateErrorMessage("ack",
							requestPOJO.getOriginatorEui(),
							requestPOJO.getTargetEui(),
							requestPOJO.getOriginatorCounter(),
							requestPOJO.getServiceReference(),
							requestPOJO.getServiceReferenceVariant(),
							requestPOJO.getUserProfile(),
							requestPOJO.getDeviceID());
				}
					
				if (Config.checkLoadBalancerURLExists()){
					LoggerModel.log.debug("Sending response to Load balancer URL to "+ Config.getLoadBalancerURL());
					LoggerModel.log.debug("Creating new HTTP Connection");
					LoadBalancerHTTPRequestHandler requestHandler = new LoadBalancerHTTPRequestHandler(Config.getLoadBalancerURL(), ackFileName);
					requestHandler.sendPost();
				}
				else{
					//When loadBalanceURL is not set in config.properties
					LoggerModel.log.debug("Sending response to system creating the request...");
					generateHTTPResponse(he, ackFileName);
					LoggerModel.log.debug("Sent a response to the system");
				}
				
				if(serviceReferenceConfigList.containsKey("alertbefore")&&serviceReferenceConfigList.get("alertbefore").equalsIgnoreCase("yes")&& passedXSDValidation)
				{
					/*boolean isAlertBeforeDeviceAlert=false;
					if(serviceReferenceConfigList.get("IsAlertBeforeDeviceAlert").equalsIgnoreCase("yes"))
							{
								isAlertBeforeDeviceAlert=true;
							}
					else
					{
						isAlertBeforeDeviceAlert=false;
					}*/
					generateAlertBefore(messageGeneration,requestPOJO);
					
				}
				else
				{
					System.out.println("Alert before variable was not set");
					
				}
				if(serviceReferenceConfigList.get("async").equalsIgnoreCase("yes")&& passedXSDValidation){
					LoggerModel.log.debug("entering asynchronous workflow");
					try
			          {
			            int unit = 1000;
			            int seconds = Config.getAsyncDelay();
			            
			            Thread.sleep(unit * seconds);
			          }
			          catch (InterruptedException e)
			          {
			            LoggerModel.log.debug(e.getMessage(), e);
			          }
					
					String asyncFileName =messageGeneration.createResponseMessage("async",
							requestPOJO.getOriginatorEui(),
							requestPOJO.getTargetEui(),
							requestPOJO.getOriginatorCounter(),
							requestPOJO.getServiceReference(),
							requestPOJO.getServiceReferenceVariant(),
							requestPOJO.getUserProfile(),
							requestPOJO.getMpxn(),
							requestPOJO.getDeviceIDList(),
							requestPOJO.getDeviceID());
					HTTPRequestHandler requestHander = new HTTPRequestHandler(Config.getMdmURL(),asyncFileName);
					requestHander.sendPost();
					
				}
				
				else
				{
					System.out.println("Async variable was not set");
					
				}
				
				
				if(serviceReferenceConfigList.containsKey("alertafter")&&serviceReferenceConfigList.get("alertafter").equalsIgnoreCase("yes")&& passedXSDValidation)
				{
					/*boolean isAlertAfterDeviceAlert;
					if(serviceReferenceConfigList.get("IsAlertAfterDeviceAlert").equalsIgnoreCase("yes"))
							{
								isAlertAfterDeviceAlert=true;
							}
					else
					{
						isAlertAfterDeviceAlert=false;
					}*/
			
					generateAlertAfter(messageGeneration,requestPOJO);
				}
				
				else
				{
					System.out.println("Alert after variable was not set");
					
				}
				
			}
			
			
			
			//Sync Workflow
			else if (serviceReferenceConfigList.get("sync").equalsIgnoreCase("yes")&& passedXSDValidation){
				//creates sync request
				LoggerModel.log.debug("Creating Synchronous response");
				String syncFileName="";
				MessageGeneration messageGeneration = new MessageGeneration();
				if (passedXSDValidation){
					
					 LoggerModel.log.debug("Sync message passed XSD Validation");
					 syncFileName=messageGeneration.createResponseMessage("sync",
							 											requestPOJO.getOriginatorEui(),
							 											requestPOJO.getTargetEui(),
							 											requestPOJO.getOriginatorCounter(),
							 											requestPOJO.getServiceReference(),
							 											requestPOJO.getServiceReferenceVariant(),
							 											requestPOJO.getUserProfile(),
							 											requestPOJO.getMpxn(),
							 											requestPOJO.getDeviceIDList(),
							 											requestPOJO.getDeviceID());
				}
				else{
					 LoggerModel.log.debug("Sync message failed XSD Validation");
					 syncFileName=messageGeneration.generateErrorMessage("sync",
							 											requestPOJO.getOriginatorEui(),
							 											requestPOJO.getTargetEui(),
							 											requestPOJO.getOriginatorCounter(),
							 											requestPOJO.getServiceReference(),
							 											requestPOJO.getServiceReferenceVariant(),
							 											requestPOJO.getUserProfile(),
							 											requestPOJO.getDeviceID());
				}
				
				
				if (Config.checkLoadBalancerURLExists()){
					LoggerModel.log.debug("Sending response to Load balancer URL to "+ Config.getLoadBalancerURL());
					LoadBalancerHTTPRequestHandler requestHandler = new LoadBalancerHTTPRequestHandler(Config.getLoadBalancerURL(), syncFileName);
					requestHandler.sendPost();
				}
				
			
				else{
					LoggerModel.log.debug("Sending response to system creating the request");
					generateHTTPResponse(he, syncFileName);
				}
				
				if(serviceReferenceConfigList.containsKey("alertafter")&&serviceReferenceConfigList.get("alertafter").equalsIgnoreCase("yes")&& passedXSDValidation)
				{
					/*boolean isAlertAfterDeviceAlert;
					if(serviceReferenceConfigList.get("IsAlertAfterDeviceAlert").equalsIgnoreCase("yes"))
							{
								isAlertAfterDeviceAlert=true;
							}
					else
					{
						isAlertAfterDeviceAlert=false;
					}*/
			
					generateAlertAfter(messageGeneration,requestPOJO);
				}
				
				else
				{
					System.out.println("Alert after variable was not set");
					
				}
				
			}

		}
		
		private void generateAlertBefore(MessageGeneration messageGeneration, DCCRequestPOJO requestPOJO) throws IOException 
		{
			// TODO Auto-generated method stub
			LoggerModel.log.debug("Sending  alert before");
			try
	          {
	            int unit = 1000;
	            int seconds = Config.getAlertBeforeDelay();
	            
	            Thread.sleep(unit * seconds);
	          }
	          catch (InterruptedException e)
	          {
	            LoggerModel.log.debug(e.getMessage(), e);
	          }
			String alertbeforefilename;
			alertbeforefilename = messageGeneration.createResponseMessage("alertbefore",
					requestPOJO.getOriginatorEui(),
					requestPOJO.getTargetEui(),
					requestPOJO.getOriginatorCounter(),
					requestPOJO.getServiceReference(),
					requestPOJO.getServiceReferenceVariant(),
					requestPOJO.getUserProfile(),
					requestPOJO.getMpxn(),
					requestPOJO.getDeviceIDList(),
					requestPOJO.getDeviceID());
			HTTPRequestHandler requestHander = new HTTPRequestHandler(Config.getMdmURL(),alertbeforefilename);
			requestHander.sendPost();			
			
		}
		
		private void generateAlertAfter(MessageGeneration messageGeneration, DCCRequestPOJO requestPOJO) throws IOException {
			// TODO Auto-generated method stub
			LoggerModel.log.debug("Sending alert after");
			try
	          {
	            int unit = 1000;
	            int seconds = Config.getAlertAfterDelay();
	            
	            Thread.sleep(unit * seconds);
	          }
	          catch (InterruptedException e)
	          {
	            LoggerModel.log.debug(e.getMessage(), e);
	          }
			String alertafterfilename;
			alertafterfilename = messageGeneration.createResponseMessage("alertafter",
					requestPOJO.getOriginatorEui(),
					requestPOJO.getTargetEui(),
					requestPOJO.getOriginatorCounter(),
					requestPOJO.getServiceReference(),
					requestPOJO.getServiceReferenceVariant(),
					requestPOJO.getUserProfile(),
					requestPOJO.getMpxn(),
					requestPOJO.getDeviceIDList(),
					requestPOJO.getDeviceID());
			HTTPRequestHandler requestHander = new HTTPRequestHandler(Config.getMdmURL(),alertafterfilename);
			requestHander.sendPost();
			
		}

		private void generateHTTPResponse(HttpExchange httpExchange,String ackResponseFileName) throws IOException {
			String response="";
			//System.out.println(httpExchange.getRequestURI());
			Headers httpHeader = httpExchange.getResponseHeaders();
			httpHeader.set("Content-Type","application/xml");
			httpExchange.sendResponseHeaders(200, response.length());
			OutputStream os = httpExchange.getResponseBody();
			BufferedReader bufferedReader = null;
			try {
				String currentStringLines;
				bufferedReader = new BufferedReader(new FileReader(ackResponseFileName));
				while ((currentStringLines = bufferedReader.readLine()) != null) {
					response+= currentStringLines;
				}

			} catch (IOException e) {
				LoggerModel.log.debug(e.getMessage(),e);
			} finally {
				try {
					if (bufferedReader != null)bufferedReader.close();
				} catch (IOException ex) {
					LoggerModel.log.debug(ex.getMessage(),ex);
				}
			}
			os.write(response.getBytes());
			os.close();
			LoggerModel.log.debug("Completed and closed the connection");
			
		}

		private String saveRequestPayLoadToString(InputStreamReader isr) throws IOException {
			/**
			 * Save HTTP request payload to string 
			 *
			 */
			BufferedReader reader = new BufferedReader(isr);
			String word="";
			String line;
			while ((line = reader.readLine()) != null) {
				word+=line;
				word+=("\n");

			}
			return word.toString();

		}
	}
}
