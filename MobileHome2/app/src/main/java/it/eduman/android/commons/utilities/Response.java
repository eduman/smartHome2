package it.eduman.android.commons.utilities;

public class Response< T> {
	private boolean ok = false;
	private T content = null;
	private String errorMessage = null;
	
	public static <T> Response<T> createOkResponse(T content) {
		Response<T> r = new Response<T>();
		r.ok = true;
		r.content = content;
		return r;
	}
	
	public static <T> Response<T> createOkResponse() {
		Response<T> r = new Response<T>();
		r.ok = true;
		return r;
	}
	
	public static <T> Response<T> createErrorResponse(String errorMessage) {
		Response<T> r = new Response<T>();
		r.ok = false;
		r.errorMessage = errorMessage;
		return r;
	}
	
	public static <T> Response<T> createErrorResponse(Exception exception) {
		return Response.createErrorResponse(ErrorUtilities.getExceptionDetails(exception));
	}
	
	public boolean isOk() {return ok;}
	public T getContent() {return content;}
	public String getErrorMessage() {return errorMessage;}

}

