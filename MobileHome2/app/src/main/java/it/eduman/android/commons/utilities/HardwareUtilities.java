package it.eduman.android.commons.utilities;

import it.eduman.mobileHome2.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class HardwareUtilities {
	
//	public static boolean enableWiFi(Context context, boolean enable){
//		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//		wifiManager.setWifiEnabled(enable);
//		return isWiFiConnected(context);
//	}
	
	public static boolean isWiFiConnected(Context context) {
	    ConnectivityManager connectivityManager = (ConnectivityManager)
	        context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = null;
	    if (connectivityManager != null) {
	        networkInfo =
	            connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    }
	    return networkInfo == null ? false : networkInfo.isConnected();
	}
	
	public static boolean isMobileDataConnected(Context context) {
	    ConnectivityManager connectivityManager = (ConnectivityManager)
	        context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = null;
	    if (connectivityManager != null) {
	        networkInfo =
	            connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	    }
	    return networkInfo == null ? false : networkInfo.isConnected();
	}
	
	public static boolean isConnected(Context context){
		return (isWiFiConnected(context) || isMobileDataConnected(context));
	}

	public static boolean isConnected(Context context,
									  boolean isWifiToBeEnabled, boolean isMobileDataToBeEnabled){
		boolean result = false;
		if (isWifiToBeEnabled && isMobileDataToBeEnabled)
			result = (isWiFiConnected(context) || isMobileDataConnected(context));
		else if (isWifiToBeEnabled)
			result = isWiFiConnected(context);
		else if (isMobileDataToBeEnabled)
			result = isMobileDataConnected(context);

		return result;
	}

	public static void enableInternetConnectionAlertDialog(
			final Context context,
			final boolean isWifiToBeEnabled,
			final boolean isMobileDataToBeEnabled){

		if (!isConnected(context, isWifiToBeEnabled, isMobileDataToBeEnabled)){
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage(R.string.internetConnectionErr).setTitle(R.string.error);

			if (isWifiToBeEnabled){
				builder.setPositiveButton(R.string.preference_wifi, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						context.startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
					}
				});
			}

			if (isMobileDataToBeEnabled){
				builder.setNeutralButton(R.string.preference_data_roaming, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						context.startActivity(new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS));
					}
				});
			}


			builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// User cancelled the dialog
				}
			});

			AlertDialog dialog = builder.create();
			dialog.show();

		}

	}
	
	
	
}
