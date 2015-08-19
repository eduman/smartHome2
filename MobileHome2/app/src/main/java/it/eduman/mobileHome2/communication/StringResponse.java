package it.eduman.mobileHome2.communication;

public class StringResponse {
	private boolean ok;
	private String body;
	
	public static StringResponse createOkResponse(String body) {
		StringResponse r = new StringResponse();
		r.ok = true;
		r.body = body;
		return r;
	}
	
	public static StringResponse createOkResponse() {
		StringResponse r = new StringResponse();
		r.ok = true;
		return r;
	}
	
	public static StringResponse createErrorResponse(String errorMessage) {
		StringResponse r = new StringResponse();
		r.ok = false;
		r.body = errorMessage;
		return r;
	}
	
	public boolean isOk() {return ok;}
	public String getBody() {return body;}

}