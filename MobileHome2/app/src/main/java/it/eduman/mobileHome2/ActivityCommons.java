package it.eduman.mobileHome2;

import it.eduman.android.commons.utilities.Response;
import it.eduman.android.commons.utilities.SoftwareUtilities;
import it.eduman.android.commons.utilities.TaskOn;
import it.eduman.mobileHome2.communication.ProxyWebServices;
//import it.eduman.mobileHome2.deprecated.proxymityAllert.ProximityAllert;
import it.eduman.smartHome.deprecated.device.DeviceContent;
import it.eduman.smartHome.deprecated.device.HardwarePinStatusContent;
import it.eduman.smartHome.deprecated.security.SecurityException;
import it.eduman.smartHome.deprecated.webServices.QueryContent;
import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceManager;
import android.view.WindowManager;

//import com.google.android.gms.maps.model.LatLng;


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
//		boolean isProximityManagerEnabled =  PreferenceManager
//				.getDefaultSharedPreferences(context)
//				.getBoolean(context.getResources().getString(R.string.preference_proximityManager_key), false);
//
//		boolean isProximityManagerMoreAccurate =  PreferenceManager
//				.getDefaultSharedPreferences(context)
//				.getBoolean(context.getResources()
//				.getString(R.string.preference_moreAccurateProximityManager_key), false);

//		if (isProximityManagerEnabled){
//			Gson gson = new Gson();
//			LatLng myHomePosition = gson.fromJson(
//					PreferenceManager.getDefaultSharedPreferences(context)
//						.getString(MobileHomeConstants.MY_HOME_LAT_LNG, null),
//						LatLng.class);
//			ProximityAllert.enableProximityAllert(context, myHomePosition, isProximityManagerMoreAccurate);
//		} else {
//			ProximityAllert.disableProximityAllert(context, isProximityManagerMoreAccurate);
//		}


	}

	public static void actuateButton (final Context context, final DeviceContent device,
			final HardwarePinStatusContent hw, final String command, ProxyWebServices proxyWebServices) throws SecurityException{
		actuateToggleButton(context, device, hw, command, false, proxyWebServices);
	}

	public static Boolean actuateToggleButton (final Context context, final DeviceContent device,
			final HardwarePinStatusContent hw, final String command, final boolean isInvertedSwitch, ProxyWebServices proxyWebServices) throws SecurityException{
		QueryContent query = new QueryContent()
				.setDeviceID(device.getDeviceID())
				.setActuator(String.valueOf(hw.getPin()))
				.setCommand(command)
				.setValue(String.valueOf(hw.getStatus()));

//		return (Boolean)proxyWebServices.actuate(device.getDeviceID(), String.valueOf(hw.getPin()), command, String.valueOf(hw.getStatus()), new TaskOn<Response<DeviceContent>>() {
				return (Boolean)proxyWebServices.actuate(query, new TaskOn<Response<DeviceContent>>() {
			@Override
			public Object doTask(Response<DeviceContent> parameter) {
				Boolean result = null;
				DeviceContent device2 = device;
				if(!parameter.isOk()){
					SoftwareUtilities.MyErrorDialogFactory(context, parameter.getErrorMessage());
				} else {
					device2.getHardwarePinStatusList().clear();
					device2.getHardwarePinStatusList().addAll(parameter.getContent().getHardwarePinStatusList());
					for (HardwarePinStatusContent newHW : device.getHardwarePinStatusList()){
						if (newHW.getPin() == hw.getPin()){
							hw.setActuationCommand(newHW.getActuationCommand())
								.setStatus(newHW.getStatus())
								.setConfiguredAs(newHW.getConfiguredAs());
							if (isInvertedSwitch)
								result = !(Integer.parseInt(newHW.getStatus()) != 0);
							else
								result =  Integer.parseInt(newHW.getStatus()) != 0;
						}
					}
				}
				return result;
			}
		});

	}

}
