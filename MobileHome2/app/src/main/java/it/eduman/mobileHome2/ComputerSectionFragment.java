package it.eduman.mobileHome2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
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
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;

import it.eduman.android.commons.utilities.ErrorUtilities;
import it.eduman.android.commons.utilities.HardwareUtilities;
import it.eduman.android.commons.utilities.HttpConnection;
import it.eduman.android.commons.utilities.SoftwareUtilities;
import it.eduman.mobileHome2.commons.MobileHomeConstants;
import it.eduman.smartHome.Contants.ActuatorAndSensorProperties;
import it.eduman.smartHome.IoTDevice.ActuationCommands;
import it.eduman.smartHome.IoTDevice.Function;
import it.eduman.smartHome.IoTDevice.IoTDevice;
import it.eduman.smartHome.computer.ComputerSettings;


//import android.widget.ToggleButton;
/**
 * A dummy fragment representing a section of the app, but that simply
 * displays dummy text.
 */
public class ComputerSectionFragment extends MyFragment implements AdapterView.OnItemSelectedListener{


	private View rootView = null;
	private ComputerSettings computerSettings;
	private int computerSpinnerPosition = 0;
	private SharedPreferences sharedPref;
	private HashMap<String, ComputerSettings> computersMap = new HashMap<>();

	public ComputerSectionFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.computer_fragment_activity,
				container, false);

		sharedPref = PreferenceManager.getDefaultSharedPreferences(rootView.getContext());
		
		try {
			this.computerSpinnerPosition = sharedPref.getInt(MobileHomeConstants.COMPUTERS_SPINNER_POSITION, 0);
		} catch (NullPointerException e){
			this.computerSpinnerPosition = 0;
		}
		Spinner computerSpinner = (Spinner) rootView.findViewById(R.id.computerActivity_computerSpinner);
		try {
			computerSpinner.setSelection(this.computerSpinnerPosition);
		} catch (Exception e){
			this.computerSpinnerPosition = 0;
			computerSpinner.setSelection(this.computerSpinnerPosition);	
		}

		ImageButton configComputer = (ImageButton) rootView.findViewById(R.id.computerActivity_configComputerButton);
		configComputer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(rootView.getContext(), ComputerSettingsActivity.class);
				startActivity(intent);
			}
		});

		ImageButton refreshButton = (ImageButton)rootView.findViewById(R.id.computer_fragment_refresh_button);
		refreshButton.setVisibility(View.VISIBLE);
		refreshButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				update();
			} 
		});
		
		ImageButton homeButton = (ImageButton)rootView.findViewById(R.id.computer_fragment_home_button);
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
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState != null){
			this.computerSpinnerPosition = savedInstanceState.getInt(MobileHomeConstants.COMPUTERS_SPINNER_POSITION);
			Spinner computerSpinner = (Spinner) rootView.findViewById(R.id.computerActivity_computerSpinner);
			try {
				computerSpinner.setSelection(this.computerSpinnerPosition);
			} catch (Exception e){
				this.computerSpinnerPosition = 0;
				computerSpinner.setSelection(this.computerSpinnerPosition);	
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(MobileHomeConstants.COMPUTERS_SPINNER_POSITION, this.computerSpinnerPosition);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
		hideButtons();
		switch (parent.getId()){
		case R.id.computerActivity_computerSpinner:
			String computerSpinnerString = parent.getItemAtPosition(position).toString().replace(")", "");
			String uniqueComputerKey = computerSpinnerString.substring(computerSpinnerString.lastIndexOf("(ID: ") + "(ID: ".length(), computerSpinnerString.length());
			if (computersMap.containsKey(uniqueComputerKey)){
				this.computerSpinnerPosition = position;
				Editor edit = sharedPref.edit();
				edit.putInt(MobileHomeConstants.COMPUTERS_SPINNER_POSITION, this.computerSpinnerPosition);
				edit.commit();
				computerSettings = computersMap.get(uniqueComputerKey);
				if (HardwareUtilities.isWiFiConnected(rootView.getContext())) {
					RetrieveComputer rc = new RetrieveComputer();
					rc.execute(computerSettings.getUrl());
				} else {
					HardwareUtilities.enableInternetConnectionAlertDialog(
							rootView.getContext(), true, false);
				}
			}
			break;
		}

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	protected void showButtons (final IoTDevice device){

		boolean isSwitch = false, isButton = false;
		int buttonId = -1;

		for(final Function function : device.getFunctions()){
			buttonId = getButtonId(function.getType());

			if (buttonId != -1) {
				isSwitch = false;
				isButton = false;
				if (function.getConfiguredAs().equalsIgnoreCase(
						ActuatorAndSensorProperties.Button.toString()))
					isButton = true;
				if (function.getConfiguredAs().equalsIgnoreCase(
						ActuatorAndSensorProperties.Switch.toString()))
					isSwitch = true;

				if (isSwitch || isButton){

					if (isSwitch){
						if (function.getType().equalsIgnoreCase("VolumeMute")){
							ImageButton onOff = (ImageButton) rootView.findViewById(buttonId);
							onOff.setVisibility(View.VISIBLE);

							if (function.getStatus().equalsIgnoreCase("Muted"))
								onOff.setImageResource(R.drawable.ic_action_volume_muted);
							else
								onOff.setImageResource(R.drawable.ic_action_volume_on);

							onOff.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									if (HardwareUtilities.isWiFiConnected(rootView.getContext())){
										RetrieveComputer rc = new RetrieveComputer();
										rc.execute(function.getWs());
									} else {
										HardwareUtilities.enableInternetConnectionAlertDialog(
												rootView.getContext(), true, false);
									}
								}
							});
						}
					} else if (isButton){
						ImageButton button = (ImageButton)rootView.findViewById(buttonId);
						button.setVisibility(View.VISIBLE);
						button.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								if (HardwareUtilities.isWiFiConnected(rootView.getContext())){
									RetrieveComputer rc = new RetrieveComputer();
									rc.execute(function.getWs());
								} else {
									HardwareUtilities.enableInternetConnectionAlertDialog(
											rootView.getContext(), true, false);
								}
							} 
						});
					}
				}
			} else { SoftwareUtilities.MyErrorDialogFactory(rootView.getContext(), R.string.actuationButtonNotFound); }
		}
	}

	private int getButtonId(String command){
		int viewID = -1;
		if (command.equalsIgnoreCase(ActuationCommands.ComputerActuationCommands.VolumeDown.toString()))
			viewID =  R.id.computerActivity_imageButtonVolumeDown;
		else if (command.equalsIgnoreCase(ActuationCommands.ComputerActuationCommands.VolumeUp.toString()))
			viewID =  R.id.computerActivity_imageButtonVolumeUp;
		else if (command.equalsIgnoreCase(ActuationCommands.ComputerActuationCommands.VolumeMute.toString()))
			viewID =  R.id.computerActivity_imageButtonVolumeMute;
		else if (command.equalsIgnoreCase(ActuationCommands.ComputerActuationCommands.PlayerNext.toString()))
			viewID =  R.id.computerActivity_imageButtonNext;
		else if (command.equalsIgnoreCase(ActuationCommands.ComputerActuationCommands.PlayerPrevious.toString()))
			viewID =  R.id.computerActivity_imageButtonPrevious;
		else if (command.equalsIgnoreCase(ActuationCommands.ComputerActuationCommands.PlayerPlayPause.toString()))
			viewID =  R.id.computerActivity_imageButtonPlayPause;
		else if (command.equalsIgnoreCase(ActuationCommands.ComputerActuationCommands.PlayerStop.toString()))
			viewID =  R.id.computerActivity_imageButtonStop;
		else if (command.equalsIgnoreCase(ActuationCommands.ComputerActuationCommands.Sleep.toString()))
			viewID =  R.id.computerActivity_imageButtonSleep;
		else if (command.equalsIgnoreCase(ActuationCommands.ComputerActuationCommands.BrightnessDown.toString()))
			viewID =  R.id.computerActivity_imageButtonBrightnessDown;
		else if (command.equalsIgnoreCase(ActuationCommands.ComputerActuationCommands.BrightnessUp.toString()))
			viewID =  R.id.computerActivity_imageButtonBrightnessUp;

		return viewID;
	}

	private void hideButtons(){
		ImageButton genericButon;

		TextView volumeMute = (TextView) rootView.findViewById(R.id.computerActivity_volumeMute);
		volumeMute.setVisibility(View.INVISIBLE);

		// in case you want manage also a ToggleButton
//		ToggleButton onOff = (ToggleButton) rootView.findViewById(R.id.computerActivity_imageButtonVolumeMute);
//		onOff.setVisibility(View.INVISIBLE);
		
		genericButon = (ImageButton) rootView.findViewById(R.id.computerActivity_imageButtonVolumeMute);
		genericButon.setVisibility(View.INVISIBLE);

		genericButon = (ImageButton) rootView.findViewById(R.id.computerActivity_imageButtonVolumeUp);
		genericButon.setVisibility(View.INVISIBLE);

		genericButon = (ImageButton) rootView.findViewById(R.id.computerActivity_imageButtonVolumeDown);
		genericButon.setVisibility(View.INVISIBLE);

		genericButon = (ImageButton) rootView.findViewById(R.id.computerActivity_imageButtonPrevious);
		genericButon.setVisibility(View.INVISIBLE);

		genericButon = (ImageButton) rootView.findViewById(R.id.computerActivity_imageButtonNext);
		genericButon.setVisibility(View.INVISIBLE);

		genericButon = (ImageButton) rootView.findViewById(R.id.computerActivity_imageButtonPlayPause);
		genericButon.setVisibility(View.INVISIBLE);

		genericButon = (ImageButton) rootView.findViewById(R.id.computerActivity_imageButtonStop);
		genericButon.setVisibility(View.INVISIBLE);

		genericButon = (ImageButton) rootView.findViewById(R.id.computerActivity_imageButtonBrightnessDown);
		genericButon.setVisibility(View.INVISIBLE);

		genericButon = (ImageButton) rootView.findViewById(R.id.computerActivity_imageButtonBrightnessUp);
		genericButon.setVisibility(View.INVISIBLE);

		genericButon = (ImageButton) rootView.findViewById(R.id.computerActivity_imageButtonSleep);
		genericButon.setVisibility(View.INVISIBLE);
	}


	@Override
	public void update(){
		hideButtons();
		if (MyFragment.CURRENT_VISIBLE_FRAGMENT == MobileHomeConstants.COMPUTER_FRAGMENT_POSITION){

			if (sharedPref == null) 
				sharedPref = PreferenceManager.getDefaultSharedPreferences(rootView.getContext());
			
			try {
				this.computerSpinnerPosition = sharedPref.getInt(MobileHomeConstants.COMPUTERS_SPINNER_POSITION, 0);
			} catch (NullPointerException e){
				this.computerSpinnerPosition = 0;
			}
			Spinner computerSpinner = (Spinner) rootView.findViewById(R.id.computerActivity_computerSpinner);
			try {
				computerSpinner.setSelection(this.computerSpinnerPosition);
			} catch (Exception e){
				this.computerSpinnerPosition = 0;
				computerSpinner.setSelection(this.computerSpinnerPosition);	
			}

			Gson gson = new Gson();
			this.sharedPref = PreferenceManager.getDefaultSharedPreferences(rootView.getContext());
			String computersListJson = this.sharedPref.getString(MobileHomeConstants.COMPUTERS_JSON, null);

			HardwareUtilities.enableInternetConnectionAlertDialog(
					rootView.getContext(), true, false);
			if (computersListJson!=null){
				Type type = new TypeToken<HashMap<String, ComputerSettings>>(){}.getType();
				this.computersMap = gson.fromJson(computersListJson, type);
				this.populateComputersSpinner();
			}

		}
	}

	private void populateComputersSpinner(){
		Spinner computersSpinner = (Spinner)rootView.findViewById(R.id.computerActivity_computerSpinner);
		ArrayAdapter<CharSequence> adapter = 
				new ArrayAdapter<>(rootView.getContext(), android.R.layout.simple_spinner_item);
		if (this.computersMap != null) {
			for (ComputerSettings comp : this.computersMap.values())
				adapter.add(comp.getDescription() + " (ID: " + comp.getUrl()+ ")");
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			computersSpinner.setAdapter(adapter);
			try {
				computersSpinner.setSelection(this.computerSpinnerPosition);
			} catch (Exception e){
				this.computerSpinnerPosition = 0;
				computersSpinner.setSelection(this.computerSpinnerPosition);	
			}
			computersSpinner.setOnItemSelectedListener(this);
		} else {
			SoftwareUtilities.MyErrorDialogFactory(rootView.getContext(), R.string.noComputerInfo);
		}
	}


	private class RetrieveComputer extends AsyncTask<String, Void, IoTDevice> {
		private ProgressBar progress = (ProgressBar) rootView.findViewById(R.id.computerActivity_progressBar);
		private String errors = "";


		@Override
		protected void onPreExecute() {
			progress.setVisibility(View.VISIBLE);
		}

		@Override
		protected IoTDevice doInBackground(String... params) {
			IoTDevice device = null;
			try {

				String response;
				HashMap<String, String> httpHeaders = new HashMap<>();
				httpHeaders.put("Content-Type", "application/json");
				response = HttpConnection.sendGet(params[0], httpHeaders);
				device = new Gson().fromJson(response, IoTDevice.class);

			} catch (Exception e) {
				this.errors += ErrorUtilities.getExceptionMessage(e);
			}

			return device;
		}	


		@Override
		protected void onPostExecute(IoTDevice results) {
			TextView errorTextView = (TextView) rootView.findViewById(R.id.computerActivity_error_textview);
			if(results == null){
				try{
//					SoftwareUtilities.MyErrorDialogFactory(rootView.getContext(), this.errors);
					errorTextView.setTextAppearance(rootView.getContext(), android.R.style.TextAppearance_Medium);
					errorTextView.setText(rootView.getContext().getString(R.string.error) + ": " + this.errors);
					errorTextView.setSingleLine(false);
					errorTextView.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);

				} catch (Exception e){
					Log.e("Error", e.getMessage());
				}
			} else {
				errorTextView.setText("");
				errorTextView.setTextAppearance(rootView.getContext(), android.R.style.TextAppearance_Small);
				showButtons(results);
			}
			progress.setVisibility(View.INVISIBLE);

		}
	}

}