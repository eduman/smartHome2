//package it.eduman.mobileHome2.deprecated.proxymityAllert;
//
//import it.eduman.android.commons.utilities.SoftwareUtilities;
//import android.app.PendingIntent;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.location.LocationManager;
//
//import com.google.android.gms.maps.model.LatLng;
//
//public class ProximityAllert {
//
//	public static final long POINT_RADIUS = 50; // in Meters
//	public static final long PROX_ALERT_EXPIRATION = -1; // will not expire
//	public static final String PROX_ALERT_INTENT = "it.eduman.mobileHome2.proximityAllert";
//	private static final int intentID = 0;
//	private static final Intent intent = new Intent(PROX_ALERT_INTENT);
//	private static PendingIntent checkedProximityIntent = null;
//
//	public static void registerReceiver(Context context){
//		IntentFilter filter = new IntentFilter(ProximityAllert.PROX_ALERT_INTENT);
//		context.registerReceiver(new ProximityIntentReceiver(), filter);
//	}
//
//
//	public static void enableProximityAllert(Context context, LatLng position, boolean isMassiveChecking){
//
//		if (isMassiveChecking)
//			enableProximityUncheckedAllert(context, position);
//		else
//			enableCheckedProximityAllert(context, position);
//
//	}
//
//	public static void disableProximityAllert(Context context, boolean isMassiveChecking){
//		if (isMassiveChecking)
//			disableUncheckedProximityAllert(context);
//		else
//			disableCheckedProximityAllert(context);
//	}
//
//
//
//	// This version checks if the intent is already pending it limited the number of event generated
//	// On bottom the version without check
//
//	private static void enableCheckedProximityAllert(Context context, LatLng position){
//		if (position != null){
//
//			if (checkedProximityIntent == null) {
//				LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//				checkedProximityIntent = PendingIntent.getBroadcast(context, intentID, intent, 0);
//				locationManager.addProximityAlert(
//						position.latitude,
//						position.longitude,
//						POINT_RADIUS, PROX_ALERT_EXPIRATION,
//						checkedProximityIntent);
//
//				SoftwareUtilities.shortDebugToast(context, "Enabling the proxymity allert");
//			}
//
//		} else {
//			SoftwareUtilities.shortDebugToast(context, "I cannot enable the proxymity allert");
//		}
//	}
//
//	private static void disableCheckedProximityAllert(Context context){
////		Intent intent = new Intent(PROX_ALERT_INTENT);
//		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//		if (checkedProximityIntent != null) {
//			locationManager.removeProximityAlert(checkedProximityIntent);
//			checkedProximityIntent = null;
//		SoftwareUtilities.shortDebugToast(context, "Disabling the proxymity allert");
//		}
//	}
//
//
//
//
//	// This version does not check if the intent is already pending it can cause a lot of event
//	private static void enableProximityUncheckedAllert(Context context, LatLng position){
//		if (position != null){
////			Intent intent = new Intent(PROX_ALERT_INTENT);
//			LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//			PendingIntent proximityIntentMassive = PendingIntent.getBroadcast(context, intentID, intent, 0);
//			locationManager.addProximityAlert(
//					position.latitude,
//					position.longitude,
//					POINT_RADIUS, PROX_ALERT_EXPIRATION,
//					proximityIntentMassive);
//
//			SoftwareUtilities.shortDebugToast(context, "Enabling the more accurate proxymity allert");
//
//		} else {
//			SoftwareUtilities.shortDebugToast(context, "I cannot enable the more accurate proxymity allert");
//		}
//	}
//
//	private static void disableUncheckedProximityAllert(Context context){
////		Intent intent = new Intent(PROX_ALERT_INTENT);
//		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//
//		PendingIntent proximityIntentMassive = PendingIntent.getBroadcast(context, intentID , intent, 0 /*PendingIntent.FLAG_NO_CREATE*/);
//		locationManager.removeProximityAlert(proximityIntentMassive);
//
//		SoftwareUtilities.shortDebugToast(context, "Disabling the more accurate proxymity allert");
//	}
//
//
//
//
//}
//
