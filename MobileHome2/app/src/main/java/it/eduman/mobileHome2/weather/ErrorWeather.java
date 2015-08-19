package it.eduman.mobileHome2.weather;

public class ErrorWeather {
	
	private String message;
	private String exceptionCod;
	
	public ErrorWeather (String message, String exceptionCod){
		this.message = message;
		this.exceptionCod = exceptionCod;
	}
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getExceptionCod() {
		return exceptionCod;
	}
	public void setExceptionCod(String exceptionCod) {
		this.exceptionCod = exceptionCod;
	}
	
	
}
