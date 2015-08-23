package it.eduman.mobileHome2.communication;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.eduman.android.commons.utilities.KeyValue;

public class NetworkManagerWebServices {
	private static final String NAMESPACE = "http://smartHome.eduman.it";
	private SharedPreferences sharedPref;
	private Context context;
	
	public NetworkManagerWebServices(Context context){
		this.context = context;
		this.sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	private String getNetworkManagerAddress(){
		return "";
//		return this.sharedPref.getString(
//				context.getString(R.string.preference_networkManagerAddress_key), null);
	}
	
	private String getNetworkManagerPortNumber(){
		return "";
//		return this.sharedPref.getString(
//				context.getString(R.string.preference_networkMangerPortNumber_key), null);
	}
	
	private String getNetworkManagerUrl(){
		return "http://" + this.getNetworkManagerAddress() + ":" 
				+ this.getNetworkManagerPortNumber()
				+ "/axis/services/NetworkManagerApplication?wsdl";
				
	}
	
	public String getHIDsbyDescription(String description) throws WebServicesException{
		List<KeyValue> parameters = new ArrayList<KeyValue>();
		parameters.add(new KeyValue("in0", description));
		WebServicesWrapper wsWrapp = 
				new WebServicesWrapper(NAMESPACE, this.getNetworkManagerUrl());
		
		// getting first hid by default
		String webServiceReplyBody;
		try {
			webServiceReplyBody = wsWrapp.callWebServiceMethod("getHIDsbyDescription", parameters);

			if (webServiceReplyBody.startsWith("anyType{item=")) {
				webServiceReplyBody = webServiceReplyBody.substring("anyType{".length());
				webServiceReplyBody = webServiceReplyBody.substring(0, webServiceReplyBody.length() - "}".length());
				String hids[] = webServiceReplyBody.split(";");
				return hids[0].substring("item=".length());
			} else {
				throw new WebServicesException("Invalid web service response format:\n" + webServiceReplyBody);
			}
		} catch (IOException e) {
			throw new WebServicesException("IOException while getting web service response:\n" + e.getMessage());
		} catch (XmlPullParserException e) {
			throw new WebServicesException("XmlPullParserException while getting web service response:\n" + e.getMessage());
		}
	}
	
	public String getTunneledUrl(String desctription) throws WebServicesException{
		String response = this.getHIDsbyDescription(desctription);
		return "http://" + this.getNetworkManagerAddress() + ":"
		+ this.getNetworkManagerPortNumber() + "/SOAPTunneling/0/"
		+ response
		+ "/0/hola";

	}
}
