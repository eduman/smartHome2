package it.eduman.mobileHome2;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;

import it.eduman.android.commons.utilities.ErrorUtilities;
import it.eduman.android.commons.utilities.HardwareUtilities;
import it.eduman.android.commons.utilities.HttpConnection;
import it.eduman.android.commons.utilities.SoftwareUtilities;
import it.eduman.mobileHome2.commons.MobileHomeConstants;
import it.eduman.smartHome.HomeStructure.HomeStructure;
import it.eduman.smartHome.HomeStructure.Room;


public class HomeSectionFragment extends MyFragment implements AdapterView.OnItemSelectedListener{

	private HomeStructure home;
	private int roomSpinnerPosition = 0;
	private static View rootView = null;
	private SharedPreferences sharedPref;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.home_fragment_activity, container, false);
		sharedPref = PreferenceManager.getDefaultSharedPreferences(rootView.getContext());


		ImageButton refreshButton = (ImageButton)rootView.findViewById(R.id.home_fragment_refresh_button);
		refreshButton.setVisibility(View.VISIBLE);
		refreshButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				update();
			}
		});

		ImageButton homeButton = (ImageButton)rootView.findViewById(R.id.home_fragment_home_button);
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

//	@Override
//	public void setUserVisibleHint(boolean visible) {
//		super.setUserVisibleHint(visible);
//		if (visible && isResumed()) {
//			update();
//		}
//	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState != null){
			try {
				this.roomSpinnerPosition = sharedPref.getInt(MobileHomeConstants.HOME_ROOMS_SPINNER_POSITION, 0);
			} catch (NullPointerException e){
				this.roomSpinnerPosition = 0;
			}

//			try {
//				this.deviceSpinnerPosition = sharedPref.getInt(
//						MobileHomeConstants.HOME_DEVICES_SPINNER_POSITION + this.roomSpinnerPosition, 0);
//			} catch (NullPointerException e){
//				this.deviceSpinnerPosition = 0;
//			}

			Spinner roomSpinner = (Spinner) rootView.findViewById(R.id.spinnerRooms);
			try {
				if (roomSpinner != null)
					{ roomSpinner.setSelection(this.roomSpinnerPosition); }
			} catch (Exception e){
				this.roomSpinnerPosition = 0;
				if (roomSpinner != null)
					{ roomSpinner.setSelection(this.roomSpinnerPosition); }
			}
		}
	}


	@Override
	public void onStop(){
		super.onStop();
	}



	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Save the values you need from your textview into "outState"-object
		outState.putInt(MobileHomeConstants.HOME_ROOMS_SPINNER_POSITION, this.roomSpinnerPosition);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
		Editor edit = sharedPref.edit();
		switch (parent.getId()) {
			case R.id.spinnerRooms:
				String roomSpinnerString = (String)parent.getItemAtPosition(position).toString().replace(")", "");
				String roomID = roomSpinnerString.substring(roomSpinnerString.lastIndexOf("(ID: ") + "(ID: ".length(), roomSpinnerString.length());
				this.roomSpinnerPosition = position;
				edit.putInt(MobileHomeConstants.HOME_ROOMS_SPINNER_POSITION, this.roomSpinnerPosition);
				edit.commit();
				List<Room> rooms = home.getRooms();
				boolean found = false;
				for (int i = 0; i < rooms.size() && !found; i++) {
					if (rooms.get(i).getRoomID().equalsIgnoreCase(roomID)){
						found = true;
					}
				}

				break;
		}

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
	}

	@Override
	public void update(){
		if (MyFragment.CURRENT_VISIBLE_FRAGMENT == MobileHomeConstants.HOME_FRAGMENT_POSITION){
			if (this.sharedPref == null)
				this.sharedPref = PreferenceManager.getDefaultSharedPreferences(rootView.getContext());
			String homeServiceProvider = this.sharedPref.getString(
					rootView.getResources().getString(R.string.preference_home_service_provider_key), null);
			if (homeServiceProvider == null){
				SoftwareUtilities.MyErrorDialogFactory(rootView.getContext(), R.string.setHomeInfo);
			} else {
				HardwareUtilities.enableInternetConnectionAlertDialog(rootView.getContext(), true, false);
				if (HardwareUtilities.isWiFiConnected(rootView.getContext())){
					RetrieveHome rh = new RetrieveHome();
					rh.execute(homeServiceProvider);
				}
			}

		}
	}

	protected void showInfo(){
		if (home != null){
			TextView homeText = (TextView) rootView.findViewById(R.id.homeID);
			homeText.setText(home.getDescription() + " (ID: "+ home.getHomeID() + ")");
			populateRoomSpinner();
		} else {
			SoftwareUtilities.MyErrorDialogFactory(rootView.getContext(), R.string.noHomeInfo);
		}
	}

	protected void populateRoomSpinner(){
		Spinner roomsSpinner = (Spinner)rootView.findViewById(R.id.spinnerRooms);
		ArrayAdapter<CharSequence> adapter =
				new ArrayAdapter<CharSequence>(rootView.getContext(), android.R.layout.simple_spinner_item);
		if (home != null) {
			List<Room> rooms = home.getRooms();
			for (Room r : rooms)
				adapter.add(r.getDescription() + " (ID: " + r.getRoomID()+ ")");
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			roomsSpinner.setAdapter(adapter);
			try {
				if (roomsSpinner != null)
					{ roomsSpinner.setSelection(this.roomSpinnerPosition); }
			} catch (Exception e){
				this.roomSpinnerPosition = 0;
				if (roomsSpinner != null)
					{ roomsSpinner.setSelection(this.roomSpinnerPosition); }
			}
			roomsSpinner.setOnItemSelectedListener(this);
		} else {
			SoftwareUtilities.MyErrorDialogFactory(rootView.getContext(), R.string.noRoomsInfo);
		}
	}
//
//	protected void populateDeviceSpinner(String roomID){
//		TableLayout tableLayout = (TableLayout) rootView.findViewById(R.id.tableLayout);
//		tableLayout.removeAllViews();
//		Spinner deviceSpinner = (Spinner)rootView.findViewById(R.id.spinnerDevices);
//		ArrayAdapter<CharSequence> adapter =
//				new ArrayAdapter<CharSequence>(rootView.getContext(), android.R.layout.simple_spinner_item);
//		if (home != null && home.getRoomsMap().containsKey(roomID)) {
//			HashMap<String, DeviceContent> devicesMap = home.getRoomsMap().get(roomID).getDevicesMap();
//			for (DeviceContent m : devicesMap.values())
//				adapter.add(m.getDescription() + " (ID: " + m.getDeviceID()+ ")");
//			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//			deviceSpinner.setAdapter(adapter);
//			try {
//				if (deviceSpinner != null)
//					{ deviceSpinner.setSelection(this.deviceSpinnerPosition); }
//			} catch (Exception e){
//				this.deviceSpinnerPosition = 0;
//				if (deviceSpinner != null)
//					{ deviceSpinner.setSelection(this.deviceSpinnerPosition); }
//
//			}
//			deviceSpinner.setOnItemSelectedListener(this);
//		} else {
//			SoftwareUtilities.MyErrorDialogFactory(rootView.getContext(), R.string.noDevicesInfo);
//		}
//	}
//
//	public void printDeviceSettings (final DeviceContent device, final Context context,
//			final TableLayout tableLayout,final ProxyWebServices proxyWebServices){
//
//		int index = 0;
//		boolean isSwitch = false, isIswitch = false, isButton = false;
//		List<HardwarePinStatusContent> sensorsList = new ArrayList<HardwarePinStatusContent>();
//
//		for(final HardwarePinStatusContent hw : device.getHardwarePinStatusList()){
//			isSwitch = false;
//			isIswitch = false;
//			isButton = false;
//			if (hw.getConfiguredAs().equalsIgnoreCase(
//					DeviceConstants.ActuatorAndSensorProperties.Sensor.toString()))
//				sensorsList.add(hw);
//			if (hw.getConfiguredAs().equalsIgnoreCase(
//					DeviceConstants.ActuatorAndSensorProperties.Button.toString()))
//				isButton = true;
//			if (hw.getConfiguredAs().equalsIgnoreCase(
//					DeviceConstants.ActuatorAndSensorProperties.Switch.toString()))
//				isSwitch = true;
//
//			if (hw.getConfiguredAs().equalsIgnoreCase(
//					DeviceConstants.ActuatorAndSensorProperties.Iswitch.toString()))
//				isIswitch = true;
//
//			if (isSwitch || isIswitch || isButton){
//				TableRow tableRow = new TableRow(context);
//				tableRow.setLayoutParams(new LayoutParams(
//						LayoutParams.MATCH_PARENT,
//						LayoutParams.MATCH_PARENT));
//				TextView textView = new TextView(context);
//				Button genericButton = new Button(context);
//
//				if (isSwitch || isIswitch){
//					ToggleButton onOff = new ToggleButton(context, null, R.drawable.my_button_blue);
//					onOff.setId(index++);
//					onOff.setTextOff("Off");
//					onOff.setTextOn("On");
//					onOff.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
//					onOff.setTextColor(Color.WHITE);
//					onOff.setLayoutParams(new LayoutParams(
//							100,
//							LayoutParams.WRAP_CONTENT));
//					onOff.setChecked(Integer.parseInt(hw.getStatus()) != 0);
//					onOff.setBackgroundResource(R.drawable.my_button_blue);
//					onOff.setOnClickListener(new View.OnClickListener() {
//						@Override
//						public void onClick(View v) {
//							ToggleButton onOff = (ToggleButton) v;
//							if (HardwareUtilities.isWiFiConnected(context)){
//								String command = hw.getActuationCommand();
//								try{
//									Boolean newIsOn = ActivityCommons.actuateToggleButton(context, device, hw, command, false, proxyWebServices);
//									onOff.setChecked(Integer.parseInt(hw.getStatus()) != 0);
//									if (newIsOn != null) onOff.setChecked(newIsOn);
//									else onOff.setChecked(Integer.parseInt(hw.getStatus()) != 0);
//								} catch (it.eduman.smartHome.deprecated.security.SecurityException e) {
//									SoftwareUtilities.MyErrorDialogFactory(
//										rootView.getContext(),
//										ErrorUtilities.getExceptionMessage(e));
//							}
//							} else {
//								HardwareUtilities.enableInternetConnectionAlertDialog(
//										context, true, false);
//							}
//						}
//					});
//					genericButton = onOff;
//				} else if (isButton){
//					Button button = new Button(context, null, R.drawable.my_button_blue);
//					button.setId(index++);
//					button.setLayoutParams(new LayoutParams(
//							100,
//							LayoutParams.WRAP_CONTENT));
//					button.setText(R.string.runButton);
//					button.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
//					button.setTextColor(Color.WHITE);
//					button.setBackgroundResource(R.drawable.my_button_blue);
//					button.setOnClickListener(new View.OnClickListener() {
//						@Override
//						public void onClick(View v) {
//							if (HardwareUtilities.isWiFiConnected(context)){
//								String command = hw.getActuationCommand();
//								try {
//									ActivityCommons.actuateButton(context, device, hw, command, proxyWebServices);
//								} catch (it.eduman.smartHome.deprecated.security.SecurityException e) {
//									SoftwareUtilities.MyErrorDialogFactory(
//											rootView.getContext(),
//											ErrorUtilities.getExceptionMessage(e));
//								}
//							} else {
//								HardwareUtilities.enableInternetConnectionAlertDialog(
//									context, true, false);
//								}
//						}
//					});
//					genericButton = button;
//				}
//
//
//				SpannableString spanString = new SpannableString(hw.getType());
//				spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
//				textView.setId(index++);
//				textView.setText(hw.getType().replace("_", " ") + "  ");
//				textView.setTextSize(18);
//				textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
//				textView.setLayoutParams(new LayoutParams(
//						LayoutParams.MATCH_PARENT,
//						LayoutParams.MATCH_PARENT));
//				tableRow.addView(textView);
//				tableRow.addView(genericButton);
//				tableLayout.addView(tableRow);
//			}
//		}
//
//
//		for (HardwarePinStatusContent hw : sensorsList){
//			TableRow tableRow = new TableRow(context);
//			tableRow.setLayoutParams(new LayoutParams(
//					LayoutParams.MATCH_PARENT,
//					LayoutParams.MATCH_PARENT));
//			TextView textView;
//			SpannableString spanString = new SpannableString(hw.getType());
//			spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
//
//			textView = new TextView(context);
//			textView.setId(index++);
//			textView.setText(hw.getType() + ": ");
//			textView.setTextSize(18);
//			textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
//			textView.setLayoutParams(new LayoutParams(
//					LayoutParams.MATCH_PARENT,
//					LayoutParams.MATCH_PARENT));
//			tableRow.addView(textView);
//
//			textView = new TextView(context);
//			textView.setId(index++);
//			textView.setText(hw.getStatus());
//			textView.setTextSize(18);
//			textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
//			textView.setLayoutParams(new LayoutParams(
//					LayoutParams.MATCH_PARENT,
//					LayoutParams.MATCH_PARENT));
//			tableRow.addView(textView);
//			tableLayout.addView(tableRow);
//		}
//	}


	private class RetrieveHome extends AsyncTask<String, Void, HomeStructure>{
		ProgressBar progress = (ProgressBar) rootView.findViewById(R.id.homeActivity_progressBar);
		TableLayout tableLayout = (TableLayout) rootView.findViewById(R.id.tableLayout);
		Spinner roomsSpinner = (Spinner) rootView.findViewById(R.id.spinnerRooms);
		ArrayAdapter<CharSequence> adapter;
		private String errors = "";

		@Override
        protected void onPreExecute() {
			this.tableLayout.removeAllViews();
			this.progress.setVisibility(View.VISIBLE);
        }

		@Override
		protected HomeStructure doInBackground(String... params)
		{
			HomeStructure homeStructure = null;
			try {
				String response;
				HashMap<String, String> httpHeaders = new HashMap<String, String>();
				httpHeaders.put("Content-Type", "application/json");
				response = HttpConnection.sendGet(params[0], httpHeaders);
				homeStructure = new Gson().fromJson(response, HomeStructure.class);
			} catch (Exception e){
				this.errors += ErrorUtilities.getExceptionMessage(e);
			}

			return homeStructure;

		}

		@Override
        protected void onPostExecute(HomeStructure results) {

			if (results == null){
				TextView homeText = (TextView) rootView.findViewById(R.id.homeID);
				homeText.setText(rootView.getContext().getResources().getString(R.string.unknownHome));
				this.roomsSpinner.setAdapter(adapter);


//				SoftwareUtilities.MyErrorDialogFactory(
//						rootView.getContext(),
//						rootView.getContext().getResources().getString(R.string.startingInfoErr)
//						+ this.errors);

				tableLayout.removeAllViews();

				TableRow tableRow = new TableRow(rootView.getContext());
				tableLayout.addView(tableRow);
				TextView textView = new TextView(rootView.getContext());
				textView.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.MATCH_PARENT));
				textView.setTextAppearance(rootView.getContext(), android.R.style.TextAppearance_Medium);
				textView.setText(rootView.getContext().getString(R.string.error) + ": " + this.errors);
				textView.setSingleLine(false);
				textView.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
				tableRow.addView(textView);
			} else {
				home = results;
				showInfo();
			}

			progress.setVisibility(View.INVISIBLE);
        }

	}



//	private class RetriveDevice extends AsyncTask<String, Void, Response<DeviceContent> >{
//		ProgressBar progress = (ProgressBar) rootView.findViewById(R.id.homeActivity_progressBar);
//		TableLayout tableLayout = (TableLayout) rootView.findViewById(R.id.tableLayout);
////		Spinner deviceSpinner = (Spinner)findViewById(R.id.spinnerDevices);
////		ArrayAdapter<CharSequence> adapter;
//
//		Context context;
//
//		public RetriveDevice (Context context){
//			this.context = context;
////			this.adapter =
////					new ArrayAdapter<CharSequence>(this.context, android.R.layout.simple_spinner_item);
//
//		}
//
//		@Override
//        protected void onPreExecute() {
//			tableLayout.removeAllViews();
//			progress.setVisibility(View.VISIBLE);
//        }
//
//		@SuppressWarnings("unchecked")
//		@Override
//		protected Response<DeviceContent> doInBackground(String... params) {
//			Response<DeviceContent> device;
//			try {
//				ProxyWebServices shws = new ProxyWebServices(context, true);
//				device = (Response<DeviceContent>)shws.getDevice(params[0], new TaskOn<Response<DeviceContent>>() {
//					@Override
//					public Object doTask(Response<DeviceContent> parameter) {
//						return parameter;
//					}
//				});
//			} catch (Exception e) {
//				device = Response.createErrorResponse(e);
//			}
//			return device;
//		}
//
//		@Override
//        protected void onPostExecute(Response<DeviceContent> results) {
//			tableLayout.removeAllViews();
//			if(!results.isOk()){
////				this.deviceSpinner.setAdapter(adapter);
//				SoftwareUtilities.MyErrorDialogFactory(context, results.getErrorMessage());
//			} else {
//				printDeviceSettings(results.getContent(), context, tableLayout, new ProxyWebServices(context, true));
//			}
//			progress.setVisibility(View.INVISIBLE);
//        }
//
//	}

}
