package it.eduman.mobileHome2;


import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.WindowManager;

public class SettingsActivity extends PreferenceActivity{
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);	
		// this following line must be always after super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_menu); 	

		String[] keys = new String[]{
				getResources().getString(R.string.preference_home_service_provider_key),
				};
		
		boolean isFullScreen = PreferenceManager.getDefaultSharedPreferences(this)
				.getBoolean(getResources().getString(R.string.preference_fullScreen_key), false);
		
		if (isFullScreen){
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);  
		    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN); 

		} else {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN); 
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		
		Preference wifi = (Preference) findPreference(getResources().getString(
						R.string.preference_wifiSettings_key));
		wifi.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
				return true;
			}
		});
		
//		Preference mobile = (Preference) findPreference(getResources().getString(
//						R.string.preference_data_roamingSettings_key));
//		mobile.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//			@Override
//			public boolean onPreferenceClick(Preference preference) {
//				startActivity(new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS));
//				return true;
//			}
//		});
		
		for (String key : keys){
			final EditTextPreference etp = (EditTextPreference) findPreference(key);
			etp.setSummary(etp.getText());
			etp.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					etp.setSummary((String) newValue);
					return true;
				}
			});
		}

	}

}
