package it.eduman.mobileHome2;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.HashMap;

import it.eduman.android.commons.utilities.ErrorUtilities;
import it.eduman.android.commons.utilities.HardwareUtilities;
import it.eduman.android.commons.utilities.HttpConnection;
import it.eduman.android.commons.utilities.SoftwareUtilities;
import it.eduman.mobileHome2.commons.MobileHomeConstants;
import it.eduman.smartHome.HomeStructure.HomeStructure;
import it.eduman.smartHome.HomeStructure.Rule;

public class RuleManagerSectionFragment extends MyFragment {

    private HomeStructure home;
    private static View rootView = null;
    private SharedPreferences sharedPref;
    private TableLayout tableLayout;
    private Button saveButton;
    private Button resetButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.rulemanager_fragment_activity, container, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(rootView.getContext());

        tableLayout = (TableLayout)rootView.findViewById(R.id.rulemanager_TableLayout);

        ImageButton refreshButton = (ImageButton)rootView.findViewById(R.id.rulemanager_fragment_refresh_button);
        refreshButton.setVisibility(View.VISIBLE);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update();
            }
        });

        ImageButton homeButton = (ImageButton)rootView.findViewById(R.id.rulemanager_fragment_home_button);
        homeButton.setVisibility(View.VISIBLE);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).nextPage(MobileHomeConstants.MAIN_FRAGMENT_POSITION);
            }
        });

        saveButton = (Button)rootView.findViewById(R.id.rulemanager_fragment_save_button);
        saveButton.setVisibility(View.INVISIBLE);
        saveButton.setEnabled(false);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String homeServiceProvider = sharedPref.getString(
                        rootView.getResources().getString(R.string.preference_home_service_provider_key), null);
                homeServiceProvider += "/updaterule";
                if (home != null && homeServiceProvider != null){
//                if (home != null){
                    String json = (new Gson()).toJson(home);
                    SendHome sendHome = new SendHome();
//                    sendHome.execute(home.getRuleUpdater(), json);
                    sendHome.execute(homeServiceProvider, json);
                    //TODO send MQTT event for updating control strategies
                }
            }
        });

        resetButton = (Button)rootView.findViewById(R.id.rulemanager_fragment_reset_button);
        resetButton.setVisibility(View.INVISIBLE);
        resetButton.setEnabled(false);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update();
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
    public synchronized void update() {
        if (MyFragment.CURRENT_VISIBLE_FRAGMENT == MobileHomeConstants.RULEMANAGER_FRAGMENT_POSITION){
            if (this.sharedPref == null)
                this.sharedPref = PreferenceManager.getDefaultSharedPreferences(rootView.getContext());
            String homeServiceProvider = this.sharedPref.getString(
                    rootView.getResources().getString(R.string.preference_home_service_provider_key), null);
            if (homeServiceProvider == null){
                SoftwareUtilities.MyErrorDialogFactory(rootView.getContext(), R.string.setHomeInfo);
            } else {
                HardwareUtilities.enableInternetConnectionAlertDialog(rootView.getContext(), true, false);
                if (HardwareUtilities.isWiFiConnected(rootView.getContext())){
                    RetrieveHome rh = new RetrieveHome(rootView.getContext());
                    rh.execute(homeServiceProvider);
                }
            }

        }
    }

    private void showInfo(){
        if (home != null){

            saveButton.setVisibility(View.INVISIBLE);
            saveButton.setEnabled(true);

            resetButton.setVisibility(View.INVISIBLE);
            resetButton.setEnabled(true);

            tableLayout.removeAllViews();
            for (Rule rule : home.getRules()){
                TableRow row =new TableRow(rootView.getContext());
                row.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                CheckBox checkBox = new CheckBox(rootView.getContext());
                checkBox.setText(rule.getRuleSID());
                checkBox.setChecked(rule.isRuleEnabled());
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        for (Rule rule : home.getRules()) {
                            if (rule.getRuleSID().equalsIgnoreCase(buttonView.getText().toString())) {
                                rule.setIsRuleEnabled(isChecked);
                            }
                        }

                        saveButton.setVisibility(View.VISIBLE);
                        saveButton.setEnabled(true);

                        resetButton.setVisibility(View.VISIBLE);
                        resetButton.setEnabled(true);
                    }
                });
                row.addView(checkBox);
                tableLayout.addView(row);
            }


        } else {
            SoftwareUtilities.MyErrorDialogFactory(rootView.getContext(), R.string.noHomeInfo);
        }
    }

    private class RetrieveHome extends AsyncTask<String, Void, HomeStructure> {
        private ProgressBar progress = (ProgressBar) rootView.findViewById(R.id.rulemanager_framgent_progressBar);
        private String errors = "";
        private Context context;

        public RetrieveHome (Context context){
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
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

            this.progress.setVisibility(View.INVISIBLE);
        }
    }

    private class SendHome extends AsyncTask<String, Void, Void> {
        private ProgressBar progress = (ProgressBar) rootView.findViewById(R.id.rulemanager_framgent_progressBar);
        private String errors = "";
        private String response = "";


        @Override
        protected void onPreExecute() {
            this.progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... params)
        {
            try {
                if (home != null){
                    HashMap<String, String> httpHeaders = new HashMap<>();
                    httpHeaders.put("Content-Type", "application/json");
                    response = HttpConnection.sendPUT(params[0], params[1], httpHeaders);
                }
            } catch (Exception e){
                this.errors += ErrorUtilities.getExceptionMessage(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void results) {

            if (this.errors.equals("")) {
                SoftwareUtilities.MyInfoDialogFactory(rootView.getContext(),
                        rootView.getContext().getResources().getString(R.string.ruleSettingsSent));

                saveButton.setVisibility(View.INVISIBLE);
                saveButton.setEnabled(false);
                resetButton.setVisibility(View.INVISIBLE);
                resetButton.setEnabled(false);

            } else {
                    SoftwareUtilities.MyErrorDialogFactory(
                            rootView.getContext(),
                            rootView.getContext().getResources().getString(R.string.startingInfoErr)
                                    + this.errors);
                }

            this.progress.setVisibility(View.INVISIBLE);
            }
    }
}
