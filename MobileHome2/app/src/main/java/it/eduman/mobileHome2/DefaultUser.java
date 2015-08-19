package it.eduman.mobileHome2;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

public class DefaultUser {
	private static String defUsername = null;
			
	public static String getDefaultUsername (Context context){
		if (defUsername == null){
			defUsername = getUsernameByType(context, "com.google");
		}
		return defUsername;		
	}
	
	public static String getUsernameByType(Context context, String type){
		String username;
		AccountManager manager = AccountManager.get(context); 
		Account[] accounts = manager.getAccountsByType(type);
		username = accounts[0].name;
		return username;		
	}

}
