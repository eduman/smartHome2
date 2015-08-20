package it.eduman.mobileHome2.communication;

import it.eduman.android.commons.utilities.KeyValue;
import it.eduman.android.commons.utilities.Response;
import it.eduman.android.commons.utilities.TaskOn;
import it.eduman.smartHome.deprecated.device.DeviceContent;
import it.eduman.smartHome.deprecated.device.MeasurementContent;
import it.eduman.smartHome.deprecated.home.BuildingContent;
import it.eduman.smartHome.deprecated.home.HomeContent;
import it.eduman.smartHome.deprecated.home.RoomContent;
import it.eduman.smartHome.deprecated.security.MyCipher;
import it.eduman.smartHome.deprecated.security.SecurityException;
import it.eduman.smartHome.deprecated.userMessage.UserMessageContent;
import it.eduman.smartHome.deprecated.webServices.QueryContent;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ProxyWebServices {
//	private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
	private static final String DATE_FORMAT_PATTERN = "MMM dd, yyyy hh:mm:ss";
//	private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN);
	private static final String NAMESPACE = "http://androidProxy.eduman.it";
	private SharedPreferences sharedPref;
	private Context context;
	private Gson gson;
	private String proxyAddress;
	private String proxyPortNumber;
	private String lsWebServiceName;
	private String wsdlName;
	private boolean isCommunicationCiphered;
	private String password;
	private WifiManager wifiManager;
	
	public ProxyWebServices(Context context, boolean isCommunicationCiphered){
		this.context = context;
		this.sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
//		password = this.sharedPref.getString(
//				context.getString(R.string.preference_androidProxyPassword_key), null);
//
//		this.gson = new GsonBuilder().setDateFormat(DATE_FORMAT_PATTERN).create();
//		proxyAddress = this.sharedPref.getString(
//				context.getString(R.string.preference_androidProxyWebServiceAddress_key), null);
//
//		proxyPortNumber = this.sharedPref.getString(
//				context.getString(R.string.preference_androidProxyWebServicePortNumber_key), null);
//
//		lsWebServiceName = this.sharedPref.getString(
//				context.getString(R.string.preference_webServiceName_key), "AndroidProxy");
		
		wsdlName = ServiceNames.Proxy.AndroidProxy.toString();
		this.isCommunicationCiphered = isCommunicationCiphered;
		
		this.wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
	}
	
	
	public String getWsdlName() {
		return wsdlName;
	}



	public String getPassword() {
		return password;
	}


	public ProxyWebServices setPassword(String password) {
		this.password = password;
		return this;
	}


	public ProxyWebServices setWsdlName(String wsdlName) {
		this.wsdlName = wsdlName;
		return this;
	}



	public String getProxyAddress(){
		return proxyAddress;
	}
	
	public String getProxyPortNumber(){
		return proxyPortNumber;
	}
	
	public ProxyWebServices setProxyAddress(String proxyAddress){
		this.proxyAddress = proxyAddress;
		return this;
	}
	
	public ProxyWebServices setProxyPortNumber(String proxyPortNumber){
		this.proxyPortNumber = proxyPortNumber;
		return this;
	}
	
	public String getLsWebServiceName(){
		return lsWebServiceName;
	}
	
	public ProxyWebServices setLsWebServiceName(String lsWebServiceName){
		this.lsWebServiceName = lsWebServiceName;
		return this;
	}
	
	private String getProxyWebServiceUrl() throws WebServicesException{
		String URL = "";
		boolean isTunneled = false;
		boolean autoSwitchOnSSID = false;
		boolean userSSID =false;
//		boolean isTunneled = this.sharedPref.getBoolean(
//				context.getString(R.string.preference_linksmartTunneling_key), false);
//		boolean autoSwitchOnSSID = this.sharedPref.getBoolean(
//				context.getString(R.string.preference_ssid_recognition_key), false);
//		String userSSID = String.format("\"%s\"",
//				this.sharedPref.getString(
//				context.getString(R.string.preference_ssid_name_recognition_key), "unknown_ssid"));
//
//
//		if (isTunneled){
//	        String ssid = this.wifiManager.getConnectionInfo().getSSID();
//	        ssid.replace("\"", "");
//	        //TODO
//			if (autoSwitchOnSSID && ssid.equalsIgnoreCase(userSSID)){
//				isTunneled = false;
//			} else {
//				try{
//					NetworkManagerWebServices nmWebServices = new NetworkManagerWebServices(context);
//					URL = nmWebServices.getTunneledUrl(this.getLsWebServiceName());
//					isTunneled = true;
//				} catch (WebServicesException e) {
//					throw new WebServicesException("Unable to find the remote Web Service called " + this.getLsWebServiceName(), e);
//				}
//			}
//		}
//
//		if (!isTunneled) {
//			URL = "http://"+ this.getProxyAddress() + ":"
//					+ this.getProxyPortNumber()
//					+ "/axis/services/" + wsdlName + "?wsdl";
//		}
		return URL;
	}
		
	private StringResponse getWebServiceResponse(String methodName, List<KeyValue> parameters) {
		try {
			WebServicesWrapper wsWrapp = 
					new WebServicesWrapper(NAMESPACE, this.getProxyWebServiceUrl());
			String webServiceReply = wsWrapp.callWebServiceMethod(methodName, parameters);
			String webServiceReplyBody = webServiceReply;
			if (webServiceReplyBody.startsWith("anyType{body=")) {
				webServiceReplyBody = webServiceReplyBody.substring("anyType{body=".length());
				if (webServiceReplyBody.endsWith("; ok=true; }")) {
					webServiceReplyBody = webServiceReplyBody.substring(0, webServiceReplyBody.length() - "; ok=true; }".length());
					return StringResponse.createOkResponse(webServiceReplyBody);
				} else if (webServiceReplyBody.endsWith("; ok=false; }")) {
					webServiceReplyBody = webServiceReplyBody.substring(0, webServiceReplyBody.length() - "; ok=false; }".length());
					return StringResponse.createErrorResponse(webServiceReplyBody);
				} else {
					return StringResponse.createErrorResponse("Invalid web service response format:\n" + webServiceReply);
				}
			} else return StringResponse.createErrorResponse("Invalid web service response format:\n" + webServiceReply);
		} catch (IOException e) {
			return StringResponse.createErrorResponse("IOException while getting web service response:\n" + e.getMessage());
		} catch (XmlPullParserException e) {
			return StringResponse.createErrorResponse("XmlPullParserException while getting web service response:\n" + e.getMessage());
		} catch (WebServicesException e) {
			return StringResponse.createErrorResponse(e.getMessage());
		}
	}
	
//	private <T> Object callJsonMethod(String method, 
//			List<KeyValue> parameters, Class<T> _class, TaskOn<Response<T>> task) {
//		StringResponse webServiceResponse = getWebServiceResponse(method, parameters);
//		Response<T> response;
//		if (!webServiceResponse.isOk()) {
//			response = Response.createErrorResponse("Web service response error:\n" + webServiceResponse.getBody());
//		} else {
//			try {
//				String json = webServiceResponse.getBody();
//				if (isCrypted){
//					json = MyCipher.decrypt(password, webServiceResponse.getBody());
//				}
//				T packet = gson.fromJson(json, _class);
//				response = Response.createOkResponse(packet);
//			} catch (SecurityException e) {
//				response = Response.createErrorResponse(e.getMessage());
//			}
//			
//		}
//		return task.doTask(response);
//	}
	
	private <T> Object callJsonMethod(String method, 
			List<KeyValue> parameters, Type typeOf, TaskOn<Response<T>> task) {
		StringResponse webServiceResponse = getWebServiceResponse(method, parameters);
		Response<T> response;
		if (!webServiceResponse.isOk()) {
			response = Response.createErrorResponse("Web service response error:\n" + webServiceResponse.getBody());
		} else {
			try {
				String json = webServiceResponse.getBody();
				if (isCommunicationCiphered){
					json = MyCipher.decrypt(password, webServiceResponse.getBody());
				}
				T packet = gson.fromJson(json, typeOf);
				response = Response.createOkResponse(packet);
			} catch (SecurityException e) {
				response = Response.createErrorResponse(e.getMessage());
			} catch (Exception e) {
				response = Response.createErrorResponse(e.getMessage());
			}
			
		}
		return task.doTask(response);
	}
	
//	//StringResponse Version
//		public StringResponse actuate(String deviceID, String actuator, String command, String value){
//			List<KeyValue> parameters = new ArrayList<KeyValue>();
//			parameters.add(new KeyValue("in0", deviceID));
//			parameters.add(new KeyValue("in1", actuator));
//			parameters.add(new KeyValue("in2", command));
//			parameters.add(new KeyValue("in3", value));
//			return this.getWebServiceResponse("actuate", parameters);
//		}
		
		
		//TODO
		// versione JSON
//		public Object actuate(
//				String deviceID, String actuator, String command, 
//				String value, TaskOn<Response<DeviceContent>> task){
//			List<KeyValue> parameters = new ArrayList<KeyValue>();
//			parameters.add(new KeyValue("in0", deviceID));
//			parameters.add(new KeyValue("in1", actuator));
//			parameters.add(new KeyValue("in2", command));
//			parameters.add(new KeyValue("in3", value));
//			return this.callJsonMethod("actuate", parameters, DeviceContent.class, task);
//		}
		
		public Object getAlldevices(TaskOn<Response<ArrayList<DeviceContent>>> task){
			Type type = new TypeToken<ArrayList<DeviceContent>>(){}.getType();
			return this.callJsonMethod("getAllDevices", null, type, task);
		}
		
		public Object getDevice(String deviceID, TaskOn<Response<DeviceContent>> task){
			List<KeyValue> parameters = new ArrayList<KeyValue>();
			parameters.add(new KeyValue("in0", deviceID));
			return this.callJsonMethod("getDevice", parameters, DeviceContent.class, task);
		}
		
		public Object getAllBuildings (
				boolean areMeasurementsIncluded, TaskOn<Response<ArrayList<BuildingContent>>> task){
			List<KeyValue> parameters = new ArrayList<KeyValue>();
			parameters.add(new KeyValue("in0", areMeasurementsIncluded));
			Type type = new TypeToken<ArrayList<BuildingContent>>(){}.getType();
			return this.callJsonMethod("getAllBuildings", parameters, type, task);
		}
		
		public Object getAllHomes (boolean areMeasurementsIncluded, TaskOn<Response<ArrayList<HomeContent>>> task){
			List<KeyValue> parameters = new ArrayList<KeyValue>();
			parameters.add(new KeyValue("in0", areMeasurementsIncluded));
			Type type = new TypeToken<ArrayList<HomeContent>>(){}.getType();
			return this.callJsonMethod("getAllHomes", parameters, type, task);
		}
		
		public Object getAllRooms (boolean areMeasurementsIncluded, TaskOn<Response<ArrayList<RoomContent>>> task){
			List<KeyValue> parameters = new ArrayList<KeyValue>();
			parameters.add(new KeyValue("in0", areMeasurementsIncluded));
			Type type = new TypeToken<ArrayList<RoomContent>>(){}.getType();
			return this.callJsonMethod("getAllRooms", parameters, type, task);
		}
		
		public Object getAllDevicesFromDB (
				boolean areMeasurementsIncluded, TaskOn<Response<ArrayList<DeviceContent>>> task){
			List<KeyValue> parameters = new ArrayList<KeyValue>();
			parameters.add(new KeyValue("in0", areMeasurementsIncluded));
			Type type = new TypeToken<ArrayList<DeviceContent>>(){}.getType();
			return this.callJsonMethod("getAllDevicesFromDB", parameters, type, task);
		}
		
		public Object getAllMeasurements (TaskOn<Response<ArrayList<MeasurementContent>>> task){
			Type type = new TypeToken<ArrayList<MeasurementContent>>(){}.getType();
			return this.callJsonMethod("getAllMeasurements", null, type, task);
		}
		
		public Object getBuilding (
				String buildingID, boolean areHomeIncluded, 
				boolean areMeasurementsIncluded, TaskOn<Response<BuildingContent>> task){
			List<KeyValue> parameters = new ArrayList<KeyValue>();
			parameters.add(new KeyValue("in0", buildingID));
			parameters.add(new KeyValue("in1", areHomeIncluded));
			parameters.add(new KeyValue("in2", areMeasurementsIncluded));
			return this.callJsonMethod("getBuilding", parameters, BuildingContent.class, task);
		}
		
		public Object getHome (
				String homeID, boolean areMeasurementsIncluded, TaskOn<Response<HomeContent>> task){
			List<KeyValue> parameters = new ArrayList<KeyValue>();
			parameters.add(new KeyValue("in0", homeID));
			parameters.add(new KeyValue("in1", areMeasurementsIncluded));
			return this.callJsonMethod("getHome", parameters, HomeContent.class, task);
		}
		
		public Object getRoom (
				String roomID, boolean areMeasurementsIncluded, TaskOn<Response<RoomContent>> task){
			List<KeyValue> parameters = new ArrayList<KeyValue>();
			parameters.add(new KeyValue("in0", roomID));
			parameters.add(new KeyValue("in1", areMeasurementsIncluded));
			return this.callJsonMethod("getRoom", parameters, RoomContent.class, task);
		}
		
		public Object getDeviceFromDB (
				String deviceID, boolean areMeasurementsIncluded, TaskOn<Response<DeviceContent>> task){
			List<KeyValue> parameters = new ArrayList<KeyValue>();
			parameters.add(new KeyValue("in0", deviceID));
			parameters.add(new KeyValue("in1", areMeasurementsIncluded));
			return this.callJsonMethod("getDeviceFromDB", parameters, DeviceContent.class, task);
		}
		
		public Object getAllDevicesByHomeID (
				String homeID, boolean areMeasurementsIncluded, TaskOn<Response<HomeContent>> task){
			List<KeyValue> parameters = new ArrayList<KeyValue>();
			parameters.add(new KeyValue("in0", homeID));
			parameters.add(new KeyValue("in1", areMeasurementsIncluded));
			return this.callJsonMethod("getAllDevicesByHomeID", parameters, HomeContent.class, task);
		}
		
		public Object getAllDevicesByRoomID (
				String roomID, boolean areMeasurementsIncluded, TaskOn<Response<RoomContent>> task){
			List<KeyValue> parameters = new ArrayList<KeyValue>();
			parameters.add(new KeyValue("in0", roomID));
			parameters.add(new KeyValue("in1", areMeasurementsIncluded));
			return this.callJsonMethod("getAllDevicesByRoomID", parameters, RoomContent.class, task);
		}
		
		public Object getMeasurement (
				String measurementId, TaskOn<Response<MeasurementContent>> task){
			List<KeyValue> parameters = new ArrayList<KeyValue>();
			parameters.add(new KeyValue("in0", measurementId));
			return this.callJsonMethod("getMeasurement", parameters, MeasurementContent.class, task);
		}
		
		public Object getLastMeasurementByDeviceID (
				String deviceId, String measure, TaskOn<Response<MeasurementContent>> task){
			List<KeyValue> parameters = new ArrayList<KeyValue>();
			parameters.add(new KeyValue("in0", deviceId));
			parameters.add(new KeyValue("in1", measure));
			return this.callJsonMethod("getLastMeasurementByDeviceID", parameters, MeasurementContent.class, task);
		}
		
		public Object getLastMeasurementByHomeID (
				String homeId, String measure, TaskOn<Response<MeasurementContent>> task){
			List<KeyValue> parameters = new ArrayList<KeyValue>();
			parameters.add(new KeyValue("in0", homeId));
			parameters.add(new KeyValue("in1", measure));
			return this.callJsonMethod("getLastMeasurementByHomeID", parameters, MeasurementContent.class, task);
		}
		
		public Object getLastMeasurementByRoomID (
				String roomId, String measure, TaskOn<Response<MeasurementContent>> task){
			List<KeyValue> parameters = new ArrayList<KeyValue>();
			parameters.add(new KeyValue("in0", roomId));
			parameters.add(new KeyValue("in1", measure));
			return this.callJsonMethod("getLastMeasurementByRoomID", parameters, MeasurementContent.class, task);
		}
		
		
		
		// Wrapped version for the web services
		//TODO
		public Object actuate(QueryContent queryContent, 
				TaskOn<Response<DeviceContent>> task) throws SecurityException{
			List<KeyValue> parameters = new ArrayList<KeyValue>();
			String jsonContent = toJsonOCiphered(queryContent);
			parameters.add(new KeyValue("in0", jsonContent));
			return this.callJsonMethod("actuate", parameters, DeviceContent.class, task);
		}
		
		//TODO change the names of the methods deleting "Wrapp"
		public Object getDeviceWrapp (QueryContent queryContent, 
				TaskOn<Response<DeviceContent>> task) throws SecurityException{
		List<KeyValue> parameters = new ArrayList<KeyValue>();
		String jsonContent = toJsonOCiphered(queryContent);
		parameters.add(new KeyValue("in0", jsonContent));
		return this.callJsonMethod("getDevice", parameters, DeviceContent.class, task);
	}
	
		
		
//		public Object proximityAllert(String userName, 
//				boolean isApproaching, TaskOn<Response<ProximityContent>> task){
//			List<KeyValue> parameters = new ArrayList<KeyValue>();
//			parameters.add(new KeyValue("in0", userName));
//			parameters.add(new KeyValue("in1", isApproaching));
//			return this.callJsonMethod("proximityAllert", parameters, ProximityContent.class, task);
//		}
		
		public Object proximityAllert(QueryContent queryContent, 
				TaskOn<Response<QueryContent>> task) throws SecurityException{
			List<KeyValue> parameters = new ArrayList<KeyValue>();
			String jsonContent = toJsonOCiphered(queryContent);
			parameters.add(new KeyValue("in0", jsonContent));
			return this.callJsonMethod("proximityAllert", parameters, QueryContent.class, task);
		}
		
		public Object getUserMessages(QueryContent queryContent,
				TaskOn<Response<UserMessageContent>> task) throws SecurityException{
			List<KeyValue> parameters = new ArrayList<KeyValue>();
			String jsonContent = toJsonOCiphered(queryContent);
			parameters.add(new KeyValue("in0", jsonContent));
			return this.callJsonMethod("getUserMessages", parameters, UserMessageContent.class, task);
		}
		
		public Object deleteUserMessage(QueryContent queryContent,
				TaskOn<Response<UserMessageContent>> task) throws SecurityException{
			List<KeyValue> parameters = new ArrayList<KeyValue>();
			String jsonContent = toJsonOCiphered(queryContent);
			parameters.add(new KeyValue("in0", jsonContent));
			return this.callJsonMethod("deleteUserMessage", parameters, UserMessageContent.class, task);
			
		}
		
		public Object deleteAllUserMessages(QueryContent queryContent,
				TaskOn<Response<UserMessageContent>> task) throws SecurityException{
			List<KeyValue> parameters = new ArrayList<KeyValue>();
			String jsonContent = toJsonOCiphered(queryContent);
			parameters.add(new KeyValue("in0", jsonContent));
			return this.callJsonMethod("deleteAllUserMessages", parameters, UserMessageContent.class, task);
			
		}
		
		private String toJsonOCiphered(Object content) throws SecurityException{
			Gson gson = new Gson();
			String jsonContent = null;
			if (isCommunicationCiphered){
				jsonContent = MyCipher.encrypt(password, gson.toJson(content));
			} else {
				jsonContent = gson.toJson(content);
			}
			return jsonContent;
		}
		
///////	
//		public Object getStartInformation(TaskOn<Response<StartPacket>> task) {
//			return callJsonMethod("json_getStartInformation", null, StartPacket.class, task);
//		}
//	
//		public Object getRoomInformation(String roomId, TaskOn<Response<RoomsPacket>> task) {
//			List<KeyValue> parameters = new ArrayList<KeyValue>();
//			parameters.add(new KeyValue("in0", roomId));
//			return callJsonMethod("json_getRoomInformation", parameters, RoomsPacket.class, task);
//		}
//	
//		public Object getDeviceInformation(String gatewayId, int moteLocalId, TaskOn<Response<RoomsPacket>> task) {
//			List<KeyValue> parameters = new ArrayList<KeyValue>();
//			parameters.add(new KeyValue("in0", gatewayId));
//			parameters.add(new KeyValue("in1", moteLocalId));
//			return callJsonMethod("json_getsInformation", parameters, RoomsPacket.class, task);
//		}
//	
//		public Object getMeasurements(String gatewayId, int moteLocalId, Date start, Date end, int measureId, TaskOn<Response<MeasurementsPacket>> task) {
//			List<KeyValue> parameters = new ArrayList<KeyValue>();
//			parameters.add(new KeyValue("in0", gatewayId));
//			parameters.add(new KeyValue("in1", moteLocalId));
//			parameters.add(new KeyValue("in2", dateFormat.format(start)));
//			parameters.add(new KeyValue("in3", dateFormat.format(end)));
//			parameters.add(new KeyValue("in4", measureId));
//			return callJsonMethod("json_getMeasurements", parameters, MeasurementsPacket.class, task);
//		}
		
}
