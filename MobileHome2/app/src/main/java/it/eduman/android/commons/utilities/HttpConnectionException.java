package it.eduman.android.commons.utilities;

public class HttpConnectionException extends Exception{

	private static final long serialVersionUID = 2744835579317958253L;
	
	public HttpConnectionException (String msg){
		super(msg);
	}
	
	public HttpConnectionException (Exception cause){
		super(cause);
	}
	
	public HttpConnectionException (String msg, Exception cause){
		super(msg, cause);
	}

}
