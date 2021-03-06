package it.eduman.android.commons.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class HttpConnection {

	private static final String USER_AGENT = "Mozilla/5.0";
	
	private static class HttpGetCallable implements Callable<String> {

		private String uri;
		private HashMap<String, String> headers;
		public HttpGetCallable(String uri, HashMap<String, String> headers){
			this.uri = uri;
			this.headers = headers;
		}
		
		@Override
		public String call() throws HttpConnectionException {
			try {

				StringBuilder response = new StringBuilder();

				URL obj = new URL(uri);
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();

				// optional default is GET
				con.setRequestMethod("GET");

				//add request header
				con.setRequestProperty("User-Agent", USER_AGENT);
				if (headers != null){
					for (HashMap.Entry<String, String> entry: this.headers.entrySet()){
						con.setRequestProperty(entry.getKey(), entry.getValue());
					}
				}



				int responseCode = con.getResponseCode();

				if (responseCode != 200){
					throw new HttpConnectionException(String.valueOf(responseCode) + " " + con.getResponseMessage());
				} else {
					BufferedReader in = new BufferedReader(
							new InputStreamReader(con.getInputStream()));
					String inputLine;

					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}
					in.close();
				}
				return response.toString();


			}catch (MalformedURLException e) {
				throw new HttpConnectionException(e);
			} catch (IOException e) {
				throw new HttpConnectionException(e);
			}
		}

	}

	private static class HttpPutCallable implements Callable<String> {

		private String uri;
		private HashMap<String, String> headers;
		private String parameter;
		public HttpPutCallable(String uri, String parameter, HashMap<String, String> headers){
			this.uri = uri;
			this.headers = headers;
			this.parameter = parameter;
		}

		@Override
		public String call() throws HttpConnectionException {
			try {

				StringBuilder response = new StringBuilder();

				URL obj = new URL(uri);
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();

				// optional default is GET
				con.setRequestMethod("PUT");
				con.setDoOutput(true);

				//add request header
				con.setRequestProperty("User-Agent", USER_AGENT);
				if (headers != null){
					for (HashMap.Entry<String, String> entry: this.headers.entrySet()){
						con.setRequestProperty(entry.getKey(), entry.getValue());
					}
				}

				OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream());
				osw.write(parameter);
				osw.flush();
				osw.close();

				int responseCode = con.getResponseCode();

				// code 204: just to be compliant with device connector engine
				if (responseCode != 200 && responseCode != 204){
					throw new HttpConnectionException(String.valueOf(responseCode) + " " + con.getResponseMessage());
				} else {
					BufferedReader in = new BufferedReader(
							new InputStreamReader(con.getInputStream()));
					String inputLine;

					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}
					in.close();
				}
				return response.toString();


			}catch (MalformedURLException e) {
				throw new HttpConnectionException(e);
			} catch (IOException e) {
				throw new HttpConnectionException(e);
			}
		}

	}


	public static String sendGet(String uri) throws HttpConnectionException{
		return sendGet(uri, new HashMap<String, String>());
	}

	public static String sendGet(String uri, HashMap<String, String> headers) throws HttpConnectionException{
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Callable<String> httpGet = new HttpGetCallable(uri, headers);
		Future<String> future = executor.submit(httpGet);
		try {
			return future.get();
		} catch (InterruptedException e) {
			throw new HttpConnectionException(e);
		} catch (ExecutionException e) {
			throw new HttpConnectionException(e);
		}
	}



	public static String sendPUT(String uri, String parameter, HashMap<String, String> headers) throws HttpConnectionException{
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Callable<String> httpGet = new HttpPutCallable(uri, parameter, headers);
		Future<String> future = executor.submit(httpGet);
		try {
			return future.get();
		} catch (InterruptedException e) {
			throw new HttpConnectionException(e);
		} catch (ExecutionException e) {
			throw new HttpConnectionException(e);
		}
	}

}
