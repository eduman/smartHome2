package it.eduman.mobileHome2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;

import it.eduman.android.commons.utilities.SoftwareUtilities;
import it.eduman.mobileHome2.commons.MobileHomeConstants;
import it.eduman.smartHome.computer.ComputerSettings;

public class ComputerSettingsActivity extends Activity implements AdapterView.OnItemSelectedListener{
	private SharedPreferences sharedPref;
	private static int EDIT_TEXT_EMS = 8;
	private ComputerSettings computerSettings;
	private int computerSettingsSpinnerPosition = 0;
	private HashMap<String, ComputerSettings> computersMap = new HashMap<String, ComputerSettings>();
	
	private EditText urlEditText;
	private EditText descriptionEditText;
	private Button saveComputerButton;
	private Button removeComputerButton;
	
	public ComputerSettingsActivity(){}
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}


		setContentView(R.layout.computer_settings_activity);
		setTitle(R.string.title_activity_computer_settings);
		SoftwareUtilities.shortDebugToast(this, "onCreate");
		this.init();
        
        this.computerSettingsSpinnerPosition = sharedPref.getInt(MobileHomeConstants.COMPUTERS_SETTINGS_SPINNER_POSITION, 0);
        Spinner computerSpinner = (Spinner) findViewById(R.id.computerSettingsActivity_computerSpinner);
        try {
        	computerSpinner.setSelection(this.computerSettingsSpinnerPosition);
        } catch (Exception e){
        	this.computerSettingsSpinnerPosition = 0;
        	computerSpinner.setSelection(this.computerSettingsSpinnerPosition);
        }
    }
    
    @Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		this.computerSettingsSpinnerPosition = savedInstanceState.getInt("computerSpinnerPosition");
		this.urlEditText.setText(savedInstanceState.getString("urlEditText"));
		this.descriptionEditText.setText(savedInstanceState.getString("descriptionEditText"));
        Spinner computerSpinner = (Spinner) findViewById(R.id.computerSettingsActivity_computerSpinner);
        try {
        	computerSpinner.setSelection(this.computerSettingsSpinnerPosition);
        } catch (Exception e){
        	this.computerSettingsSpinnerPosition = 0;
        	computerSpinner.setSelection(this.computerSettingsSpinnerPosition);
        }
	}
    
	@Override
	public void onStart(){
		super.onStart();		
		ActivityCommons.checkAndSetFullScreen(this);
		ActivityCommons.checkAndSetScreenAlwaysOn(this);
	}

	@Override
	public void onRestart(){
		super.onRestart();
		ActivityCommons.updateAfterUserSettingsChanges(this);
		this.init();

	}
	
	@Override
	public void onStop(){
		super.onStop();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// Save the values you need from your textview into "outState"-object
		outState.putInt("computerSpinnerPosition", this.computerSettingsSpinnerPosition);
		outState.putString("urlEditText", this.urlEditText.getText().toString());
		outState.putString("descriptionEditText", this.descriptionEditText.getText().toString());
		super.onSaveInstanceState(outState);
	}
    
    public boolean onCreateOptionsMenu(Menu menu) {
		return MenuUtilities.myMenuFactory(this, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return MenuUtilities.onMyOptionsItemSelected(
				this, item.getItemId());
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
		ProgressDialog progress = ProgressDialog.show(this, 
				getResources().getString(R.string.loadingMsg), 
				getResources().getString(R.string.waitMsg), false, false);
		switch (parent.getId()){
			case R.id.computerSettingsActivity_computerSpinner:
				String computerSpinnerString = parent.getItemAtPosition(position).toString().replace(")", "");
				String lsDescriptionStr = computerSpinnerString.substring(computerSpinnerString.lastIndexOf("(ID: ") + "(ID: ".length(), computerSpinnerString.length());
				this.clearEditText();
				if (computersMap.containsKey(lsDescriptionStr)){
					this.computerSettingsSpinnerPosition = position;
					Editor edit = sharedPref.edit();
					edit.putInt(MobileHomeConstants.COMPUTERS_SETTINGS_SPINNER_POSITION, this.computerSettingsSpinnerPosition);
					edit.commit();
					computerSettings = computersMap.get(lsDescriptionStr);
					urlEditText.setText(computerSettings.getUrl());
					descriptionEditText.setText(computerSettings.getDescription());
				}
		}
		
		progress.dismiss();
	}


	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
    
	
	private void init(){
		Gson gson = new Gson();
		this.sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String computersListJson = this.sharedPref.getString(MobileHomeConstants.COMPUTERS_JSON, null);
		drawStaticGui();
		if (computersListJson!=null){
			Type type = new TypeToken<HashMap<String, ComputerSettings>>(){}.getType();
			this.computersMap = gson.fromJson(computersListJson, type);
			this.populateComputersSpinner();
		}
	}
	
	private void drawStaticGui(){
		final Context context = this;
		TableLayout tableLayoutEditableSettings = (TableLayout) findViewById(R.id.computerSettingsActivity_computerSettingstableLayout);
		tableLayoutEditableSettings.removeAllViews();
		
		TableLayout tableLayoutButtonSettings = (TableLayout) findViewById(R.id.computerSettingsActivity_buttonSettingsTableLayout);
		tableLayoutButtonSettings.removeAllViews();
		
		TableRow tableRow;
		TextView textView;
		
		urlEditText = new EditText(this);
		urlEditText.setEms(EDIT_TEXT_EMS);
		urlEditText.setText(R.string.preference_computer_uri_default_value);
		urlEditText.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		
		descriptionEditText = new EditText(this);
		descriptionEditText.setEms(EDIT_TEXT_EMS);
		descriptionEditText.setText(R.string.preference_computer_description_default_value);
		descriptionEditText.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));

		tableRow = new TableRow(this);
		tableRow.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		textView = new TextView(this);
		textView.setTextSize(18);
		textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
		textView.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		textView.setText(R.string.computerDescriptionTextView);
		tableRow.addView(textView);
		tableRow.addView(descriptionEditText);
		tableLayoutEditableSettings.addView(tableRow);

		tableRow = new TableRow(this);
		tableRow.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		textView = new TextView(this);
		textView.setTextSize(18);
		textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
		textView.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		textView.setText(R.string.computerUrlTextView);
		tableRow.addView(textView);
		tableRow.addView(urlEditText);
		tableLayoutEditableSettings.addView(tableRow);
		
		

		
		tableRow = new TableRow(this);
		tableRow.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		
		saveComputerButton = new Button(this);
		saveComputerButton.setText(R.string.saveButtonStr);
		saveComputerButton.setTextColor(Color.parseColor("#FFFFFF"));
		
//		saveComputerButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.my_button_blue));
		saveComputerButton.setBackgroundResource(R.drawable.my_button_blue);

		saveComputerButton.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)saveComputerButton.getLayoutParams();
		params.rightMargin = 16;
		saveComputerButton.setLayoutParams(params);
		
		saveComputerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				boolean isOk = true;
				int errorMessage = R.string.nullString;
				String urlStr = urlEditText.getText().toString();
				String descriptionStr = descriptionEditText.getText().toString();

				if (descriptionStr.length() < 1){
					isOk = false;
					errorMessage = R.string.descriptionIsNull;
				}
				if (urlStr.length() < 1){
					isOk = false;
					errorMessage = R.string.urlIsNull;
				}	

				if (isOk){
					ComputerSettings computerSetting = 
							new ComputerSettings(urlStr, descriptionStr);
					computersMap.put(urlStr, computerSetting);
					Gson gson = new Gson();
					Editor edit = sharedPref.edit();
					edit.putString(MobileHomeConstants.COMPUTERS_JSON, gson.toJson(computersMap));
					edit.commit();
					populateComputersSpinner();
				} else {
					SoftwareUtilities.MyErrorDialogFactory(context, errorMessage);
				}
				
			}
		});
		
		removeComputerButton = new Button(this);
		removeComputerButton.setText(R.string.deleteButtonStr);
		removeComputerButton.setTextColor(Color.parseColor("#FFFFFF"));
//		removeComputerButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.my_button_blue));
		removeComputerButton.setBackgroundResource(R.drawable.my_button_blue);
		
		removeComputerButton.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		removeComputerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String urlStr = urlEditText.getText().toString();
				if (computersMap.containsKey(urlStr)){
					computersMap.remove(urlStr);
					Gson gson = new Gson();
					Editor edit = sharedPref.edit();
					edit.putString(MobileHomeConstants.COMPUTERS_JSON, gson.toJson(computersMap));
					edit.commit();
					edit.putInt(MobileHomeConstants.COMPUTERS_SPINNER_POSITION, 0);
					edit.commit();
					computerSettingsSpinnerPosition = 0;
					edit.putInt(MobileHomeConstants.COMPUTERS_SETTINGS_SPINNER_POSITION, computerSettingsSpinnerPosition);
					edit.commit();
					clearEditText();
					populateComputersSpinner();
				} else {
					SoftwareUtilities.MyErrorDialogFactory(context, R.string.unknownUrl);
				}
			}
		});

		tableRow.addView(saveComputerButton);
		tableRow.addView(removeComputerButton);
		tableLayoutButtonSettings.addView(tableRow);
		
		Button addComputer = (Button) findViewById(R.id.computerSettingsActivity_addComputerButton);
		addComputer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				clearEditText();
			}
		});
		
	}
	
	private void populateComputersSpinner(){
		Spinner computersSpinner = (Spinner)findViewById(R.id.computerSettingsActivity_computerSpinner);
		ArrayAdapter<CharSequence> adapter = 
				new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
		if (this.computersMap != null) {
			for (ComputerSettings comp : this.computersMap.values())
				adapter.add(comp.getDescription() + " (ID: " + comp.getUrl()+ ")");
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			computersSpinner.setAdapter(adapter);
			try {
				computersSpinner.setSelection(this.computerSettingsSpinnerPosition);
			} catch (Exception e) {
				this.computerSettingsSpinnerPosition = 0;
				computersSpinner.setSelection(this.computerSettingsSpinnerPosition);
			}
			computersSpinner.setOnItemSelectedListener(this);
		} else {
			SoftwareUtilities.MyErrorDialogFactory(this, R.string.noComputerInfo);
		}
	}
	
	private void clearEditText(){
		urlEditText.setText(R.string.preference_computer_uri_default_value);
		descriptionEditText.setText(R.string.preference_computer_description_default_value);
	}
	
}
