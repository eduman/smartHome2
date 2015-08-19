package it.eduman.android.commons.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class HttpConnection {

	private static final String USER_AGENT = "Mozilla/5.0";
	
	private static class HttpGetCallable implements Callable<String> {

		private String uri;
		public HttpGetCallable(String uri){
			this.uri = uri;
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

				int responseCode = con.getResponseCode();

				if (responseCode != 200){
					throw new HttpConnectionException(con.getResponseMessage());
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
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Callable<String> httpGet = new HttpGetCallable(uri);
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
