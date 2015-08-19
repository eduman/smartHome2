package it.eduman.android.commons.utilities;

public class ErrorUtilities {

	private static String stackTraceToString(StackTraceElement[] stackTrace) {
		StringBuilder text = new StringBuilder("");
		for (int i=0; i<stackTrace.length; i++) {
			if (!text.equals("")) text.append("\n");
			text.append("\tat " + stackTrace[i]);
		}
		return text.toString();
	}

	public static String getExceptionDetails(Exception e) {
		return e.getClass().getName() + ": " + e.getMessage() + "\n" + ErrorUtilities.stackTraceToString(e.getStackTrace());
	}
	
	public static String getExceptionMessage(Exception e) {
		return e.getClass().getName() + ": " + e.getMessage();
	}
}
