package it.eduman.mobileHome2;

import android.content.Context;
import android.preference.PreferenceManager;

import it.eduman.android.commons.utilities.SoftwareUtilities;

public class Debug {
	public static void setPrintDebugMSG(Context context){
		SoftwareUtilities.isDebugging =
				PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(context.getResources().getString(R.string.preference_debug_key), false);
	}

}
