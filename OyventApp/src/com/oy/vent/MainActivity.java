package com.oy.vent;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.oy.vent.fragment.CommunityFragment;
import com.oy.vent.fragment.ImageGridFragment;
import com.oy.vent.fragment.SettingsFragment;
import com.oy.vent.model.UserInfo;
import com.oy.vent.slidingmenu.adapter.NavDrawerListAdapter;
import com.oy.vent.slidingmenu.model.NavDrawerItem;
import com.oy.vent.util.GPSTracker;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;


public class MainActivity extends FragmentActivity {

	// Debug tag, for logging
    static final String TAG = "MainActivity.java";    
    private static final String PREF_FILE_NAME = "OYVENTDATA";//user hard disk file name
	
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	// nav drawer title
	private CharSequence mDrawerTitle;

	// used to store app title
	private CharSequence mTitle;

	// slide menu items
	private String[] navMenuTitles;
	private TypedArray navMenuIcons;

	private ArrayList<NavDrawerItem> navDrawerItems;
	private NavDrawerListAdapter adapter;
	
	//user info class
	private UserInfo userInfo = null; 
	
	private GPSTracker gpsTracker = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_ACTION_BAR);//to show the action bar
		setContentView(R.layout.activity_main);
		
		// check if GPS enabled
        gpsTracker = new GPSTracker(this);
        if (!gpsTracker.canGetLocation()){
        	gpsTracker.showSettingsAlert();
        	return;
        }		

        //sliding menu items
		mTitle = mDrawerTitle = getTitle();		
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);// load slide menu items		
		navMenuIcons = getResources()// nav drawer icons from resources
				.obtainTypedArray(R.array.nav_drawer_icons);		
		navDrawerItems = new ArrayList<NavDrawerItem>();// create drawer array and add nav drawer items to array		
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));//home: all feeds		
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));//community feeds
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));//settings		
		navMenuIcons.recycle();//Recycle the typed array		
		
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);//the drawer list menu
		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());//menu item click event	
		adapter = new NavDrawerListAdapter(getApplicationContext(),
				navDrawerItems);//create the adapter along with menu items
		mDrawerList.setAdapter(adapter);//setting the nav drawer list adapter

		// enabling action bar app icon and behaving it as toggle button
		getActionBar().setDisplayHomeAsUpEnabled(true);
		//getActionBar().setHomeButtonEnabled(true);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);//drawer layout
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, //nav menu toggle icon
				R.string.app_name, // nav drawer open - description for accessibility
				R.string.app_name // nav drawer close - description for accessibility
		) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				// calling onPrepareOptionsMenu() to show action bar icons
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				// calling onPrepareOptionsMenu() to hide action bar icons
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			// on first time display view for first nav item
			displayView(0);
		}
	}
	
	public GPSTracker getGPSTracker(){
		return gpsTracker;
	}

	/**
	 * Slide menu item click listener
	 * */
	private class SlideMenuClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// display view for selected nav drawer item
			displayView(position);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// toggle nav drawer on selecting action bar app icon/title
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action bar actions click
		switch (item.getItemId()) {
		case R.id.action_logout:
			removeUser();
    		//setResult(5);//return to login screen
    		goToLoginActivity();
    		finish();
		case R.id.action_settings:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {	 
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_container);
	    fragment.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {            
        	//setResult(6);        	
        	finish();    	
        }
		return false;
    }
	
	@Override
	public void onBackPressed() {
	   	super.onBackPressed();    	
	}
		
	@Override
	protected void onStart() {
	    super.onStart();
	        
	    userInfo = getUserInfo();
		if(userInfo == null)
		{
			//setResult(5);
			finish();
		}
	}
	
	//go to the Login Screen
	protected void goToLoginActivity(){
		Intent intent = new Intent(MainActivity.this,LoginActivity.class);
		//startActivityForResult(intent, REQUEST_CODE_LOGIN_CLASS);
		startActivity(intent);
		finish();
	}
	
	//remove user info from hard disk
  	protected void removeUser()
  	{
  		SharedPreferences sharedPref = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
  		sharedPref.edit().remove("userInfo").commit();
  	}

	//read user info from hard disk
	private UserInfo getUserInfo()
	{		
		SharedPreferences sharedPref = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);		
		Gson gson = new Gson();
		String json = sharedPref.getString("userInfo", "");
		UserInfo usr = gson.fromJson(json, UserInfo.class);		
		return usr;
	}
		
		
	/* *
	 * Called when invalidateOptionsMenu() is triggered
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// if nav drawer is opened, hide the action items
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Diplaying fragment view for selected nav drawer list item
	 * */
	private void displayView(int position) {
		// update the main content by replacing fragments
		//ImageGridFragment fragment = null;
		Fragment fragment = null;
		
		switch (position) {	
		case 0:
			fragment =  new ImageGridFragment();//home-all local photo feeds
			break;
		case 1:
			fragment = new CommunityFragment();//only community feeds
			break;
		case 2:
			fragment = new SettingsFragment();//settings screen
			break;

		default:
			break;
		}

		if (fragment != null) {
			FragmentManager fragmentManager =  getSupportFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.frame_container, fragment).commit();
			
		/*if (fragment != null && getSupportFragmentManager().findFragmentByTag(TAG) == null) {
	            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	            ft.replace(R.id.frame_container, fragment, TAG);
	            ft.commit();*/
	    
			

			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			mDrawerList.setSelection(position);
			setTitle(navMenuTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
		} else {
			// error in creating fragment
			Log.e("MainActivity", "Error in creating fragment");
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}
}
