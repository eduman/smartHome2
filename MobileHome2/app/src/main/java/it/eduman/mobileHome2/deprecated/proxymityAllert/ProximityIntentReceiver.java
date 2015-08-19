//package it.eduman.mobileHome2.deprecated.proxymityAllert;
//
//import it.eduman.android.commons.utilities.HardwareUtilities;
//import it.eduman.android.commons.utilities.MyNotificationBuilder;
//import it.eduman.android.commons.utilities.Response;
//import it.eduman.android.commons.utilities.TaskOn;
//import it.eduman.mobileHome2.DefaultUser;
//import it.eduman.mobileHome2.MainActivity;
//import it.eduman.mobileHome2.R;
//import it.eduman.mobileHome2.communication.ProxyWebServices;
//import it.eduman.smartHome.webServices.QueryContent;
//import android.app.NotificationManager;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.Color;
//import android.location.LocationManager;
//import android.os.AsyncTask;
//import android.util.Log;
//
//public class ProximityIntentReceiver extends BroadcastReceiver {
//
//	private static final int NOTIFICATION_ID = 1000;
//
//	@Override
//	public void onReceive(Context context, Intent intent) {
//
//		String key = LocationManager.KEY_PROXIMITY_ENTERING;
//		Boolean entering = intent.getBooleanExtra(key, false);
//		StringBuilder message = new StringBuilder();
//		boolean isApproaching = false;
//		if (entering) {
//			Log.d(getClass().getSimpleName(), "entering");
//			isApproaching = truea;
//		} else {
//			Log.d(getClass().getSimpleName(), "exiting");
//			isApproaching = false;
//		}
//
//		if (HardwareUtilities.isConnected(context)){
//			//Send the event to the cloud
//			SendProximityEvent proximityEvent = new SendProximityEvent(context);
//			proximityEvent.execute(isApproaching);
//		} else {
//			message.append(context.getResources().getString(R.string.internetConnectionErr));
//			sendNotification(context, message.toString());
//		}
//
//
//	}
//
//	private void sendNotification(Context context, String message){
//		NotificationManager notificationManager =
//				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//
//		MyNotificationBuilder notificationBuilder =
//				new MyNotificationBuilder(context, MainActivity.class)
//					.setSmallIcon(R.drawable.ic_home)
//					.setContentTitle(R.string.proximityAllertTitle)
//					.setContentText(message)
//					.setWhen(System.currentTimeMillis())
//					.setAutoCancel(true)
//					.setFlagShowLights(true)
////					.setDefaults(true, true, true)
//					.setLights(Color.BLUE, 1500, 2500);
//
//		notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
//	}
//
//
//	private class SendProximityEvent extends AsyncTask<Boolean, Void,  Response<QueryContent>> {
//
//		private Context context = null;
//
//		public SendProximityEvent (Context context) {
//			this.context = context;
//		}
//
//		@SuppressWarnings("unchecked")
//		@Override
//		protected Response<QueryContent> doInBackground(Boolean... params) {
//			Response<QueryContent> responseContent;
//			try {
//				ProxyWebServices pws = new ProxyWebServices(context, true);
//				QueryContent queryContent = new QueryContent()
//						.setUsername(DefaultUser.getDefaultUsername(context))
//						.setApproaching(params[0]);
//
//				responseContent = (Response<QueryContent>) pws.proximityAllert(
//						queryContent,
//						new TaskOn<Response<QueryContent>>() {
//							@Override
//							public Object doTask(
//									Response<QueryContent> parameter) {
//								return parameter;
//							}
//						});
//
////				content = (Response<ProximityContent>) pws.proximityAllert(
////						DefaultUser.getDefaultUsername(context), params[0],
////						new TaskOn<Response<ProximityContent>>() {
////							@Override
////							public Object doTask(
////									Response<ProximityContent> parameter) {
////								return parameter;
////							}
////						});
//			}catch (Exception e){
//				responseContent = Response.createErrorResponse(e);
//			}
//
//			return responseContent;
//		}
//
//		@Override
//        protected void onPostExecute(Response<QueryContent> results) {
//			StringBuilder message = new StringBuilder();
//
//			if (results.isOk()){
//				if (results.getContent().isApproaching()){
//					message.append(context.getResources().getString(
//							R.string.proximityAllertApproachingHomeMessage));
//				} else {
//					message.append(context.getResources().getString(
//							R.string.proximityAllertLeavingHomeMessage));
//				}
//			} else {
//				message.append(context.getResources().getString(
//						R.string.proximityInfoNotSent));
//			}
//
//			sendNotification(context, message.toString());
//
//		}
//
//	}
//
//}
