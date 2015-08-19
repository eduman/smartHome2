package it.eduman.mobileHome2.commons;

public class MobileHomeException extends Exception{

	private static final long serialVersionUID = 2744835579317958253L;
	
	public MobileHomeException (String msg){
		super(msg);
	}
	
	public MobileHomeException (Exception cause){
		super(cause);
	}
	
	public MobileHomeException (String msg, Exception cause){
		super(msg, cause);
	}

}
