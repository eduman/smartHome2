package it.eduman.mobileHome2;

import it.eduman.android.commons.utilities.HttpConnection;
import it.eduman.android.commons.utilities.HttpConnectionException;
import it.eduman.smartHome.IoTDevice.Function;
import it.eduman.smartHome.IoTDevice.IoTDevice;
import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceManager;
import android.view.WindowManager;

import com.google.gson.Gson;

import java.util.HashMap;



public class ActivityCommons {


	public static void checkAndSetFullScreen(Activity activity){

		boolean isFullScreen = PreferenceManager.getDefaultSharedPreferences(activity)
				.getBoolean(activity.getResources().getString(R.string.preference_fullScreen_key), false);

		if (isFullScreen){
			activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

		} else {
			activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}

	public static void checkAndSetScreenAlwaysOn (Activity activity){

		boolean isKeepScreenOn = PreferenceManager.getDefaultSharedPreferences(activity)
				.getBoolean(activity.getResources().getString(R.string.preference_keepScreenOn_key), false);

		if (isKeepScreenOn){
			activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

	}


	public static void updateAfterUserSettingsChanges(Context context){
		Debug.setPrintDebugMSG(context);
	}

	public static String actuateButton (final Context context, final IoTDevice device,
									   final Function function, final String commandWS) throws HttpConnectionException{
		return actuateGenerciButton(context, device, function, commandWS, false);
	}

	public static String actuateToggleButton (final Context context, final IoTDevice device,
									  final Function function, final String commandWS) throws HttpConnectionException{
		return actuateGenerciButton(context, device, function, commandWS, true);
	}

	protected static String actuateGenerciButton (final Context context, final IoTDevice device,
			final Function function, final String commandWS, final boolean isInvertedSwitch) throws  HttpConnectionException{

		String result = "";
		try {
			String response;
			HashMap<String, String> httpHeaders = new HashMap<String, String>();
			httpHeaders.put("Content-Type", "application/json");
			response = HttpConnection.sendGet(commandWS, httpHeaders);

			IoTDevice deviceResponse = new Gson().fromJson(response, IoTDevice.class);

			for (Function newFunc : deviceResponse.getFunctions()){
				if (newFunc.getPin() == function.getPin()){
					function.setConfiguredAs(newFunc.getConfiguredAs());
					function.setRest(newFunc.getRest());
					function.setStatus(newFunc.getStatus());
					function.setType(newFunc.getType());
					function.setUnit(newFunc.getUnit());
					function.setWs(newFunc.getWs());
					result = newFunc.getStatus();
				}
			}
		} catch (Exception e){
			throw new HttpConnectionException(e);
		}

		return result;

	}
}
