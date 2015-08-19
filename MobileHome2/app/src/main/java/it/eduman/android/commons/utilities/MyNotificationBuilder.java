package it.eduman.android.commons.utilities;

import java.util.ArrayList;
import java.util.List;

import it.eduman.mobileHome2.R;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

public class MyNotificationBuilder {

	private static int PRIORITY_DEFAULT = 0x00000000;
	private static int PRIORITY_MAX = 0x00000002;
	private static int PRIORITY_MIN = 0xfffffffe;
	
	private Context context = null;
	private Class<?> activity;
	private Intent notificationIntent = null;
	private Intent deleteIntent = null;
	
	private PendingIntent contentPendingIntent = null;
	private PendingIntent deletePendingIntent = null;
//	private NotificationManager notificationManager = null;
	private PendingIntent fullScreenIntent = null;
	private boolean fullScreenIntentHighPriority = false;
	private String contentTitle = "";
	private String contentText = "";
	private int rDrawableSmallIcon = R.drawable.ic_launcher;
	private Bitmap largeIcon = null;
	private long when = System.currentTimeMillis();
	private int ledARGB = Color.WHITE;
	private int ledOnMS = 1500;
	private int ledOffMS = 1500;
	private int number = 0;
	private int priority = PRIORITY_DEFAULT;
	private Integer smallIconLevel = null;
	private Uri sound = null;
	private Integer audioStreamType;
	private NotificationCompat.Style style = null;
	private CharSequence subText = null;
	private CharSequence tickerText = null;
	private RemoteViews tickerViews = null;
	private boolean isChronometerUsed = false;
	private long[] vibratePattern = null;
	private RemoteViews remoteViews = null;
	private String contentInfo = null;
	private boolean isProgress = false;
	private int maxProgress = 0;
	private int progress= 0;
	private boolean isIndeterminateProgress = false;
	private List<NotificationAction> actions = 
			new ArrayList<MyNotificationBuilder.NotificationAction>();

	//defaults
	private int defaults = Notification.DEFAULT_ALL;

	//flags
	private boolean isAutoCancel = false;
	private boolean isOngoing = false;
	private boolean isOnlyAlertOnce = false;
	private boolean isFlagShowLights = false;
	private boolean isFlagNoClear = false;
	private boolean isFlagInsistent = false;
	private boolean isFlagHighPriority = false;
	private boolean isFlagForegroundSerive = false;
	


	public MyNotificationBuilder (Context context, Class<?> activity){
		this.context = context;
		this.activity = activity;
//		this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		this.notificationIntent = new Intent(this.context, this.activity);
		this.contentPendingIntent = PendingIntent.getActivity(this.context, 0, this.notificationIntent, 0); 
	}

	public MyNotificationBuilder setContentTitle (int title){
		setContentTitle(context.getResources().getString(title));
		return this;
	}
	
	public MyNotificationBuilder setContentTitle (String title){
		this.contentTitle = title;
		return this;
	}

	public MyNotificationBuilder setContentText (String message){
		this.contentText = message;
		return this;
	}


	public MyNotificationBuilder setContentText (int message){
		setContentText(context.getResources().getString(message));
		return this;
	}

	public MyNotificationBuilder setNotificationIntent (Context context, Class<?> activity){
		this.activity = activity;
		this.context = context;
		this.notificationIntent = new Intent(this.context,this.activity);
		this.contentPendingIntent = PendingIntent.getActivity(this.context, 0, this.notificationIntent, 0);
		return this;
	}

	public MyNotificationBuilder setContentIntent (Context context, int requestCode, Intent notificationIntent, int flags){
		this.context = context;
		this.notificationIntent = notificationIntent;
		this.contentPendingIntent = PendingIntent.getActivity(this.context, requestCode, this.notificationIntent, flags);
		return this;
	} 
	
	public MyNotificationBuilder setContentIntent (PendingIntent contentPendingIntent){
		this.contentPendingIntent = contentPendingIntent;
		return this;
	} 
	
	public MyNotificationBuilder deleteIntent (Context context, int requestCode, Intent deleteIntent, int flags){
		this.context = context;
		this.deleteIntent = deleteIntent;
		this.deletePendingIntent = PendingIntent.getActivity(this.context, requestCode, this.deleteIntent, flags);
		return this;
	} 
	
	public MyNotificationBuilder deleteIntent (PendingIntent deletePendingIntent){
		this.deletePendingIntent = deletePendingIntent;
		return this;
	}
	
	public MyNotificationBuilder setFullScreenIntent (PendingIntent intent, boolean highPriority){
		this.fullScreenIntentHighPriority = highPriority;
		this.fullScreenIntent = intent;
		return this;
	}
	
	public MyNotificationBuilder setFullScreenIntent (PendingIntent intent){
		this.fullScreenIntent = intent;
		return this;
	}


	public MyNotificationBuilder setSmallIcon (int rDrawableSmallIcon){
		this.rDrawableSmallIcon = rDrawableSmallIcon;
		return this;
	}
	
	public MyNotificationBuilder setSmallIcon (int rDrawableSmallIcon, int smallIconLevel){
		this.rDrawableSmallIcon = rDrawableSmallIcon;
		this.smallIconLevel = smallIconLevel;
		return this;
	}

	public MyNotificationBuilder setLargeIcon (Bitmap largeIcon){
		this.largeIcon = largeIcon;
		return this;
	}

	public MyNotificationBuilder setWhen (long when){
		this.when = when;
		return this;
	}

//	public MyNotificationBuilder setLedARGB (int color){
//		this.ledARGB = color;
//		return this;
//
//	}
//
//	public MyNotificationBuilder setLedOnMS (int ledOnMS){
//		this.ledOnMS = ledOnMS;
//		return this;
//	}
//
//	public MyNotificationBuilder setLedOffMS (int ledOffMS){
//		this.ledOffMS = ledOffMS;
//		return this;
//	}
	
	public MyNotificationBuilder setLights (Integer color, Integer ledOnMS, Integer ledOffMS){
		
		if (color != null)
			this.ledARGB = color;
		
		if (ledOnMS != null)
			this.ledOnMS = ledOnMS;
		
		if (ledOffMS != null)
			this.ledOffMS = ledOffMS;
			
		return this;
	}

	public MyNotificationBuilder setAutoCancel (boolean isAutoCancel){
		this.isAutoCancel = isAutoCancel;
		return this;
	}

	public MyNotificationBuilder setDefaults (boolean isDefaultVibrate, boolean isDefaultLights, boolean isDefaultSounds){
		defaults = 0;
		if (isDefaultVibrate)
			defaults |= Notification.DEFAULT_VIBRATE;

		if (isDefaultLights)
			defaults |= Notification.DEFAULT_LIGHTS;

		if(isDefaultSounds)
			defaults |= Notification.DEFAULT_SOUND;
		
		return this;
	}

	public MyNotificationBuilder setContent (RemoteViews remoteViews){
		this.remoteViews = remoteViews;
		return this;
	}
	
	public MyNotificationBuilder setContentInfo (int contentInfo){
		setContentInfo(context.getResources().getString(contentInfo));
		return this;
	}
	
	public MyNotificationBuilder setContentInfo (String contentInfo){
		this.contentInfo = contentInfo;
		return this;
	}
	
	public MyNotificationBuilder setNumber (int number){
		this.number = number;
		return this;
	}
	
	public MyNotificationBuilder setOngoing (boolean isOngoing){
		this.isOngoing = isOngoing;
		return this;
	}
	
	public MyNotificationBuilder setOnlyAlertOnce (boolean isOnlyAlertOnce) {
		this.isOnlyAlertOnce = isOnlyAlertOnce;
		return this;
	}
	
	public MyNotificationBuilder setPriority (int priority){
		
		if (priority > PRIORITY_MAX)
			this.priority = PRIORITY_MAX;
		else if (priority < PRIORITY_MIN)
			this.priority = PRIORITY_MIN;
		else 
			this.priority = priority;
		
		return this;
	}
	
	public MyNotificationBuilder setSound (Uri sound){
		this.sound = sound;
		return this;
	}
	
	public MyNotificationBuilder setSound (Uri sound, int streamType) {
		this.sound = sound;
		this.audioStreamType = streamType;
		return this;
	}
	
	public MyNotificationBuilder setStyle (NotificationCompat.Style style){
		this.style = style;
		return this;
	}
	
	public MyNotificationBuilder setSubText (CharSequence text){
		this.subText = text;
		return this;
	}
	
	public MyNotificationBuilder setSubText (int text){
		setSubText(this.context.getResources().getString(text));
		return this;
	}
	
	public MyNotificationBuilder setTicker (CharSequence tickerText, RemoteViews views){
		this.tickerText = tickerText;
		this.tickerViews = views;
		return this;
	}
	
	public MyNotificationBuilder setTicker (int tickerText, RemoteViews views){
		setTicker(this.context.getResources().getString(tickerText), views);
		return this;
	}
	
	public MyNotificationBuilder setTicker (CharSequence tickerText){
		this.tickerText = tickerText;
		return this;
	}
	
	public MyNotificationBuilder setTicker (int tickerText){
		setTicker(this.context.getResources().getString(tickerText));
		return this;
	}
	
	public MyNotificationBuilder setUsesChronometer (boolean b){
		this.isChronometerUsed = b;
		return this;
	}
	
	public MyNotificationBuilder setVibrate (long[] pattern) {
		this.vibratePattern = pattern;
		return this;
	}
	
	public MyNotificationBuilder setFlagShowLights (boolean isFlagShowLights) {
		this.isFlagShowLights = isFlagShowLights;
		return this;
	}
	
	public MyNotificationBuilder setFlagNoClear (boolean isFlagNoClear) {
		this.isFlagNoClear = isFlagShowLights;
		return this;
	}
	
	public MyNotificationBuilder setFlagInsistent (boolean isFlagInsistent) {
		this.isFlagInsistent = isFlagInsistent;
		return this;
	}
	
	public MyNotificationBuilder setFlagHighPriority (boolean isFlagHighPriority) {
		this.isFlagHighPriority = isFlagHighPriority;
		return this;
	}
	
	public MyNotificationBuilder setFlagForegroundSerive (boolean isFlagForegroundSerive) {
		this.isFlagForegroundSerive = isFlagForegroundSerive;
		return this;
	}
	
	public MyNotificationBuilder setProgress (int max, int progress, boolean indeterminate){
		this.isProgress = true;
		this.maxProgress = max;
		this.progress = progress;
		this.isIndeterminateProgress = indeterminate;	
		return this;
	}
	
	public MyNotificationBuilder addAction (int icon, CharSequence title, PendingIntent intent){
		if (this.actions == null)
			this.actions = new ArrayList<MyNotificationBuilder.NotificationAction>();
		
		this.actions.add(new NotificationAction(icon, title, intent));
		return this;
	}
	
	public MyNotificationBuilder addAction (int icon, int title, PendingIntent intent){
		addAction(icon, title, intent);
		return this;
	}
	


	public Notification build(){

		Notification notification = null;

		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
			notification = createNotification();
		} else {
			notification = createDeprecatedNotification();
		}

		return notification;

//		this.notificationManager.notify(notificationID, notification);

	}


	private Notification createNotification() {
		NotificationCompat.Builder builder =  new NotificationCompat.Builder(this.context) 
		.setContentTitle(this.contentTitle)  
		.setContentText(this.contentText)
		.setWhen(this.when)
		.setLights(this.ledARGB, this.ledOnMS, this.ledOffMS)
		.setAutoCancel(isAutoCancel)
		.setOngoing(isOngoing)
		.setOnlyAlertOnce(isOnlyAlertOnce)
		.setPriority(priority)
		.setUsesChronometer(isChronometerUsed)
		.setContentIntent(this.contentPendingIntent); 


		//defaults
		builder.setDefaults(this.defaults);
		
		//flags
		
		if (remoteViews != null)
			builder.setContent(remoteViews);
		
		if (contentInfo != null)
			builder.setContentInfo(contentInfo);
		
		if (deletePendingIntent != null)
			builder.setDeleteIntent(deletePendingIntent);
		
		if (this.fullScreenIntent != null)
			builder.setFullScreenIntent(this.fullScreenIntent, this.fullScreenIntentHighPriority);

		if (largeIcon != null)
			builder.setLargeIcon(largeIcon);
		
		if (smallIconLevel != null)
			builder.setSmallIcon(this.rDrawableSmallIcon, smallIconLevel);
		else 
			builder.setSmallIcon(this.rDrawableSmallIcon);
		
		if (number > 0)
			builder.setNumber(number);
		
		if (sound != null && audioStreamType != null)
			builder.setSound(sound, audioStreamType);
		else if (sound != null)
			builder.setSound(sound);
		
		if (style != null)
			builder.setStyle(style);
		
		if (this.subText != null)
			builder.setSubText(subText);
		
		if (this.tickerText != null && this.tickerViews != null)
			builder.setTicker(tickerText, tickerViews);
		else if (this.tickerText != null)
			builder.setTicker(tickerText);
		
		if (this.vibratePattern != null)
			builder.setVibrate(vibratePattern);

		if (this.isProgress)
			builder.setProgress(this.maxProgress, this.progress, this.isIndeterminateProgress);
		
		if (this.actions.size() > 0 && this.actions != null){
			for (NotificationAction a : this.actions){
				builder.addAction(a.getIcon(), a.getTitle(), a.getIntent());
			}
		}
		
		
		return builder.build();
	}


	@SuppressWarnings("deprecation")
	private Notification createDeprecatedNotification(){

		Notification notification = new Notification();
		notification.setLatestEventInfo(
				this.context, 
				this.contentTitle,
				this.contentText, 
				this.contentPendingIntent);

		notification.icon =this.rDrawableSmallIcon;
		if (smallIconLevel != null)
			notification.iconLevel = smallIconLevel;
		
		
		notification.when = System.currentTimeMillis();

		notification.ledARGB = this.ledARGB;
		notification.ledOnMS = this.ledOnMS;
		notification.ledOffMS = this.ledOffMS;

		//defaults
		notification.defaults = this.defaults;

		//flags
		if (isAutoCancel)
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		if (isFlagShowLights)
			notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		
		if (isFlagNoClear)
			notification.flags |= Notification.FLAG_NO_CLEAR;
		
		if (isFlagInsistent)
			notification.flags |= Notification.FLAG_INSISTENT;
		
		if (isFlagHighPriority)
			notification.flags |= Notification.FLAG_HIGH_PRIORITY;
		
		if (isFlagForegroundSerive)
			notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
			
		if (isOngoing)
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
		
		if (isOnlyAlertOnce)
			notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
		
		if (deletePendingIntent != null)
			notification.deleteIntent = deletePendingIntent;
		
		if (this.fullScreenIntent != null)
			notification.fullScreenIntent = this.fullScreenIntent;
		
		if (largeIcon != null)
			notification.largeIcon = largeIcon;
		
		if (number > 0)
			notification.number = number;
		
		if (sound != null && audioStreamType != null){
			notification.sound = sound;
			notification.audioStreamType = audioStreamType;
		} else if (sound != null)
			notification.sound = sound;
		
		if (this.tickerText != null && this.tickerViews != null){
			notification.tickerText = tickerText;
			notification.tickerView = tickerViews;
		} else if (this.tickerText != null)
			notification.tickerText = tickerText;
		
		if (this.vibratePattern != null)
			notification.vibrate = vibratePattern;

		return notification;
	}
	
	
	public static class NotificationAction {
		
		private int icon; 
		private CharSequence title;
		private PendingIntent intent;
		
		public NotificationAction (int icon, CharSequence title, PendingIntent intent){
			this.icon = icon;
			this.title = title;
			this.intent = intent;
		}

		public int getIcon() {
			return icon;
		}

		public NotificationAction setIcon(int icon) {
			this.icon = icon;
			return this;
		}

		public CharSequence getTitle() {
			return title;
		}

		public NotificationAction setTitle(CharSequence title) {
			this.title = title;
			return this;
		}

		public PendingIntent getIntent() {
			return intent;
		}

		public NotificationAction setIntent(PendingIntent intent) {
			this.intent = intent;
			return this;
		}
		
		
		
	}
}
