//package it.eduman.mobileHome2;
//import it.eduman.android.commons.utilities.ActionTask;
//import it.eduman.android.commons.utilities.ErrorUtilities;
//import it.eduman.android.commons.utilities.SoftwareUtilities;
//import it.eduman.mobileHome2.commons.MobileHomeConstants;
//import it.eduman.mobileHome2.deprecated.proxymityAllert.ProximityAllert;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Locale;
//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.SharedPreferences;
//import android.content.SharedPreferences.Editor;
//import android.location.Address;
//import android.location.Criteria;
//import android.location.Geocoder;
//import android.location.Location;
//import android.location.LocationManager;
//import android.os.Bundle;
//import android.preference.PreferenceManager;
//import android.util.Log;
//import android.util.TypedValue;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageButton;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
//import com.google.android.gms.location.LocationListener;
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
//import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
//import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
//import com.google.android.gms.maps.MapView;
//import com.google.android.gms.maps.MapsInitializer;
//import com.google.android.gms.maps.model.BitmapDescriptorFactory;
//import com.google.android.gms.maps.model.CameraPosition;
//import com.google.android.gms.maps.model.CircleOptions;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.Marker;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.gson.Gson;
//
//
//public class MapSectionFragment extends MyFragment implements LocationListener{
//
//	private static final int DEFAULT_ZOOM = 17;
//	private static int MAP_TYPE = GoogleMap.MAP_TYPE_NORMAL;
//
//	private LatLng myHomeLatLng = null;
//	private GoogleMap googleMap;
//	private MapView mapView;
//	private static View rootView = null;
//	private LocationManager locationManager = null;
//	private Location lastLocation = null;
//	private static CameraPosition cameraPosition = null;
//	private SharedPreferences sharedPref;
//
//
//
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container,
//			Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		rootView = inflater.inflate(R.layout.maps_fragment,container, false);
////		ActivityCommons.updateAfterUserSettingsChanges(rootView.getContext());
//		sharedPref = PreferenceManager.getDefaultSharedPreferences(rootView.getContext());
//		locationManager = (LocationManager) rootView.getContext().getSystemService(Context.LOCATION_SERVICE);
//		lastLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));
//
//
//		setUpMapIfNeeded(savedInstanceState, inflater);
//
//		ImageButton refreshButton = (ImageButton)rootView.findViewById(R.id.map_fragment_refresh_button);
//		refreshButton.setVisibility(View.VISIBLE);
//		refreshButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				update();
//			}
//		});
//
//		ImageButton homeButton = (ImageButton)rootView.findViewById(R.id.map_fragment_home_button);
//		homeButton.setVisibility(View.VISIBLE);
//		homeButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				((MainActivity)getActivity()).nextPage(MobileHomeConstants.MAIN_FRAGMENT_POSITION);
//			}
//		});
//
//		ImageButton mapSettingsLocationButton = (ImageButton)rootView.findViewById(R.id.map_fragment_settings_button);
//		mapSettingsLocationButton.setVisibility(View.VISIBLE);
//		mapSettingsLocationButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (googleMap != null) {
//					String[] items={
//							rootView.getResources().getString(R.string.mapsNormal),
//							rootView.getResources().getString(R.string.mapsSatellite),
//							rootView.getResources().getString(R.string.mapsHybrid),
//							rootView.getResources().getString(R.string.mapsTerranian)};
//					AlertDialog.Builder itemDilog = new AlertDialog.Builder(rootView.getContext());
//					itemDilog.setTitle(rootView.getResources().getString(R.string.mapsOptions));
//					itemDilog.setCancelable(true);
//					itemDilog.setItems(items, new DialogInterface.OnClickListener() {
//						public void onClick(DialogInterface dialog, int which) {
//							switch(which){
//							case 0:
//								googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//								MAP_TYPE = GoogleMap.MAP_TYPE_NORMAL;
//								break;
//							case 1:
//								googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
//								MAP_TYPE = GoogleMap.MAP_TYPE_SATELLITE;
//								break;
//							case 2:
//								googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
//								MAP_TYPE = GoogleMap.MAP_TYPE_HYBRID;
//								break;
//							case 3:
//								googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
//								MAP_TYPE = GoogleMap.MAP_TYPE_TERRAIN;
//								break;
//							default:
//								googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//								MAP_TYPE = GoogleMap.MAP_TYPE_NORMAL;
//								break;
//							}
//						}
//					});
//					itemDilog.show();
//				} else {
//					SoftwareUtilities.MyErrorDialogFactory(rootView.getContext(), R.string.mapNotInitialized);
//				}
//			}
//		});
//
//		ImageButton myHomeLocationButton = (ImageButton)rootView.findViewById(R.id.map_fragment_myHomeLocation_button);
//		myHomeLocationButton.setVisibility(View.VISIBLE);
//		myHomeLocationButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (googleMap != null) {
//					if (myHomeLatLng != null){
//						googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myHomeLatLng, DEFAULT_ZOOM));
//					} else {
//						SoftwareUtilities.MyInfoDialogFactory(rootView.getContext(), R.string.mapHomePositionNotFound);
//					}
//				}
//			}
//		});
//
//		return rootView;
//	}
//
//	@Override
//	public void onResume(){
//		super.onResume();
//		ActivityCommons.updateAfterUserSettingsChanges(rootView.getContext());
//		mapView.onResume();
////		setUpMapIfNeeded(null);
//		MAP_TYPE = this.sharedPref.getInt(MobileHomeConstants.GOOGLE_MAPS_TYPE_VIEW, GoogleMap.MAP_TYPE_NORMAL);
//
//		googleMap = mapView.getMap();
//		myHomeLatLng = getMyHomeFromPref();
//		if (googleMap != null){
//			googleMap.setMyLocationEnabled(true);
//			googleMap.setMapType(MAP_TYPE);
//			if (cameraPosition != null) {
//				googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//				cameraPosition = null;
//			}
//			if (myHomeLatLng != null){
//				googleMap.clear();
//				addMyHomeMarker(myHomeLatLng);
//			}
//		}
//
//		setMapsButtonPoistion();
//		update();
//	}
//
//
//	@Override
//	public void onPause(){
//		Editor edit = sharedPref.edit();
//		edit.putInt(MobileHomeConstants.GOOGLE_MAPS_TYPE_VIEW, MAP_TYPE);
//		edit.commit();
//
//		locationManager = null;
//		if (googleMap != null){
//			googleMap.setMyLocationEnabled(false);
//			cameraPosition = googleMap.getCameraPosition();
//		}
//		if (mapView != null)
//		{ mapView.onPause(); }
//		saveMyHomeToPref(myHomeLatLng);
//		googleMap = null;
//		super.onPause();
//	}
//
//	@Override
//	public void update() {
//		if (MyFragment.CURRENT_VISIBLE_FRAGMENT == MobileHomeConstants.LOCATION_FRAGMENT_POSITION){
//			TextView warningTextView = (TextView) rootView.findViewById(R.id.map_warning_textView);
//			boolean isProximityManagerEnabled =  PreferenceManager
//					.getDefaultSharedPreferences(rootView.getContext())
//					.getBoolean(rootView.getContext().getResources().getString(R.string.preference_proximityManager_key), false);
//
//			if(isProximityManagerEnabled){
//				warningTextView.setText(rootView.getResources().getString(R.string.nullString));
//			} else {
//				warningTextView.setText(rootView.getResources().getString(R.string.proximityManagerDisabled));
//				String format = rootView.getResources().getString(R.string.mapEnableProximityManagerInfo);
//				String msg = String.format(format,
//						rootView.getResources().getString(R.string.preference_proximityManager_title),
//						rootView.getResources().getString(R.string.menu_item_settings));
//
//				SoftwareUtilities.MyInfoDialogFactory(rootView.getContext(), msg);
//			}
//		}
//
//	}
//
//	@Override
//	public void onLocationChanged(Location location) {
//		if (googleMap != null) {
//			googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//					new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM));
//		}
//
//	}
//
//	private void setMapsButtonPoistion(){
//		View myLocationButton = rootView.findViewById(0x2);
//		View zoomControls = rootView.findViewById(0x1);
//
//		if (zoomControls != null && zoomControls.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
//		    // ZoomControl is inside of RelativeLayout
//		    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) zoomControls.getLayoutParams();
//
//
//		    if (myLocationButton != null && myLocationButton.getLayoutParams() instanceof RelativeLayout.LayoutParams){
//		    	params.addRule(RelativeLayout.BELOW, myLocationButton.getId());
//			    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//		    } else {
//			    // Align it to - parent top|left
//			    params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//			    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//		    }
//
//		    // Update margins, set to 10dp
//		    final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
//		            getResources().getDisplayMetrics());
//		    params.setMargins(margin, margin, margin, margin);
//		}
//
////		if (myLocationButton != null && myLocationButton.getLayoutParams() instanceof RelativeLayout.LayoutParams){
////			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) myLocationButton.getLayoutParams();
////
////			params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
////		    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
////
////		    final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
////		            getResources().getDisplayMetrics());
////		    params.setMargins(margin, margin, margin, margin);
////		}
//	}
//
//
//	private void setUpMapIfNeeded(Bundle savedInstanceState,final LayoutInflater inflater) {
//		// Do a null check to confirm that we have not already instantiated the map.
//		if (googleMap == null) {
//			mapView = (MapView)rootView.findViewById(R.id.mapView);
//			mapView.onCreate(savedInstanceState);
//			mapView.onResume();
//
//			MapsInitializer.initialize(rootView.getContext());
//
//			googleMap = mapView.getMap();
//			googleMap.setMapType(MAP_TYPE);
//			googleMap.setMyLocationEnabled(true);
//
//			if (inflater != null) {
//				googleMap.setInfoWindowAdapter(new InfoWindowAdapter() {
//
//					@Override
//					public View getInfoWindow(Marker marker) {
//						return null;
//					}
//
//					@Override
//					public View getInfoContents(Marker marker) {
//
//						// Getting view from the layout file info_window_layout
//						View v = inflater.inflate(R.layout.marker_info_window_layout, null);
//
//						// Getting reference to the TextView to set title
//						TextView title = (TextView) v.findViewById(R.id.marker_info_window_title);
//						TextView snippet = (TextView) v.findViewById(R.id.marker_info_window_snippet);
//
//						title.setText(marker.getTitle());
//						snippet.setText(marker.getSnippet());
//
//						// Returning the view containing InfoWindow contents
//						return v;
//					}
//				});
//			}
//
//
//			googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
//				public void onInfoWindowClick(Marker marker) {
//					SoftwareUtilities.MyInfoDialogFactory(
//							rootView.getContext(),
//							R.string.mapDeleteMyHomePosition,
//							true,
//							new ActionTask() {
//
//								@Override
//								public void onPositiveResponse() {
//									if (googleMap != null)
//										{ googleMap.clear(); }
//									myHomeLatLng = null;
//									saveMyHomeToPref(myHomeLatLng);
//								}
//
//								@Override
//								public void onNeutralResponse() {}
//
//								@Override
//								public void onNegativeResponse() {}
//							});
//
//				}
//			});
//
//			googleMap.setOnMapLongClickListener(new OnMapLongClickListener() {
//
//				@Override
//				public void onMapLongClick(final LatLng point) {
//
//					if (myHomeLatLng != null) {
//						SoftwareUtilities.MyInfoDialogFactory(
//								rootView.getContext(),
//								R.string.mapOverrideMyHomePosition,
//								true,
//								new ActionTask() {
//
//									@Override
//									public void onPositiveResponse() {
//										myHomeLatLng = point;
//										saveMyHomeToPref(point);
//										try {
//											googleMap.clear();
//											addMyHomeMarker(point);
//										} catch (NullPointerException e){
//											Log.e("NullPointerException", ErrorUtilities.getExceptionMessage(e));
//										}
//
//									}
//
//									@Override
//									public void onNeutralResponse() {}
//
//									@Override
//									public void onNegativeResponse() {}
//								});
//					} else {
//						myHomeLatLng = point;
//						saveMyHomeToPref(point);
//						googleMap.clear();
//						addMyHomeMarker(point);
//					}
//				}
//			});
//
//
//			if (lastLocation != null)
//			{
////				googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
////				new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), DEFAULT_ZOOM));
//				CameraPosition camera = new CameraPosition.Builder()
//				.target(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()))      // Sets the center of the map to location user
//				.zoom(DEFAULT_ZOOM)         // Sets the zoom
////              .bearing(90)                // Sets the orientation of the camera to east
////              .tilt(40)                   // Sets the tilt of the camera to 30 degrees
//				.build();                   // Creates a CameraPosition from the builder
//
//				googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camera));
//
//			}
//
//
//			//Useless because done in onResume();
////			myHomeLatLng = getMyHomeFromPref();
////			if (myHomeLatLng != null)
////			{ addMyHomeMarker(myHomeLatLng); }
//		}
//	}
//
//	private void addMyHomeMarker (LatLng point){
//		String address = getAddressFromLocation(point, rootView.getContext());
//		googleMap.addMarker(new MarkerOptions()
//		.position(point)
//		.title(rootView.getContext().getResources().getString(R.string.myHomeMarkerTitle))
//		.snippet(address)
//		.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)));
//
//		googleMap.addCircle(
//				new CircleOptions()
//				.center(point)
//				.radius(ProximityAllert.POINT_RADIUS)
//				.strokeColor(0xff33b5e5)
//				.strokeWidth(3)
//				.fillColor(0x4033b5e5));
//	}
//
//
//	private String getAddressFromLocation (LatLng point, Context context){
//		Geocoder geocoder = new Geocoder(context, Locale.getDefault());
//
//		try {
//			List<Address> addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1);
//			StringBuilder result = new StringBuilder();
//			for (int i = 0; i < addresses.get(0).getMaxAddressLineIndex(); i++){
//				result.append(addresses.get(0).getAddressLine(i));
//				if (i < addresses.get(0).getMaxAddressLineIndex() - 1){
//					result.append(", ");
//				}
//			}
//			return result.toString();
//		} catch (IOException e) {
//			return  context.getResources().getString(R.string.addressNotFound);
//		}
//	}
//
//	private LatLng getMyHomeFromPref(){
//		Gson gson = new Gson();
//		return gson.fromJson(
//				sharedPref.getString(MobileHomeConstants.MY_HOME_LAT_LNG, null),
//				LatLng.class);
//	}
//
//	private void saveMyHomeToPref(LatLng position){
//		Gson gson = new Gson();
//		Editor edit = sharedPref.edit();
//		edit.putString(MobileHomeConstants.MY_HOME_LAT_LNG, gson.toJson(position));
//		edit.commit();
//
//		boolean isProximityManagerMoreAccurate =  PreferenceManager
//				.getDefaultSharedPreferences(rootView.getContext())
//				.getBoolean(rootView.getContext().getResources()
//				.getString(R.string.preference_moreAccurateProximityManager_key), false);
//
//		boolean isProximityManagerEnabled =  PreferenceManager
//				.getDefaultSharedPreferences(rootView.getContext())
//				.getBoolean(rootView.getContext()
//						.getResources().getString(R.string.preference_proximityManager_key), false);
//
//		ProximityAllert.disableProximityAllert(rootView.getContext(), isProximityManagerMoreAccurate);
//
//		if (isProximityManagerEnabled)
//			{ ProximityAllert.enableProximityAllert(rootView.getContext(), position, isProximityManagerMoreAccurate); }
//	}
//
//
//}
