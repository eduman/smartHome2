package it.eduman.mobileHome2;

import it.eduman.mobileHome2.commons.MobileHomeConstants;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


/**
 * A dummy fragment representing a section of the app, but that simply
 * displays dummy text.
 */
public class MainSectionFragment extends MyFragment {
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */

	private static View rootView = null;


	public MainSectionFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.main_fragment_activity,
				container, false);

//		ActivityCommons.updateAfterUserSettingsChanges(rootView.getContext());
		
		ImageButton roomsButton = (ImageButton) rootView.findViewById(R.id.manageRoomsButton);
		roomsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((MainActivity)getActivity()).nextPage(MobileHomeConstants.HOME_FRAGMENT_POSITION);
			}
		});
		
		ImageButton computerButton = (ImageButton) rootView.findViewById(R.id.manageComputerButton);
		computerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((MainActivity)getActivity()).nextPage(MobileHomeConstants.COMPUTER_FRAGMENT_POSITION); 
			}
		});
		
		ImageButton weatherButton = (ImageButton) rootView.findViewById(R.id.manageWeatherButton);
		weatherButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((MainActivity)getActivity()).nextPage(MobileHomeConstants.WEATHER_FRAGMENT_POSITION); 
			}
		});
		
		ImageButton messagesButton = (ImageButton) rootView.findViewById(R.id.manageMessagesButton);
		messagesButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((MainActivity)getActivity()).nextPage(MobileHomeConstants.MESSAGES_FRAGMENT_POSITION); 
			}
		});
		
		ImageButton locationButton = (ImageButton) rootView.findViewById(R.id.manageLocationButton);
		locationButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((MainActivity)getActivity()).nextPage(MobileHomeConstants.LOCATION_FRAGMENT_POSITION); 
			}
		});
		
		ImageButton settingsButton = (ImageButton) rootView.findViewById(R.id.manageSettingsButton);
		settingsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				rootView.getContext().startActivity(new Intent(rootView.getContext(), SettingsActivity.class)); 
			}
		});
		
		
		return rootView;
	}

	@Override
	public void onResume(){
		super.onResume();
		ActivityCommons.updateAfterUserSettingsChanges(rootView.getContext());
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void update(){
		
	}
}