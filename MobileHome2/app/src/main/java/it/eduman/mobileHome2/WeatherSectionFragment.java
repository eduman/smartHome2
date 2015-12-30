package it.eduman.mobileHome2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import it.eduman.android.commons.utilities.ErrorUtilities;
import it.eduman.android.commons.utilities.HardwareUtilities;
import it.eduman.android.commons.utilities.HttpConnection;
import it.eduman.android.commons.utilities.HttpConnectionException;
import it.eduman.android.commons.utilities.SoftwareUtilities;
import it.eduman.mobileHome2.commons.MobileHomeConstants;
import it.eduman.mobileHome2.commons.MobileHomeException;
import it.eduman.mobileHome2.commons.MobileHomeImageLoader;
import it.eduman.mobileHome2.weather.City;
import it.eduman.mobileHome2.weather.ErrorWeather;
import it.eduman.mobileHome2.weather.Weather;

public class WeatherSectionFragment extends MyFragment {

	private static View rootView = null;

	private static String cityName = "Torino";
	private static String cityState = "IT";
	
	private static DisplayImageOptions options;
	private SharedPreferences sharedPref;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.weather_fragment_activity,
				container, false);

//		ActivityCommons.updateAfterUserSettingsChanges(rootView.getContext());
		sharedPref = PreferenceManager.getDefaultSharedPreferences(rootView.getContext());
		options = new DisplayImageOptions.Builder()
		.showStubImage(R.drawable.ic_generic_weather)
		.showImageForEmptyUri(R.drawable.ic_generic_weather)
		.showImageOnFail(R.drawable.ic_error)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		//.displayer(new RoundedBitmapDisplayer(20))
		.build();

		cityName = sharedPref.getString(MobileHomeConstants.WEATHER_CITY_NAME, "Torino");
		cityState = sharedPref.getString(MobileHomeConstants.WEATHER_CITY_STATE, "IT");
		
		TextView country = (TextView) rootView.findViewById(R.id.weatherActivity_country);
		country.setText(cityState);

		final EditText cityEditText = (EditText) rootView.findViewById(R.id.weatherActivity_city_editText);
		cityEditText.setText(cityName);
		cityEditText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				if (s.toString().contains("\r") || s.toString().contains("\n")) {
					s = s.toString().replaceAll("[\r|\n]", "");
					cityEditText.setText(s);
					search();
		        }
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
	

		ImageButton searchCity = (ImageButton) rootView.findViewById(R.id.weatherActivity_searchWeatherButton);
		searchCity.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				search();
			}
		});

		ImageButton refreshButton = (ImageButton)rootView.findViewById(R.id.weather_fragment_refresh_button);
		refreshButton.setVisibility(View.VISIBLE);
		refreshButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				update();
			} 
		});

		ImageButton homeButton = (ImageButton)rootView.findViewById(R.id.weather_fragment_home_button);
		homeButton.setVisibility(View.VISIBLE);
		homeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((MainActivity)getActivity()).nextPage(MobileHomeConstants.MAIN_FRAGMENT_POSITION);
			} 
		});

		return rootView;
	}

	@Override
	public void onResume(){
		super.onResume();
		ActivityCommons.updateAfterUserSettingsChanges(rootView.getContext());
		update();		  
	}


	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		EditText cityEditText = (EditText) rootView.findViewById(R.id.weatherActivity_city_editText);
		TextView countryTW = (TextView) rootView.findViewById(R.id.weatherActivity_country);
		cityName = sharedPref.getString(MobileHomeConstants.WEATHER_CITY_NAME, "Torino");
		cityState = sharedPref.getString(MobileHomeConstants.WEATHER_CITY_STATE, "IT");
		if (savedInstanceState != null){
			cityEditText.setText(cityName);
			countryTW.setText(cityState);
		}
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(cityEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Save the values you need from your textview into "outState"-object
		Editor edit = sharedPref.edit();
		edit.putString(MobileHomeConstants.WEATHER_CITY_NAME, cityName);
		edit.putString(MobileHomeConstants.WEATHER_CITY_STATE, cityState);
		edit.commit();
		super.onSaveInstanceState(outState);
	}


	@Override
	public void update() {
		hideViews();
		if (MyFragment.CURRENT_VISIBLE_FRAGMENT == MobileHomeConstants.WEATHER_FRAGMENT_POSITION){
			if (HardwareUtilities.isWiFiConnected(rootView.getContext()))
				new AsyncGetWeather(rootView).execute(cityName);
			else {
				HardwareUtilities.enableInternetConnectionAlertDialog(
						rootView.getContext(), true, false);
			}

		}
	}
	
	private void search() {
		EditText cityEditText = (EditText) rootView.findViewById(R.id.weatherActivity_city_editText);
		cityName = cityEditText.getText().toString();
		Editor edit = sharedPref.edit();
		edit.putString(MobileHomeConstants.WEATHER_CITY_NAME, cityName);
		edit.commit();
		
		InputMethodManager imm = (InputMethodManager) rootView.getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(cityEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		update();
	}
	
	private void hideViews(){
		WebView weatherMap = (WebView) rootView.findViewById(R.id.weatherActivity_weatherMapWebView);
		weatherMap.setVisibility(View.INVISIBLE);

		WebView hourPlot = (WebView) rootView.findViewById(R.id.weatherActivity_hourPlotWebView);
		hourPlot.setVisibility(View.INVISIBLE);
		
		WebView dailyPlot = (WebView) rootView.findViewById(R.id.weatherActivity_dailyPlotWebView);
		dailyPlot.setVisibility(View.INVISIBLE);
		
		TextView weather = (TextView) rootView.findViewById(R.id.weatherActivity_weather);
		weather.setVisibility(View.INVISIBLE);

		TextView weatherDesc = (TextView) rootView.findViewById(R.id.weatherActivity_weatherDesc);
		weatherDesc.setVisibility(View.INVISIBLE);

		TextView temperature = (TextView) rootView.findViewById(R.id.weatherActivity_temperature);
		temperature.setVisibility(View.INVISIBLE);

		TextView temperatureMinMax = (TextView) rootView.findViewById(R.id.weatherActivity_temperatureMinMax);
		temperatureMinMax.setVisibility(View.INVISIBLE);

		TextView humidity = (TextView) rootView.findViewById(R.id.weatherActivity_humidity);
		humidity.setVisibility(View.INVISIBLE);

		//		TextView pressue = (TextView) rootView.findViewById(R.id.weatherActivity_pressue);
		//		pressue.setVisibility(View.INVISIBLE);

		ImageView weatherIcon = (ImageView) rootView.findViewById(R.id.weatherActivity_weatherIcon);
		weatherIcon.setVisibility(View.INVISIBLE);

		ImageView temperatureIcon = (ImageView) rootView.findViewById(R.id.weatherActivity_temperatureIcon);
		temperatureIcon.setVisibility(View.INVISIBLE);

		ImageView humidityIcon = (ImageView) rootView.findViewById(R.id.weatherActivity_humidityIcon);
		humidityIcon.setVisibility(View.INVISIBLE);

		//		ImageView preassureIcon = (ImageView) rootView.findViewById(R.id.weatherActivity_pressueIcon);
		//		preassureIcon.setVisibility(View.INVISIBLE);
	}


	public static class AsyncGetWeather extends AsyncTask<String, Void, String>{
		private final static String HTML_ERROR 
			= "<html><head><title>Open Weather</title></head><body><h3>ERROR</h3></body></html>";
		private final static String CURRENT_WHEATHER_DATA_URL = 
				"http://api.openweathermap.org/data/2.5/weather?q=%s&mode=json&units=metric&lang=%s&appid=2de143494c0b295cca9337e1e96b00e0";
		private final static String CURRENT_WHEATHER_ICON_URL = "http://openweathermap.org/img/w/%s.png";
		private final static String CURRENT_WEATHER_ERROR = "{\"message\":\"Error:";
		private final static String CURRENT_WEATHER_EXCEPTION = "\"exceptionCod\":";		

		private View view;
		private ProgressBar progressBar;

		public AsyncGetWeather (View view){
			this.view = view;
			progressBar = (ProgressBar) view.findViewById(R.id.weatherActivity_progressBar);	
		}

		private String urlEncoder(String toBeEncoded) throws MobileHomeException {
			try {
				return URLEncoder.encode(toBeEncoded, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new MobileHomeException(e);
			}
		}

		private String getWeatherMapHTML (float lat, float lon){
			String latStr = String.valueOf(lat);
			String lonStr = String.valueOf(lon);
			StringBuilder buf=new StringBuilder();
		    InputStream html;
			try {
				html = rootView.getContext().getAssets().open("weatherMap.html");
				BufferedReader in=
						new BufferedReader(new InputStreamReader(html));
				String str;
				while ((str=in.readLine()) != null) {
				  buf.append(str);
				}
				in.close();
			} catch (IOException e) {
				buf.append(HTML_ERROR);
			}

			return buf.toString().replace("hereTheLat", latStr).replace("hereTheLong", lonStr);
			
		}
		
		private String getHourWeatherHTML (String cityID){
			StringBuilder buf=new StringBuilder();
		    InputStream html;
			try {
				html = rootView.getContext().getAssets().open("hourWeather.html");
				BufferedReader in=
						new BufferedReader(new InputStreamReader(html));
				String str;
				while ((str=in.readLine()) != null) {
				  buf.append(str);
				}
				in.close();
			} catch (IOException e) {
				buf.append(HTML_ERROR);
			}

			return buf.toString().replace("hereTheCityID", cityID);
		}
		
		private String getDailyWeatherHTML (String cityID){
			StringBuilder buf=new StringBuilder();
		    InputStream html;
			try {
				html = rootView.getContext().getAssets().open("dailyWeather.html");
				BufferedReader in=
						new BufferedReader(new InputStreamReader(html));
				String str;
				while ((str=in.readLine()) != null) {
				  buf.append(str);
				}
				in.close();
			} catch (IOException e) {
				buf.append(HTML_ERROR);
			}

			return buf.toString().replace("hereTheCityID", cityID);
		}

		@Override
		protected void onPreExecute(){
			progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected String doInBackground(String... arg0) {
			String result = "";
			String uri;
			try {
				uri = String.format(CURRENT_WHEATHER_DATA_URL, urlEncoder(arg0[0]), Locale.getDefault().getLanguage());
				result = HttpConnection.sendGet(uri);
			} catch (MobileHomeException e) {
				ErrorWeather error = new ErrorWeather(ErrorUtilities.getExceptionMessage(e), "404");
				result = (new Gson()).toJson(error);
			} catch (HttpConnectionException e) {
				ErrorWeather error = new ErrorWeather(ErrorUtilities.getExceptionMessage(e), "404");
				result =  (new Gson()).toJson(error);
			}

			return result;
		}

		@SuppressLint("SetJavaScriptEnabled")
		@Override
		protected void onPostExecute(String wheather) {
			Gson gson = new Gson();

			if (wheather.startsWith(CURRENT_WEATHER_ERROR)){
				ErrorWeather error = gson.fromJson(wheather, ErrorWeather.class);
				SoftwareUtilities.MyErrorDialogFactory(view.getContext(), error.getMessage());
			} else if (wheather.contains(CURRENT_WEATHER_EXCEPTION)) {
				ErrorWeather error = gson.fromJson(wheather, ErrorWeather.class);
				SoftwareUtilities.MyErrorDialogFactory(view.getContext(), error.getMessage());
			}else {
				City cityWeatherObj = gson.fromJson(wheather, City.class);
				Weather weatherObj = cityWeatherObj.getWeather().get(0);


				ImageView weatherIcon = (ImageView) view.findViewById(R.id.weatherActivity_weatherIcon);
				ImageLoadingListener weatherFirstDisplayListener = new WeatherFirstDisplayListener();
				String iconUri = String.format(CURRENT_WHEATHER_ICON_URL, weatherObj.getIcon());
						
				try {
					MobileHomeImageLoader.imageLoader.displayImage(
							iconUri, 
							weatherIcon, 
							options, 
							weatherFirstDisplayListener);
				} catch (Exception e){
					Log.e("", ErrorUtilities.getExceptionDetails(e));
				}

				
				//load the hour plot
				WebView hourPlot = (WebView) rootView.findViewById(R.id.weatherActivity_hourPlotWebView);
				hourPlot.getSettings().setJavaScriptEnabled(true);
				hourPlot.getSettings().setLoadsImagesAutomatically(true);
				hourPlot.getSettings().setUseWideViewPort(true);
				hourPlot.setWebViewClient(new WebViewClient());  
				hourPlot.loadDataWithBaseURL(null, 
						getHourWeatherHTML(String.valueOf(cityWeatherObj.getId())),
						"text/html", "UTF-8", null);
				hourPlot.setVisibility(View.VISIBLE);
				
				//load the daily plot
				WebView dailyPlot = (WebView) rootView.findViewById(R.id.weatherActivity_dailyPlotWebView);
				dailyPlot.getSettings().setJavaScriptEnabled(true);
				dailyPlot.getSettings().setLoadsImagesAutomatically(true);
				dailyPlot.getSettings().setUseWideViewPort(true);
				dailyPlot.setWebViewClient(new WebViewClient());  
				dailyPlot.loadDataWithBaseURL(null, 
						getDailyWeatherHTML(String.valueOf(cityWeatherObj.getId())),
						"text/html", "UTF-8", null);
				dailyPlot.setVisibility(View.VISIBLE);
				
				//Load the map
				WebView weatherMap = (WebView) rootView.findViewById(R.id.weatherActivity_weatherMapWebView);
				weatherMap.getSettings().setJavaScriptEnabled(true);
				weatherMap.getSettings().setLoadsImagesAutomatically(true);
				weatherMap.setWebViewClient(new WebViewClient());  
				weatherMap.loadDataWithBaseURL(null, 
						getWeatherMapHTML(cityWeatherObj.getCoord().getLat(), cityWeatherObj.getCoord().getLon()),
						"text/html", "UTF-8", null);
				weatherMap.setVisibility(View.VISIBLE);


				// Set the fields
				EditText city = (EditText)view.findViewById(R.id.weatherActivity_city_editText);
				city.setText(cityWeatherObj.getName());

				TextView country = (TextView) view.findViewById(R.id.weatherActivity_country);
				country.setText(cityWeatherObj.getSys().getCountry());
				cityState = cityWeatherObj.getSys().getCountry();

				StringBuilder formatter = new StringBuilder(weatherObj.getDescription());
				formatter.setCharAt(0, Character.toUpperCase(formatter.charAt(0)));
				weatherObj.setDescription(formatter.toString());

				TextView weather = (TextView) view.findViewById(R.id.weatherActivity_weather);
				weather.setText(weatherObj.getDescription());
				weather.setVisibility(View.VISIBLE);

				TextView weatherDesc = (TextView) view.findViewById(R.id.weatherActivity_weatherDesc);
				weatherDesc.setText(weatherObj.getMain());
				weatherDesc.setVisibility(View.VISIBLE);

				TextView temperature = (TextView) view.findViewById(R.id.weatherActivity_temperature);
				temperature.setText(String.format(
						view.getResources().getString(R.string.weatherTemperatureText), 
						String.valueOf(cityWeatherObj.getMain().getTemp())));
				temperature.setVisibility(View.VISIBLE);

				TextView temperatureMinMax = (TextView) view.findViewById(R.id.weatherActivity_temperatureMinMax);
				temperatureMinMax.setText(String.format(
						view.getResources().getString(R.string.weatherTemperatureTextMaxMin), 
						String.valueOf(cityWeatherObj.getMain().getTemp_max()), 
						String.valueOf(cityWeatherObj.getMain().getTemp_min())));
				temperatureMinMax.setVisibility(View.VISIBLE);

				TextView humidity = (TextView) view.findViewById(R.id.weatherActivity_humidity);
				humidity.setText(String.format(
						view.getResources().getString(R.string.weatherHumidityText), 
						String.valueOf(cityWeatherObj.getMain().getHumidity())));
				humidity.setVisibility(View.VISIBLE);

				//				TextView pressue = (TextView) view.findViewById(R.id.weatherActivity_pressue);
				//				pressue.setText(String.format(
				//						view.getResources().getString(R.string.weatherPressueText), 
				//						String.valueOf(cityWeatherObj.getMain().getPressure())));
				//				pressue.setVisibility(View.VISIBLE);

				//				ImageView weatherIcon = (ImageView) view.findViewById(R.id.weatherActivity_weatherIcon);
				//				weatherIcon.setVisibility(View.VISIBLE);

				ImageView temperatureIcon = (ImageView) view.findViewById(R.id.weatherActivity_temperatureIcon);
				temperatureIcon.setVisibility(View.VISIBLE);

				ImageView humidityIcon = (ImageView) view.findViewById(R.id.weatherActivity_humidityIcon);
				humidityIcon.setVisibility(View.VISIBLE);

				//				ImageView preassureIcon = (ImageView) view.findViewById(R.id.weatherActivity_pressueIcon);
				//				preassureIcon.setVisibility(View.VISIBLE);

				ScrollView scrollView = (ScrollView) view.findViewById(R.id.weather_fragment_scrollView);
				scrollView.fullScroll(View.FOCUS_UP);
			}

			progressBar.setVisibility(View.INVISIBLE);
		} 

	}


	private static class WeatherFirstDisplayListener extends SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
				ImageView weatherIcon = (ImageView) view.findViewById(R.id.weatherActivity_weatherIcon);
				weatherIcon.setVisibility(View.VISIBLE);
			}
		}
	}

}
