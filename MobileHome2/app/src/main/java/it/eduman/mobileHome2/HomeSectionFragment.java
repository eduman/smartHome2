package it.eduman.mobileHome2;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.eduman.android.commons.utilities.ActionTask;
import it.eduman.android.commons.utilities.ErrorUtilities;
import it.eduman.android.commons.utilities.HardwareUtilities;
import it.eduman.android.commons.utilities.HttpConnection;
import it.eduman.android.commons.utilities.SoftwareUtilities;
import it.eduman.mobileHome2.commons.MobileHomeConstants;
import it.eduman.smartHome.Constants;
import it.eduman.smartHome.HomeStructure.Device;
import it.eduman.smartHome.HomeStructure.DeviceProtocol;
import it.eduman.smartHome.HomeStructure.HomeStructure;
import it.eduman.smartHome.HomeStructure.Room;
import it.eduman.smartHome.IoTDevice.Function;
import it.eduman.smartHome.IoTDevice.IoTDevice;


public class HomeSectionFragment extends MyFragment implements AdapterView.OnItemSelectedListener{

	private HomeStructure home;
	private int roomSpinnerPosition = 0;
	private static View rootView = null;
	private SharedPreferences sharedPref;
	private HashMap<String, TableRow> deviceTableRows = new HashMap<>();


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
				((MainActivity) getActivity()).nextPage(MobileHomeConstants.MAIN_FRAGMENT_POSITION);
			}
		});

		Button turnoffButton = (Button)rootView.findViewById(R.id.home_fragment_switchoff_button);
		turnoffButton.setVisibility(View.VISIBLE);
		turnoffButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SoftwareUtilities.MyInfoDialogFactory(rootView.getContext(), R.string.turn_off_message, true, new ActionTask() {
					@Override
					public void onPositiveResponse() {
						//TODO send MQTT message
						SoftwareUtilities.MyInfoDialogFactory(rootView.getContext(), "TODO");
					}

					@Override
					public void onNegativeResponse() {

					}

					@Override
					public void onNeutralResponse() {

					}
				});
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
				roomSpinner.setSelection(this.roomSpinnerPosition);
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
				String roomSpinnerString = parent.getItemAtPosition(position).toString().replace(")", "");
				String roomID = roomSpinnerString.substring(roomSpinnerString.lastIndexOf("(ID: ") + "(ID: ".length(), roomSpinnerString.length());
				this.roomSpinnerPosition = position;
				edit.putInt(MobileHomeConstants.HOME_ROOMS_SPINNER_POSITION, this.roomSpinnerPosition);
				edit.commit();
				List<Room> rooms = home.getRooms();
				boolean found = false;
				for (int i = 0; i < rooms.size() && !found; i++) {
					if (rooms.get(i).getRoomID().equalsIgnoreCase(roomID)){
						found = true;
						TableLayout tableLayout = (TableLayout) rootView.findViewById(R.id.home_fragments_devicesTableLayout);
						tableLayout.removeAllViews();

						Room r = rooms.get(i);

						for(Device device : r.getDevices()) {
							if (device.getProtocol().size() > 0) {
								RetrieveDevice retrieveDevice = new RetrieveDevice(device.getDeviceID()+ device.getType());
								retrieveDevice.execute(device.getProtocol().get(0));

								//TODO
								try {
									Thread.sleep(500);                 //1000 milliseconds is one second.
								} catch(InterruptedException ex) {
									Thread.currentThread().interrupt();
								}
							}
						}

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
				new ArrayAdapter<>(rootView.getContext(), android.R.layout.simple_spinner_item);
		if (home != null) {
			List<Room> rooms = home.getRooms();
			for (Room r : rooms)
				adapter.add(r.getDescription() + " (ID: " + r.getRoomID()+ ")");
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			roomsSpinner.setAdapter(adapter);
			try {
				roomsSpinner.setSelection(this.roomSpinnerPosition);
			} catch (Exception e){
				this.roomSpinnerPosition = 0;
				roomsSpinner.setSelection(this.roomSpinnerPosition);
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

	public void printDeviceSettings (final IoTDevice device, TableLayout mainTableLayout) {

		int index = 0;
		boolean isSwitch = false, isIswitch = false, isButton = false;
		List<Function> sensorsList = new ArrayList<>();


		TableRow tableRow = new TableRow(rootView.getContext());
		tableRow.setLayoutParams(new TableRow.LayoutParams(
				TableRow.LayoutParams.MATCH_PARENT,
				TableRow.LayoutParams.MATCH_PARENT));
		mainTableLayout.addView(tableRow);

		TextView textView = new TextView(rootView.getContext());
		textView.setId(index++);
//		textView.setText(device.getDescription());
//		tableRow.addView(textView);

		if (device.getType().equalsIgnoreCase(Constants.ProtocoloWS.vlc.toString())) {
			textView = new TextView(rootView.getContext());
			textView.setId(index++);
			textView.setText(rootView.getContext().getString(R.string.webcamTextView));
			textView.setTextSize(18);
			textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
			textView.setLayoutParams(new TableRow.LayoutParams(
					TableRow.LayoutParams.MATCH_PARENT,
					TableRow.LayoutParams.MATCH_PARENT));
			TableRow tableRow2 = new TableRow(rootView.getContext());
			tableRow2.setLayoutParams(new TableRow.LayoutParams(
					TableRow.LayoutParams.MATCH_PARENT,
					TableRow.LayoutParams.MATCH_PARENT));
			tableRow2.addView(textView);

			Button button = new Button(rootView.getContext(), null, R.drawable.my_button_blue);
			button.setId(index++);
			button.setLayoutParams(new TableRow.LayoutParams(
					100,
					TableRow.LayoutParams.WRAP_CONTENT));
			button.setText(R.string.runButton);
			button.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
			button.setTextColor(Color.WHITE);
			button.setBackgroundResource(R.drawable.my_button_blue);

			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					SoftwareUtilities.MyInfoDialogFactory(
							rootView.getContext(),
							String.format(rootView.getContext().getString(R.string.streemWebcamTextView), device.getIp()));
				}
			});
			tableRow2.addView(button);
			mainTableLayout.addView(tableRow2);

		} else {

			for (final Function function : device.getFunctions()) {
				isSwitch = false;
				isIswitch = false;
				isButton = false;
				if (function.getConfiguredAs().equalsIgnoreCase(
						Constants.ActuatorAndSensorProperties.Sensor.toString()))
					sensorsList.add(function);
				if (function.getConfiguredAs().equalsIgnoreCase(
						Constants.ActuatorAndSensorProperties.Button.toString()))
					isButton = true;
				if (function.getConfiguredAs().equalsIgnoreCase(
						Constants.ActuatorAndSensorProperties.Switch.toString()))
					isSwitch = true;

				if (function.getConfiguredAs().equalsIgnoreCase(
						Constants.ActuatorAndSensorProperties.Iswitch.toString()))
					isIswitch = true;


				if (isSwitch || isIswitch || isButton) {


					Button genericButton = new Button(rootView.getContext());

					if (isSwitch || isIswitch) {
						ToggleButton onOff = new ToggleButton(rootView.getContext(), null, R.drawable.my_button_blue);
						onOff.setId(index++);
						onOff.setTextOff("Off");
						onOff.setTextOn("On");
						onOff.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
						onOff.setTextColor(Color.WHITE);
						onOff.setLayoutParams(new TableRow.LayoutParams(
								100,
								TableRow.LayoutParams.WRAP_CONTENT));
						onOff.setChecked(Integer.parseInt(function.getStatus()) != 0);
						onOff.setBackgroundResource(R.drawable.my_button_blue);

						onOff.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								if (HardwareUtilities.isWiFiConnected(rootView.getContext())) {
									RetrieveDevice rc = new RetrieveDevice(device.getIp() + device.getType());
									rc.execute(function.getWs());
								} else {
									HardwareUtilities.enableInternetConnectionAlertDialog(
											rootView.getContext(), true, false);
								}
							}
						});

						genericButton = onOff;
					} else if (isButton) {
						Button button = new Button(rootView.getContext(), null, R.drawable.my_button_blue);
						button.setId(index++);
						button.setLayoutParams(new TableRow.LayoutParams(
								100,
								TableRow.LayoutParams.WRAP_CONTENT));
						button.setText(R.string.runButton);
						button.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
						button.setTextColor(Color.WHITE);
						button.setBackgroundResource(R.drawable.my_button_blue);

						button.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								if (HardwareUtilities.isWiFiConnected(rootView.getContext())) {
									RetrieveDevice rc = new RetrieveDevice(device.getIp() + device.getType());
									rc.execute(device);
								} else {
									HardwareUtilities.enableInternetConnectionAlertDialog(
											rootView.getContext(), true, false);
								}
							}
						});

						genericButton = button;
					}


					SpannableString spanString = new SpannableString(function.getType());
					spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
					TextView textView2 = new TextView(rootView.getContext());
					textView2.setId(index++);
					textView2.setText(function.getType().replace("_", " ") + "  ");
					textView2.setTextSize(18);
					textView2.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
					textView2.setLayoutParams(new TableRow.LayoutParams(
							TableRow.LayoutParams.MATCH_PARENT,
							TableRow.LayoutParams.MATCH_PARENT));

					TableRow tableRow2 = new TableRow(rootView.getContext());
					tableRow2.setLayoutParams(new TableRow.LayoutParams(
							TableRow.LayoutParams.MATCH_PARENT,
							TableRow.LayoutParams.MATCH_PARENT));
					tableRow2.addView(textView2);
					tableRow2.addView(genericButton);
					mainTableLayout.addView(tableRow2);
				}
			}


			for (Function hw : sensorsList) {
				SpannableString spanString = new SpannableString(hw.getType());
				spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);

				textView = new TextView(rootView.getContext());
				textView.setId(index++);
				textView.setText(hw.getType() + ": ");
				textView.setTextSize(18);
				textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
				textView.setLayoutParams(new TableRow.LayoutParams(
						TableRow.LayoutParams.MATCH_PARENT,
						TableRow.LayoutParams.MATCH_PARENT));
				TableRow tableRow3 = new TableRow(rootView.getContext());
				tableRow3.setLayoutParams(new TableRow.LayoutParams(
						TableRow.LayoutParams.MATCH_PARENT,
						TableRow.LayoutParams.MATCH_PARENT));
				tableRow3.addView(textView);

				textView = new TextView(rootView.getContext());
				textView.setId(index++);
				textView.setText(hw.getStatus());
				textView.setTextSize(18);
				textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
				textView.setLayoutParams(new TableRow.LayoutParams(
						TableRow.LayoutParams.MATCH_PARENT,
						TableRow.LayoutParams.MATCH_PARENT));
				tableRow3.addView(textView);
				mainTableLayout.addView(tableRow3);
			}
		}
	}


	private class RetrieveHome extends AsyncTask<String, Void, HomeStructure>{
		ProgressBar progress = (ProgressBar) rootView.findViewById(R.id.homeActivity_progressBar);
		TableLayout tableLayout = (TableLayout) rootView.findViewById(R.id.home_fragments_devicesTableLayout);
		Spinner roomsSpinner = (Spinner) rootView.findViewById(R.id.spinnerRooms);
		ArrayAdapter<CharSequence> adapter;
		private String errors = "";

		@Override
        protected void onPreExecute() {
			this.tableLayout.removeAllViews();
			deviceTableRows.clear();
			this.progress.setVisibility(View.VISIBLE);
        }

		@Override
		protected HomeStructure doInBackground(String... params)
		{
			HomeStructure homeStructure = null;
			try {
				String response;
				HashMap<String, String> httpHeaders = new HashMap<>();
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

				tableLayout.removeAllViews();
				deviceTableRows.clear();

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

	private class RetrieveDevice extends AsyncTask<Object, Void, IoTDevice> {
		private ProgressBar progress = (ProgressBar) rootView.findViewById(R.id.homeActivity_progressBar);
		private String errors = "";
		private DeviceProtocol deviceProtocol;
		private String deviceID = "";

		public RetrieveDevice(String deviceID){
			this.deviceID = deviceID;
		}


		@Override
		protected void onPreExecute() {
			progress.setVisibility(View.VISIBLE);
		}

		@Override
		protected IoTDevice doInBackground(Object... params) {
			IoTDevice device = null;
			try {

				String response;
				HashMap<String, String> httpHeaders = new HashMap<>();
				httpHeaders.put("Content-Type", "application/json");

				if (params[0] instanceof DeviceProtocol){
					this.deviceProtocol = (DeviceProtocol)params[0];
					//TODO manage also other protocols
					if (this.deviceProtocol.getWs().equalsIgnoreCase(Constants.ProtocoloWS.rest.toString())){
						if (this.deviceProtocol.getType().equalsIgnoreCase(Constants.ProtocolType.GET.toString())){
							response = HttpConnection.sendGet(this.deviceProtocol.getUri(), httpHeaders);
							device = new Gson().fromJson(response, IoTDevice.class);
						}
					} else if (this.deviceProtocol.getWs().equalsIgnoreCase(Constants.ProtocoloWS.vlc.toString())) {
						device = new IoTDevice();
						device.setType(Constants.ProtocoloWS.vlc.toString());
						device.setIp(this.deviceID.replace(Constants.DeviceType.vlc.toString(), ""));
					}

				} else if (params[0] instanceof String){

					response = HttpConnection.sendGet((String)params[0], httpHeaders);
					device = new Gson().fromJson(response, IoTDevice.class);
				}

			} catch (Exception e) {
				this.errors += ErrorUtilities.getExceptionMessage(e);
			}

			return device;
		}

		@Override
		protected void onPostExecute(IoTDevice results) {
			TableRow tableRow;
			TableLayout tableLayout = (TableLayout) rootView.findViewById(R.id.home_fragments_devicesTableLayout);

			if (deviceTableRows.containsKey(this.deviceID)){
				tableRow = deviceTableRows.get(this.deviceID);
				tableRow.removeAllViews();
//				tableLayout.removeView(tableRow);
			} else {

				tableRow = new TableRow(rootView.getContext());
				tableRow.setLayoutParams(new TableRow.LayoutParams(
						TableRow.LayoutParams.MATCH_PARENT,
						TableRow.LayoutParams.MATCH_PARENT));
				tableLayout.addView(tableRow);
				deviceTableRows.put(this.deviceID, tableRow);

			}

			if (results == null) {
				try {
					TextView textView = new TextView(rootView.getContext());
					textView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
							TableRow.LayoutParams.MATCH_PARENT));
					textView.setTextAppearance(rootView.getContext(), android.R.style.TextAppearance_Medium);
					textView.setText(rootView.getContext().getString(R.string.error) + ": " + this.errors);
					textView.setSingleLine(false);
					textView.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
					tableRow.addView(textView);

				} catch (Exception e) {
					Log.e("Error", e.getMessage());
				}
			} else {
				TableLayout deviceTableLayout = new TableLayout(rootView.getContext());
				deviceTableLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
						TableRow.LayoutParams.MATCH_PARENT));
				tableRow.addView(deviceTableLayout);
				printDeviceSettings(results, deviceTableLayout);
			}
			progress.setVisibility(View.INVISIBLE);

		}
	}

}
