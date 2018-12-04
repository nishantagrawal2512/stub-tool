package model;

public class DCCRequestPOJO {
	

/************************************************************
 * 
 * @author Shah Murad Hussain
 * @version 0.100
 * @since 09.09.2016
 * Accenture
 * This class is a data structure 
 * used to hold the values of the XML payload sent
 * from the calling system e.g. MDM
 * 
 * @author Smit Mirani
 * @version 106.1
 * @since 06.04.2017
 * Added MPxN variable for the DCCOnly requests
 ************************************************************/
	
	private String requestFileName="";
	private String originatorEui="";
	private String targetEui="";
	private String originatorCounter="";
	private String serviceReference="";
	private String serviceReferenceVariant="";
	private String userProfile="";
	private String REQUEST_NAMESPACE_PREFIX= "";
	private String requestData="";
	private String mpxn ="";
	private String DeviceIDList ="";
	private String DeviceID ="";
	
	public String getDeviceID() {
		return DeviceID;
	}
	public void setDeviceID(String deviceID) {
		this.DeviceID = deviceID;
	}
	public String getDeviceIDList() {
		return DeviceIDList;
	}
	public void setDeviceIDList(String deviceIDList) {
		this.DeviceIDList = deviceIDList;
	}
	public String getMpxn() {
		return mpxn;
	}
	public void setMpxn(String mpxn) {
		this.mpxn = mpxn;
	}
	public String getRequestFileName() {
		return requestFileName;
	}
	public void setRequestFileName(String requestFileName) {
		this.requestFileName = requestFileName;
	}
	public String getOriginatorEui() {
		return originatorEui;
	}
	public void setOriginatorEui(String originatorEui) {
		this.originatorEui = originatorEui;
	}
	public String getTargetEui() {
		return targetEui;
	}
	public void setTargetEui(String targetEui) {
		this.targetEui = targetEui;
	}
	public String getOriginatorCounter() {
		return originatorCounter;
	}
	public void setOriginatorCounter(String originatorCounter) {
		this.originatorCounter = originatorCounter;
	}
	public String getServiceReference() {
		return serviceReference;
	}
	public void setServiceReference(String serviceReference) {
		this.serviceReference = serviceReference;
	}
	public String getServiceReferenceVariant() {
		return serviceReferenceVariant;
	}
	public void setServiceReferenceVariant(String serviceReferenceVariant) {
		this.serviceReferenceVariant = serviceReferenceVariant;
	}
	public String getUserProfile() {
		return userProfile;
	}
	public void setUserProfile(String userProfile) {
		this.userProfile = userProfile;
	}
	public String getREQUEST_NAMESPACE_PREFIX() {
		return REQUEST_NAMESPACE_PREFIX;
	}
	public void setREQUEST_NAMESPACE_PREFIX(String rEQUEST_NAMESPACE_PREFIX) {
		this.REQUEST_NAMESPACE_PREFIX = rEQUEST_NAMESPACE_PREFIX;
	}
	public String getRequestData() {
		return requestData;
	}
	public void setRequestData(String requestData) {
		this.requestData = requestData;
	}
	
	
	
}
