package it.eduman.smartHome.home;

public class HomeException extends Exception{

	private static final long serialVersionUID = 4627359711233036609L;

	public HomeException (String message){
		super (message);
	}
	
	public HomeException (Exception cause){
		super (cause);
	}
	public HomeException (String message, Exception cause){
		super (message, cause);
	}
	
}
