package it.eduman.mobileHome2;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.List;

import it.eduman.android.commons.utilities.HardwareUtilities;
import it.eduman.android.commons.utilities.HttpConnection;
import it.eduman.android.commons.utilities.SoftwareUtilities;
import it.eduman.mobileHome2.commons.MobileHomeConstants;
import it.eduman.smartHome.HomeStructure.HomeStructure;
import it.eduman.smartHome.HomeStructure.Rule;
import it.eduman.smartHome.user.UserPresence;


public class UserSectionFragment extends MyFragment {

    private static View rootView = null;
    private SharedPreferences sharedPref;
    private HashMap<String, UserPresence> usersList = new HashMap<>();
    private HomeStructure home = null;
    protected String homeServiceProvider = null;
//    private HashMap<String, ImageButton> userButtonsList = new HashMap<>();
    float scale;
    private int updateInterval = 60000;
    private UIUpdater mUIUpdater;
    TableLayout tableLayout;
    TableLayout.LayoutParams layoutRow =
            new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);

//    TableRow.LayoutParams layoutText =
//            new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//                    ViewGroup.LayoutParams.MATCH_PARENT);

    TableRow.LayoutParams layoutButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.user_fragment_activity, container, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(rootView.getContext());


        ImageButton refreshButton = (ImageButton)rootView.findViewById(R.id.user_fragment_refresh_button);
        refreshButton.setVisibility(View.VISIBLE);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update();
            }
        });

        ImageButton homeButton = (ImageButton)rootView.findViewById(R.id.user_fragment_home_button);
        homeButton.setVisibility(View.VISIBLE);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).nextPage(MobileHomeConstants.MAIN_FRAGMENT_POSITION);
            }
        });

        tableLayout = (TableLayout) rootView.findViewById(R.id.user_TableLayout);

        scale = rootView.getContext().getResources().getDisplayMetrics().density;
        layoutButton = new TableRow.LayoutParams((int)(150*scale), (int)(150*scale));

        this.mUIUpdater = new UIUpdater(new Runnable() {
            @Override
            public void run() {
                update();
            }
        }, this.updateInterval);

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        ActivityCommons.updateAfterUserSettingsChanges(rootView.getContext());
        //update();
        this.mUIUpdater.startUpdates();
    }

    @Override
    public void onPause(){
        this.mUIUpdater.stopUpdates();
        super.onPause();
    }

    @Override
    public synchronized void update() {
        if (MyFragment.CURRENT_VISIBLE_FRAGMENT == MobileHomeConstants.USER_FRAGMENT_POSITION){
            tableLayout.removeAllViews();

            if (this.sharedPref == null)
                this.sharedPref = PreferenceManager.getDefaultSharedPreferences(rootView.getContext());
            String homeServiceProvider = this.sharedPref.getString(
                    rootView.getResources().getString(R.string.preference_home_service_provider_key), null);
            if (homeServiceProvider == null){
                SoftwareUtilities.MyErrorDialogFactory(rootView.getContext(), R.string.setHomeInfo);
            } else {
                this.homeServiceProvider = homeServiceProvider;
                HardwareUtilities.enableInternetConnectionAlertDialog(rootView.getContext(), true, false);
                if (HardwareUtilities.isWiFiConnected(rootView.getContext())){
                    RetrieveHome rh = new RetrieveHome();
                    rh.execute(homeServiceProvider);
                }
            }

        }

    }

    void showInfo(){
//        userButtonsList.clear();

//        TableRow textTableRow  = null;
        TableRow buttonTableRow  = null;

        final UserPresence[] usersPresence = usersList.values().toArray(new UserPresence[usersList.size()]);
        for (int i = 0; i < usersPresence.length; i++){
//            if ( (i % 2) == 0) {
//                // new rows are needed
//                buttonTableRow = new TableRow(rootView.getContext());
//                buttonTableRow.setLayoutParams(layoutRow);
//                buttonTableRow.setGravity(Gravity.CENTER_HORIZONTAL);
//                tableLayout.addView(buttonTableRow);
//
//                textTableRow = new TableRow(rootView.getContext());
//                textTableRow.setLayoutParams(layoutRow);
//                textTableRow.setGravity(Gravity.CENTER_HORIZONTAL);
//                tableLayout.addView(textTableRow);
//
//
//                ImageButton imageButton = new ImageButton(rootView.getContext());
//                layoutButton.setMargins(40, 0, 20, 0);
//                imageButton.setLayoutParams(layoutButton);
//                imageButton.setImageResource(R.drawable.ic_action_user);
//                imageButton.setBackgroundResource(R.drawable.my_button_blue);
//                imageButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                buttonTableRow.addView(imageButton);
//                userButtonsList.put(usersPresence[i].getUser(), imageButton);
//
//                TextView textView = new TextView(rootView.getContext());
//                layoutText.setMargins(40, 0, 20, 0);
//                textView.setLayoutParams(layoutText);
//                textView.setTextAppearance(rootView.getContext(), android.R.style.TextAppearance_Large);
//                textView.setGravity(Gravity.CENTER_HORIZONTAL);
//                textTableRow.addView(textView);
//
//                textView.setText(usersPresence[i].getUser());
//
//            } else{
//                // adding new image buttons and text to old rows
//                ImageButton imageButton = new ImageButton(rootView.getContext());
//                layoutButton.setMargins(20, 0, 40, 0);
//                imageButton.setLayoutParams(layoutButton);
//                imageButton.setImageResource(R.drawable.ic_action_user);
//                imageButton.setBackgroundResource(R.drawable.my_button_blue);
//                imageButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                buttonTableRow.addView(imageButton);
//                userButtonsList.put(usersPresence[i].getUser(), imageButton);
//
//                TextView textView = new TextView(rootView.getContext());
//                layoutText.setMargins(20, 0, 40, 0);
//                textView.setLayoutParams(layoutText);
//                textView.setTextAppearance(rootView.getContext(), android.R.style.TextAppearance_Large);
//                textView.setGravity(Gravity.CENTER_HORIZONTAL);
//                textTableRow.addView(textView);
//
//                textView.setText(usersPresence[i].getUser());
//
//            }

            if ( (i % 2) == 0) {
                buttonTableRow = new TableRow(rootView.getContext());
                buttonTableRow.setLayoutParams(layoutRow);
                buttonTableRow.setGravity(Gravity.CENTER_HORIZONTAL);
                buttonTableRow.setPadding(0,0,0,100);
                tableLayout.addView(buttonTableRow);

//                textTableRow = new TableRow(rootView.getContext());
//                textTableRow.setLayoutParams(layoutRow);
//                textTableRow.setGravity(Gravity.CENTER_HORIZONTAL);
//                tableLayout.addView(textTableRow);

//                layoutText.setMargins(40, 0, 20, 0);
                layoutButton.setMargins(40, 0, 20, 0);
            } else {
//                layoutText.setMargins(20, 0, 40, 0);
                layoutButton.setMargins(20, 0, 40, 0);

            }

//            ImageButton imageButton = new ImageButton(rootView.getContext());
//            imageButton.setLayoutParams(layoutButton);
//            imageButton.setImageResource(R.drawable.ic_action_user);
//            imageButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
//            SoftwareUtilities.MyInfoDialogFactory(rootView.getContext(), String.valueOf(imageButton.getId()));
//
//            imageButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    SoftwareUtilities.MyInfoDialogFactory(rootView.getContext(), String.valueOf(v.getId()));
//                }
//            });
//
//            if (usersPresence[i].isPresent()){
//                imageButton.setBackgroundResource(R.drawable.my_button_blue);
//            } else {
//                imageButton.setBackgroundResource(R.drawable.my_button_red);
//            }
//            buttonTableRow.addView(imageButton);
//            userButtonsList.put(usersPresence[i].getUser(), imageButton);

//            TextView textView = new TextView(rootView.getContext());
//            textView.setLayoutParams(layoutText);
//            textView.setTextAppearance(rootView.getContext(), android.R.style.TextAppearance_Large);
//            textView.setGravity(Gravity.CENTER_HORIZONTAL);
//            textTableRow.addView(textView);
//            textView.setText(usersPresence[i].getUser());


            Button button = new Button(rootView.getContext());
            button.setLayoutParams(layoutButton);
            button.setTextAppearance(rootView.getContext(), android.R.style.TextAppearance_Large);
            button.setGravity(Gravity.CENTER);
            button.setText(usersPresence[i].getUser());
            if (usersPresence[i].isPresent()){
                button.setBackgroundResource(R.drawable.my_button_blue);
            } else {
                button.setBackgroundResource(R.drawable.my_button_red);
            }
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button button = (Button) v;
                    String username = button.getText().toString();
                    if (usersList.containsKey(username)) {

                        HardwareUtilities.enableInternetConnectionAlertDialog(rootView.getContext(), true, false);
                        if (HardwareUtilities.isWiFiConnected(rootView.getContext())) {
                            boolean newUserPresence = !usersList.get(username).isPresent();
                            UpdateUser userUpdate = new UpdateUser();
                            userUpdate.execute(homeServiceProvider, username, String.valueOf(newUserPresence));
                        }

                    }
                }
            });
            buttonTableRow.addView(button);
        }
    }

    private class UpdateUser extends  AsyncTask<String, Void, String>{
        private ProgressBar progress = (ProgressBar) rootView.findViewById(R.id.user_framgent_progressBar);

        @Override
        protected void onPreExecute() {
            this.progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String error = "ok";
            Gson gson = new Gson();
            try {
                String response;
                HashMap<String, String> httpHeaders = new HashMap<>();
                httpHeaders.put("Content-Type", "application/json");
                response = HttpConnection.sendGet(params[0], httpHeaders);
                home = gson.fromJson(response, HomeStructure.class);
//                usersList.clear();
//                for ( Rule rule : home.getRules()){
//                    for (String user : rule.getUserList()){
//                        usersList.put(user, (new UserPresence(user)));
//                    }
//                }

                if (home != null){
                    String uri = home.getUserPresenceManager().getUpdateStatus();
                    String finalUri = String.format(uri, params[1], params[2]);
                    response = HttpConnection.sendGet(finalUri, httpHeaders);
                }

                //TODO: verify if sleeping is really needed
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException ie) {
//                    //Handle exception
//                }

                if (home != null) {
                    response = HttpConnection.sendGet(home.getUserPresenceManager().getStatus(), httpHeaders);
                    List<UserPresence> updatedUsers= gson.fromJson(response, new TypeToken<List<UserPresence>>(){}.getType());
                    for (UserPresence u : updatedUsers){
                        if (usersList.containsKey(u.getUser())){
                            usersList.put(u.getUser(), u);
                        }
                    }
                }

            } catch (Exception e){
//                error = ErrorUtilities.getExceptionMessage(e);
                error = e.getMessage();
            }


            return error;
        }

        @Override
        protected void onPostExecute(String results) {
            if (!results.equalsIgnoreCase("ok")){
                tableLayout.removeAllViews();

                TableRow tableRow = new TableRow(rootView.getContext());
                tableLayout.addView(tableRow);
                TextView textView = new TextView(rootView.getContext());
                textView.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                textView.setTextAppearance(rootView.getContext(), android.R.style.TextAppearance_Medium);
                textView.setText(rootView.getContext().getString(R.string.error) + ": " + results);
                textView.setSingleLine(false);
                textView.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                tableRow.addView(textView);

            } else {
                tableLayout.removeAllViews();
                showInfo();
            }
            this.progress.setVisibility(View.INVISIBLE);
        }
    }






    private class RetrieveHome extends AsyncTask<String, Void, String> {
        private ProgressBar progress = (ProgressBar) rootView.findViewById(R.id.user_framgent_progressBar);

        @Override
        protected void onPreExecute() {
            this.progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params)
        {
            String error = "ok";
            Gson gson = new Gson();
            try {
                String response;
                HashMap<String, String> httpHeaders = new HashMap<>();
                httpHeaders.put("Content-Type", "application/json");
                response = HttpConnection.sendGet(params[0], httpHeaders);
                home = gson.fromJson(response, HomeStructure.class);
                usersList.clear();
                for ( Rule rule : home.getRules()){
                    for (String user : rule.getUserList()){
                        usersList.put(user, (new UserPresence(user)));
                    }
                }

                if (home != null) {
                    response = HttpConnection.sendGet(home.getUserPresenceManager().getStatus(), httpHeaders);
                    List<UserPresence> updatedUsers= gson.fromJson(response, new TypeToken<List<UserPresence>>(){}.getType());
                    for (UserPresence u : updatedUsers){
                        if (usersList.containsKey(u.getUser())){
                            usersList.put(u.getUser(), u);
                        }
                    }
                }
            } catch (Exception e){
//                error = ErrorUtilities.getExceptionMessage(e);
                error = e.getMessage();
            }

            return error;

        }

        @Override
        protected void onPostExecute(String results) {
            if (!results.equalsIgnoreCase("ok")){
                tableLayout.removeAllViews();

                TableRow tableRow = new TableRow(rootView.getContext());
                tableLayout.addView(tableRow);
                TextView textView = new TextView(rootView.getContext());
                textView.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                textView.setTextAppearance(rootView.getContext(), android.R.style.TextAppearance_Medium);
                textView.setText(rootView.getContext().getString(R.string.error) + ": " + results);
                textView.setSingleLine(false);
                textView.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                tableRow.addView(textView);

            } else {
                showInfo();
            }
            this.progress.setVisibility(View.INVISIBLE);
        }
    }
}
