package it.eduman.mobileHome2;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Locale;

import it.eduman.mobileHome2.commons.MobileHomeConstants;
import it.eduman.mobileHome2.commons.MobileHomeConstants.Config;
import it.eduman.mobileHome2.commons.MobileHomeImageLoader;

//import it.eduman.mobileHome2.deprecated.proxymityAllert.ProximityAllert;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	private SectionsPagerAdapter mSectionsPagerAdapter;
	private MyFragment homeFragment = null;
	private MyFragment computerFragment = null;
	private MyFragment mainFragment = null;
	private MyFragment weatherFragment = null;
	private MyFragment plotsFragment = null;
	private MyFragment userFragment = null;
	private MyFragment videosurveillanceFragment = null;
	private MyFragment rulemanagerFragment = null;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	@SuppressWarnings("unused")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		if (Config.DEVELOPER_MODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDialog().build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyDeath().build());

		}

		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		MobileHomeImageLoader.initImageLoader(this);

		this.mainFragment = new MainSectionFragment();
		this.homeFragment = new HomeSectionFragment();
		this.plotsFragment = new DashboardSectionFragment();
		this.userFragment = new UserSectionFragment();
		this.videosurveillanceFragment = new VideoSurveillanceSectionFragment();
		this.computerFragment = new ComputerSectionFragment();
		this.weatherFragment = new WeatherSectionFragment();
		this.rulemanagerFragment = new RuleManagerSectionFragment();

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
//		mSectionsPagerAdapter = new SectionsPagerAdapter(
//				getSupportFragmentManager());

		mSectionsPagerAdapter = new SectionsPagerAdapter(
				this.getApplicationContext(),
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}

//		ProximityAllert.registerReceiver(getApplicationContext());
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
	}

	@Override
	public void onStop(){
		super.onStop();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return MenuUtilities.myMenuFactory(this, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return MenuUtilities.onMyOptionsItemSelected(
				this, item.getItemId());
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		this.showFragment(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		this.showFragment(tab.getPosition());
	}

	public void nextPage(int tabNumber)
	{
		mViewPager.setCurrentItem(tabNumber);
	}

	private void showFragment(int position){
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(position);

		//TODO commented fragments  to be enabled
		if (mainFragment != null &&
				homeFragment != null &&
				plotsFragment != null &&
				userFragment != null &&
				videosurveillanceFragment != null &&
				rulemanagerFragment != null &&
				computerFragment != null &&
				weatherFragment != null) {
			switch (position) {
				case MobileHomeConstants.MAIN_FRAGMENT_POSITION:
					MyFragment.CURRENT_VISIBLE_FRAGMENT =
							MobileHomeConstants.MAIN_FRAGMENT_POSITION;
					mainFragment.update();
					break;
				case MobileHomeConstants.HOME_FRAGMENT_POSITION:
					MyFragment.CURRENT_VISIBLE_FRAGMENT =
							MobileHomeConstants.HOME_FRAGMENT_POSITION;
					homeFragment.update();
					break;
				case MobileHomeConstants.PLOTS_FRAGMENT_POSITION:
					MyFragment.CURRENT_VISIBLE_FRAGMENT =
							MobileHomeConstants.PLOTS_FRAGMENT_POSITION;
					plotsFragment.update();
					break;
				case MobileHomeConstants.USER_FRAGMENT_POSITION:
					MyFragment.CURRENT_VISIBLE_FRAGMENT =
							MobileHomeConstants.USER_FRAGMENT_POSITION;
					userFragment.update();
					break;
				case MobileHomeConstants.VIDEOSURVEILLANCE_FRAGMENT_POSITION:
					MyFragment.CURRENT_VISIBLE_FRAGMENT =
							MobileHomeConstants.VIDEOSURVEILLANCE_FRAGMENT_POSITION;
					videosurveillanceFragment.update();
					break;
				case MobileHomeConstants.COMPUTER_FRAGMENT_POSITION:
					MyFragment.CURRENT_VISIBLE_FRAGMENT =
							MobileHomeConstants.COMPUTER_FRAGMENT_POSITION;
					computerFragment.update();
					break;
				case MobileHomeConstants.WEATHER_FRAGMENT_POSITION:
					MyFragment.CURRENT_VISIBLE_FRAGMENT =
							MobileHomeConstants.WEATHER_FRAGMENT_POSITION;
					weatherFragment.update();
					break;
				case MobileHomeConstants.RULEMANAGER_FRAGMENT_POSITION:
					MyFragment.CURRENT_VISIBLE_FRAGMENT =
							MobileHomeConstants.RULEMANAGER_FRAGMENT_POSITION;
					rulemanagerFragment.update();
					break;

			}
		}

	}


	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		private Context context;

		public SectionsPagerAdapter(Context context, FragmentManager fm) {
			super(fm);
			this.context = context;
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			MyFragment fragment = null;
			Bundle args = new Bundle();

			//TODO commented fragments  to be enabled
			switch (position){
				case MobileHomeConstants.MAIN_FRAGMENT_POSITION:
					fragment = mainFragment;
					args.putInt(MobileHomeConstants.ARG_SECTION_NUMBER, position + 1);
					fragment.setArguments(args);
					break;
				case MobileHomeConstants.HOME_FRAGMENT_POSITION:
					fragment = homeFragment;
					args.putInt(MobileHomeConstants.ARG_SECTION_NUMBER, position + 1);
					fragment.setArguments(args);
					break;
				case MobileHomeConstants.PLOTS_FRAGMENT_POSITION:
					fragment = plotsFragment;
					args.putInt(MobileHomeConstants.ARG_SECTION_NUMBER, position + 1);
					fragment.setArguments(args);
					break;
				case MobileHomeConstants.USER_FRAGMENT_POSITION:
					fragment = userFragment;
					args.putInt(MobileHomeConstants.ARG_SECTION_NUMBER, position + 1);
					fragment.setArguments(args);
					break;
				case MobileHomeConstants.VIDEOSURVEILLANCE_FRAGMENT_POSITION:
					fragment = videosurveillanceFragment;
					args.putInt(MobileHomeConstants.ARG_SECTION_NUMBER, position + 1);
					fragment.setArguments(args);
					break;
				case MobileHomeConstants.COMPUTER_FRAGMENT_POSITION:
					fragment = computerFragment;
					args.putInt(MobileHomeConstants.ARG_SECTION_NUMBER, position + 1);
					fragment.setArguments(args);
					break;
				case MobileHomeConstants.WEATHER_FRAGMENT_POSITION:
					fragment = weatherFragment;
					args.putInt(MobileHomeConstants.ARG_SECTION_NUMBER, position + 1);
					fragment.setArguments(args);
					break;
				case MobileHomeConstants.RULEMANAGER_FRAGMENT_POSITION:
					fragment = rulemanagerFragment;
					args.putInt(MobileHomeConstants.ARG_SECTION_NUMBER, position + 1);
					fragment.setArguments(args);
					break;

			}


			return fragment;
		}

		@Override
		public int getCount() {
			return MobileHomeConstants.TOTAL_TAB_PAGES;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
				case MobileHomeConstants.MAIN_FRAGMENT_POSITION:
					return this.context.getString(R.string.title_main).toUpperCase(l);
				case MobileHomeConstants.HOME_FRAGMENT_POSITION:
					return this.context.getString(R.string.title_home).toUpperCase(l);
				case MobileHomeConstants.PLOTS_FRAGMENT_POSITION:
					return this.context.getString(R.string.title_dashboard).toUpperCase(l);
				case MobileHomeConstants.USER_FRAGMENT_POSITION:
					return this.context.getString(R.string.title_user).toUpperCase(l);
				case MobileHomeConstants.VIDEOSURVEILLANCE_FRAGMENT_POSITION:
					return this.context.getString(R.string.title_videosurveillance).toUpperCase(l);
				case MobileHomeConstants.COMPUTER_FRAGMENT_POSITION:
					return this.context.getString(R.string.title_computer).toUpperCase(l);
				case MobileHomeConstants.WEATHER_FRAGMENT_POSITION:
					return this.context.getString(R.string.title_weather).toUpperCase(l);
				case MobileHomeConstants.RULEMANAGER_FRAGMENT_POSITION:
					return this.context.getString(R.string.title_rulemanager).toUpperCase(l);
			}
			return null;
		}
	}



}
