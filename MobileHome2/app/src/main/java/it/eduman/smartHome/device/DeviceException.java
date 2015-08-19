package it.eduman.smartHome.device;

public class DeviceException extends Exception{
	private static final long serialVersionUID = -1017106826202162489L;
	
	public DeviceException(String message){
		super (message);
	}
	
	public DeviceException(Exception cause){
		super (cause);
	}
	public DeviceException(String message, Exception cause){
		super (message, cause);
	}
}
