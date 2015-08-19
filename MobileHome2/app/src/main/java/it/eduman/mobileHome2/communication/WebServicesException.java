package it.eduman.mobileHome2.communication;

public class WebServicesException extends Exception{
	private static final long serialVersionUID = 8979350459069411593L;
	
	public WebServicesException (String message){
		super (message);
	}
	
	public WebServicesException (Exception cause){
		super (cause);
	}
	
	public WebServicesException (String message, Exception cause){
		super (message, cause);
	}

}
