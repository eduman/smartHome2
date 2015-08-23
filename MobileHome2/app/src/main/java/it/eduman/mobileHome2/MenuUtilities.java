package it.eduman.mobileHome2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.view.Menu;

import it.eduman.android.commons.utilities.SoftwareUtilities;

public class MenuUtilities {

	public static boolean myMenuFactory(Activity activity, Menu menu){
		// Inflate the menu; this adds items to the action bar if it is present.
		activity.getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public static boolean onMyOptionsItemSelected (Context context, int selectedItem){
		switch (selectedItem) {
//		case R.id.item_clear_memory_cache:
//			TrackYourTVseriesImageLoader.imageLoader.clearMemoryCache();
//			return true;
//		case R.id.item_clear_disc_cache:
//			TrackYourTVseriesImageLoader.imageLoader.clearDiscCache();
//			return true;
		case R.id.item_action_settings:
			context.startActivity(new Intent(context, SettingsActivity.class));
			return true;
		case R.id.item_about:
			String title = context.getResources().getString(R.string.menu_item_about) + 
				" " +  context.getResources().getString(R.string.app_name);
			String version = "";
			try {
				version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
				Log.e("", e.getMessage());
				e.printStackTrace();
			}
			String msg = context.getResources().getString(R.string.app_name) + 
					" " + version +
					"\n" + context.getResources().getString(R.string.app_developer_name) + 
					"\n" + context.getResources().getString(R.string.app_developer_email);
			SoftwareUtilities.MyGenericDialogFactory(context, title, msg);
			return true;
		case R.id.item_contact_developer:
			String subject = context.getResources().getString(R.string.app_name)
				+ " " + context.getResources().getString(R.string.send_email_subject);
			Intent email = new Intent(Intent.ACTION_SEND);
			email.putExtra(Intent.EXTRA_EMAIL, new String[]{context.getResources().getString(R.string.app_developer_email)});		  
			email.putExtra(Intent.EXTRA_SUBJECT, subject);
			email.setType("message/rfc822");
			context.startActivity(Intent.createChooser(email, context.getResources().getString(R.string.send_email_action)));
			return true;
			
		default:
			return false;
		}
	}

}
