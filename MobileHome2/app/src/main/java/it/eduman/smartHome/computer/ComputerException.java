package it.eduman.smartHome.computer;

public class ComputerException extends Exception{
	private static final long serialVersionUID = -1017106826202162489L;
	
	public ComputerException(String message){
		super (message);
	}
	
	public ComputerException(Exception cause){
		super (cause);
	}
	public ComputerException(String message, Exception cause){
		super (message, cause);
	}
}
