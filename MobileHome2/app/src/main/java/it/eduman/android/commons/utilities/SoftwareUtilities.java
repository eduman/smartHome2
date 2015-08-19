package it.eduman.android.commons.utilities;

import it.eduman.mobileHome2.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;


public class SoftwareUtilities {
	
	public static boolean isDebugging = true;
	
	public static void MyGenericDialogFactory(Context context, int title, int msg){
		MyGenericDialogFactory(context, context.getResources().getString(title), 
				context.getResources().getString(msg), false, null);
	}
	
	public static void MyGenericDialogFactory(Context context, int title, String msg){
		MyGenericDialogFactory(context, context.getResources().getString(title), msg, false, null);
	}
	
	public static void MyGenericDialogFactory(Context context, String title, int msg){
		MyGenericDialogFactory(context, title,  context.getResources().getString(msg), false, null);
	}
	
	public static void MyGenericDialogFactory(Context context, String title, String msg){
		MyGenericDialogFactory(context, title, msg, false, null);
	}
	
	public static void MyGenericDialogFactory(Context context, int title, int msg, 
			boolean showNegativeButton, final ActionTask action){
		MyGenericDialogFactory(context, context.getResources().getString(title), 
				context.getResources().getString(msg), showNegativeButton, action);
	}
	
	public static void MyGenericDialogFactory(Context context, int title, String msg, 
			boolean showNegativeButton, final ActionTask action){
		MyGenericDialogFactory(context, context.getResources().getString(title), msg, 
				showNegativeButton, action);
	}
	
	public static void MyGenericDialogFactory(Context context, String title, int msg, 
			boolean showNegativeButton, final ActionTask action){
		MyGenericDialogFactory(context, title,  context.getResources().getString(msg), 
				showNegativeButton, action);
	}
	
	
	public static void MyGenericDialogFactory(final Context context, String title, String msg, boolean showNegativeButton, final ActionTask action){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(msg).setTitle(title);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				if (action != null){
					action.onPositiveResponse();
				}
				
			}
		});
		
		if (showNegativeButton){
			builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					action.onNegativeResponse();
				}
			});
		}
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	public static void MyInfoDialogFactory(Context context, int msg){
		MyGenericDialogFactory(context, R.string.info, context.getResources().getString(msg), false, null);
	}
	
	public static void MyInfoDialogFactory(Context context, int msg, boolean showNegativeButton, ActionTask action){
		MyGenericDialogFactory(context, R.string.info, context.getResources().getString(msg), showNegativeButton, action);
	}
	
	public static void MyInfoDialogFactory(final Context context, String msg){
		MyGenericDialogFactory(context, R.string.info, msg, false, null);

	}
	
	public static void MyErrorDialogFactory(Context context, int msg){
		MyGenericDialogFactory(context, R.string.error, context.getResources().getString(msg), false, null);
	}
	
	public static void MyErrorDialogFactory(Context context, int msg, boolean showNegativeButton, ActionTask action){
		MyGenericDialogFactory(context, R.string.error, context.getResources().getString(msg), showNegativeButton, action);
	}
	
	public static void MyErrorDialogFactory(final Context context, String msg){
		MyGenericDialogFactory(context, R.string.error, msg, false, null);
	}
		
	public static void shortDebugToast(Context context, String msg){
		if (isDebugging) 
			shortToast(context, msg);
	}
	
	public static void shortDebugToast(Context context, int msg){
		if (isDebugging) 
			shortToast(context, msg);
	}
	
	public static void longDebugToast(Context context, String msg){
		if (isDebugging) 
			longToast(context, msg);
	}
	
	public static void longDebugToast(Context context, int msg){
		if (isDebugging) 
			longToast(context, msg);
	}
	
	public static void shortToast(Context context, String msg){
			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}
	
	public static void shortToast(Context context, int msg){
			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}
	
	public static void longToast(Context context, String msg){
			Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}
	
	public static void longToast(Context context, int msg){
			Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}

}
