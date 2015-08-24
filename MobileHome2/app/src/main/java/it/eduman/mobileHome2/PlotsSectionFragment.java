package it.eduman.mobileHome2;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.google.gson.Gson;

import java.util.HashMap;

import it.eduman.android.commons.utilities.ErrorUtilities;
import it.eduman.android.commons.utilities.HardwareUtilities;
import it.eduman.android.commons.utilities.HttpConnection;
import it.eduman.android.commons.utilities.HttpConnectionException;
import it.eduman.android.commons.utilities.SoftwareUtilities;
import it.eduman.mobileHome2.commons.MobileHomeConstants;
import it.eduman.smartHome.HomeStructure.HomeStructure;

public class PlotsSectionFragment extends MyFragment {

    private static View rootView = null;
    private WebView myWebView;
    private ProgressBar progress;
    private SharedPreferences sharedPref;
    private ImageButton backwardButton;
    private ImageButton forwardButton;
    private Bundle webViewBundle;
    private String currentURI = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.plots_fragment_activity, container, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(rootView.getContext());

        String homeID = this.sharedPref.getString(
                rootView.getResources().getString(R.string.preference_home_service_provider_key), null);



        //set progress bar
        progress = (ProgressBar) rootView.findViewById(R.id.plots_fragment_webview_progressBar);
        progress.setMax(100);
        final float[] roundedCorners = new float[] { 5, 5, 5, 5, 5, 5, 5, 5 };
        ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
        pgDrawable.getPaint().setColor(getResources().getColor(R.color.blue_twitter));
        ClipDrawable progressClip = new ClipDrawable(pgDrawable, Gravity.START, ClipDrawable.HORIZONTAL);
        progress.setProgressDrawable(progressClip);

        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            progress.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));
        } else {
            progress.setBackground(getResources().getDrawable(android.R.drawable.progress_horizontal));
        }

        backwardButton =(ImageButton) rootView.findViewById(R.id.plots_fragment_backward_button);
        backwardButton.setAlpha(.5f);
        backwardButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (myWebView.canGoBack()) {
                    myWebView.goBack();
                }
            }
        });


        forwardButton =(ImageButton) rootView.findViewById(R.id.plots_fragment_forward_button);
        forwardButton.setAlpha(.5f);
        forwardButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(myWebView.canGoForward()){
                    myWebView.goForward();
                }
            }
        });

        // Set up the webview
        myWebView = (WebView) rootView.findViewById(R.id.plots_webview);
        myWebView.setWebViewClient(new MyWebViewClient());
        myWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                PlotsSectionFragment.this.progress.setProgress(progress);
            }
        });
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setLoadsImagesAutomatically(true);
        myWebView.getSettings().setUseWideViewPort(true);
//		myWebView.setWebViewClient(new WebViewClient());
//		myWebView.setWebChromeClient(new MyWebViewClient());
        myWebView.getSettings().setBuiltInZoomControls(true);
        myWebView.getSettings().setLoadWithOverviewMode(true);
//		myWebView.getSettings().setPluginState(PluginState.ON);
        myWebView.bringToFront();



        ImageButton refreshButton = (ImageButton)rootView.findViewById(R.id.plots_fragment_refresh_button);
        refreshButton.setVisibility(View.VISIBLE);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update();
            }
        });

        ImageButton homeButton = (ImageButton)rootView.findViewById(R.id.plots_fragment_home_button);
        homeButton.setVisibility(View.VISIBLE);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).nextPage(MobileHomeConstants.MAIN_FRAGMENT_POSITION);
            }
        });


        //restore
        if (webViewBundle == null || currentURI.equals("")) {
            if (homeID == null){
                SoftwareUtilities.MyErrorDialogFactory(rootView.getContext(), R.string.setHomeInfo);
            } else {
                HardwareUtilities.enableInternetConnectionAlertDialog(rootView.getContext(), true, false);
                if (HardwareUtilities.isWiFiConnected(rootView.getContext())){
                    RetrieveHome rh = new RetrieveHome();
                    rh.execute(homeID);
                }
            }

        } else {
            myWebView.restoreState(webViewBundle);
        }

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        ActivityCommons.updateAfterUserSettingsChanges(rootView.getContext());
//        update();
    }

    @Override
    public void onPause(){
        super.onPause();
        if (myWebView != null) {
            webViewBundle = new Bundle();
            myWebView.saveState(webViewBundle);
        }
    }

    @Override
    public void update() {
        if (myWebView != null) {
            loadPage(myWebView.getUrl());
        }
    }

    public void loadPage (String url){
        try {
            if (HardwareUtilities.isWiFiConnected(rootView.getContext())){
                if (myWebView != null){
                    myWebView.loadUrl(url);
                }
            } else {
                HardwareUtilities.enableInternetConnectionAlertDialog(
                        rootView.getContext(), true, false);
            }
        } catch (Exception e) {
            SoftwareUtilities.MyErrorDialogFactory(rootView.getContext(), R.string.webPageError);
        }
    }


    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            loadPage(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            PlotsSectionFragment.this.progress.setProgress(100);
            super.onPageFinished(view, url);

            PlotsSectionFragment.this.backwardButton.setEnabled(view.canGoBack());
            if(view.canGoBack()){
                PlotsSectionFragment.this.backwardButton.setAlpha(1.0f);
            } else {
                PlotsSectionFragment.this.backwardButton.setAlpha(.5f);
            }

            PlotsSectionFragment.this.forwardButton.setEnabled(view.canGoForward());
            if(view.canGoForward()){
                PlotsSectionFragment.this.forwardButton.setAlpha(1.0f);
            } else {
                PlotsSectionFragment.this.forwardButton.setAlpha(.5f);
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            PlotsSectionFragment.this.progress.setProgress(0);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onReceivedError(WebView view, int errorCod, String description, String failingUrl) {
            SoftwareUtilities.MyErrorDialogFactory
                    (PlotsSectionFragment.rootView.getContext(), "Your Internet Connection May not be active Or " + description);

        }
    }

    private class RetrieveHome extends AsyncTask<String, Void, HomeStructure> {
        ProgressBar homeStructureProgress = (ProgressBar) rootView.findViewById(R.id.plots_fragment_homestructure_progressBar);

        private String errors = "";


        @Override
        protected void onPreExecute() {
            this.homeStructureProgress.setVisibility(View.VISIBLE);
        }


        @SuppressWarnings("unchecked")
        @Override
        protected HomeStructure doInBackground(String... params)
        {
            HomeStructure homeStructure = null;
            try {
                String response;
                HashMap<String, String> httpHeaders = new HashMap<String, String>();
                httpHeaders.put("Content-Type", "application/json");
                response = HttpConnection.sendGet(
                        params[0],
                        httpHeaders);
                homeStructure = new Gson().fromJson(response, HomeStructure.class);
            } catch (HttpConnectionException e){
                this.errors += ErrorUtilities.getExceptionMessage(e);
            }

            return homeStructure;

        }

        @Override
        protected void onPostExecute(HomeStructure results) {

            if (results == null){
                SoftwareUtilities.MyErrorDialogFactory(
                        rootView.getContext(),
                        rootView.getContext().getResources().getString(R.string.startingInfoErr)
                                + this.errors);
            } else {
                currentURI = results.getThingspeakChannel();
            }

            this.homeStructureProgress.setVisibility(View.INVISIBLE);
            loadPage(currentURI);
        }
    }
}
