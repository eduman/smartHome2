package it.eduman.smartHome.deprecated.security;

public class SecurityException extends Exception{
	private static final long serialVersionUID = 1445963965552969089L;
	
	public SecurityException (String message){
		super(message);
	}

	public SecurityException (Exception cause){
		super(cause);
	}
	
	public SecurityException (String message, Exception cause){
		super(message, cause);
	}
}
