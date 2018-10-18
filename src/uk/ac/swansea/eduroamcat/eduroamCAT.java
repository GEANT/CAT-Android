//*******************
//Author: Gareth Ayres <g.j.ayres@swansea.ac.uk>
//SENSE Project
//https://github.com/GEANT/CAT-Android
//*******************

package uk.ac.swansea.eduroamcat;

import java.util.ArrayList;
import java.util.List;
import android.app.NotificationManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import android.net.Uri;

public class eduroamCAT extends FragmentActivity implements ActionBar.TabListener
{
		private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
		//final static vars
	    private static WifiManager wifi = null;
	    static WifiController wifiCon = null;
	    static BroadcastReceiver wifiStatus = null;
	    static WifiConfigAPI18 wifiConfig18 = null;
	    static WiFiProfile wifiProfile = null;
	    static ArrayList<ConfigProfile> profiles = null;
	    final Intent configIntent = getIntent();
 		final IntentFilter intentFilter = new IntentFilter();
 		ProfilesStorage db = new ProfilesStorage(this);
 		static String state="";
 		final static String appSSID="eduroam";	
        final Fragment connectFragment = new ConnectFragment();
        final Fragment configureFragment = new ConfigureFragment();
        final static Fragment statusFragment = new StatusFragment();
        private static boolean advanced_mode= false;
		public static boolean downloaded=false;
		public final static int currentapiVersion = android.os.Build.VERSION.SDK_INT;
	    
	    //Debug method to print to logd
	    public static void debug(String msg)
	    {
	    	if (BuildConfig.DEBUG) Log.d("eduroamCAT", msg);
	    }
	    
	    //get advanced mode toggled
	    public static boolean isAdvancedMode()
	    {
	    	return advanced_mode;
	    }
	    
	    //Defines WiFi Manager
		public static WifiManager getWifiManager()
		{
			return wifi;
		}
		
		public static void setSummary()
		{
			((StatusFragment) statusFragment).latestSummary();
		}

		//reconect if profile installed
		public static void reconnect()
		{
			List <WifiConfiguration> currentConfigs;
			if (wifi.isWifiEnabled()) 
			{
				currentConfigs = wifi.getConfiguredNetworks();
				for (WifiConfiguration currentConfig : currentConfigs) 
				{
					eduroamCAT.debug(currentConfig.SSID);
					if (currentConfig.SSID.equals("\"eduroam\""))
						{
							wifi.disconnect();   
							boolean enabled = wifi.enableNetwork(currentConfig.networkId, true);   
							debug("Setting ("+currentConfig.SSID+") to Enabled returned " + enabled );
						}
				}
			}
		}
		
		public void loadProfiles()
		{
				if (db.numberOfRowsEAP()>0)
				{  	
					profiles=db.getEAPProfiles();
				}
				else
				{
					debug("No profiles found in DB");
				}
		}
				
		public static void setState(String statea)
		{
			state=statea;
		}
 
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		eduroamCAT.debug("Starting on create for main activity");
		//set focus off text fields
//		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		      
		//setup the action bar (tabs at top of app)
		final ActionBar actionBar = getActionBar();
		actionBar.setTitle(R.string.app_name);
		
//		//hide ActionBar when not used
//		getWindow().
//		  getDecorView().
//		  setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		
		//Action Bar Tabs
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		  
		  // create new tabs and set up the titles of the tabs
          ActionBar.Tab connectTab = actionBar.newTab().setText(getString(R.string.title_tab1));
          ActionBar.Tab configureTab = actionBar.newTab().setText(getString(R.string.title_tab2));
          ActionBar.Tab statusTab = actionBar.newTab().setText(getString(R.string.title_tab3));

          // bind the fragments to the tabs - set up tabListeners for each tab
          connectTab.setTabListener(new TabsListener(connectFragment,getApplicationContext()));
          configureTab.setTabListener(new TabsListener(configureFragment,getApplicationContext()));
          statusTab.setTabListener(new TabsListener(statusFragment,getApplicationContext()));


          // add the tabs to the action bar
          actionBar.addTab(connectTab);
          actionBar.addTab(configureTab);
          actionBar.addTab(statusTab);
          
          //Setup WiFi Module
          if (wifi==null) wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
  		
  		//Setup wifi status broadcast receivers
  		  if (wifiStatus==null) wifiStatus = new WifiStatus(getSystemService(Context.CONNECTIVITY_SERVICE));
  		
  		//setup some intents for debugging wireless, supplicant and network state

  		 intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
  		 intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
  		 intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
  		 intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.EXTRA_SUPPLICANT_ERROR);
		intentFilter.addAction(WifiManager.EXTRA_SUPPLICANT_CONNECTED);
		intentFilter.addAction(WifiManager.EXTRA_WIFI_STATE);
		intentFilter.addAction(WifiManager.EXTRA_WIFI_INFO);
		registerReceiver(wifiStatus, intentFilter);
  		//Initial checks
  		 if (wifiCon==null) wifiCon = new WifiController(this,getSystemService(Context.CONNECTIVITY_SERVICE));
  		if (wifiCon.isWifiEnabled() && wifiCon.isSupplicantOK())
  		{
  			//ok
  			debug("Wifi and Supplicant OK...");
  			StatusFragment.setDebug(getString(R.string.status_wifi_supp_ok));
  			//ConnectFragment.setStatus("WiFi Adapter OK");
  		}
  		else
  		{
  			//no ok, warn user
  			Toast.makeText(this, getString(R.string.status_wifi_disabled),Toast.LENGTH_SHORT).show();
  			StatusFragment.setDebug(getString(R.string.status_wifi_supp_problem));
  			debug("WiFi disabled on device...");
  			//ConnectFragment.setStatus("WiFi Adapter disbaled");
  		}
  		
  		//WiFi Profile
  		if (wifiProfile==null) wifiProfile = new WiFiProfile();
  		//DB check for wifi profile
  		if (db.numberOfRowsWiFi()>0)
  		{
  			debug("num rows wifi="+db.numberOfRowsWiFi());
  			eduroamCAT.wifiProfile=db.getWiFi(0);
  			debug("SSID FOUND="+eduroamCAT.wifiProfile.getSSID());
  		}
  		else
  		{
  			debug("num rows wifi="+db.numberOfRowsWiFi());
  			eduroamCAT.wifiProfile.hasError();
  		}
  		
  		//wifiConfig = new WifiConfig();
  		if (wifiConfig18==null) wifiConfig18 = new WifiConfigAPI18();
 
  		  		
  		//EAP Profile
  		ArrayList <ConfigProfile> profiles = new ArrayList <ConfigProfile>();
  		//draw tabs on top of app
  		
  		if (profiles.isEmpty() && db.numberOfRowsEAP()>0 && db.numberOfRowsAUTH()>0)
  		{
  			loadProfiles();
  			actionBar.setSelectedNavigationItem(0);
  			//nothing yet
  		}
  		else
  		{
  			debug("No prfoiles found...");
  			actionBar.setSelectedNavigationItem(1);
  		}		
  		
		actionBar.show();
	}
	
	
    public static void alertUser(String message, String title, Activity activ)
    {
		new AlertDialog.Builder(activ)
	    .setTitle(title)
	    .setMessage(message)
	    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	        	eduroamCAT.debug("OK clicked");
	        }
	     })
	     .show();
    }

	public static void notifyConnected(Activity activ,Context context)
	{
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
		int mId=1;
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(activ)
						.setSmallIcon(R.drawable.ic_launcher)
						.setContentTitle(activ.getString(R.string.notification_title_connected))
						.setContentText(activ.getString(R.string.notification_message_connected));
		notificationManager.notify(mId, mBuilder.build());
	}

	@Override
	//Menu in top right of app
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		
		//Add menu opetion in rop right of app
		 MenuInflater inflater = getMenuInflater();
		    //inflater.inflate(R.menu.settings, menu);
			inflater.inflate(R.menu.selectfile, menu);
			inflater.inflate(R.menu.advanced, menu);
		    inflater.inflate(R.menu.support, menu);
		    inflater.inflate(R.menu.about, menu);
		    inflater.inflate(R.menu.version, menu);
		    //inflater.inflate(R.menu.exit, menu);
		return true;
	}
	
	@Override
	  public void onRestoreInstanceState(Bundle savedInstanceState) {
	    // Restore the previously serialized current tab position.
	    if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
	      getActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
	    }
	    registerReceiver(wifiStatus, intentFilter);
	  }

	  @Override
	  public void onSaveInstanceState(Bundle outState) {
	    // Serialize the current tab position.
	    outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
	        .getSelectedNavigationIndex());
	  }

	  public void onTabSelected(ActionBar.Tab tab,
	      FragmentTransaction fragmentTransaction) {

	  }

	  public void onTabUnselected(ActionBar.Tab tab,
	      FragmentTransaction fragmentTransaction) {
	  }

	  public void onTabReselected(ActionBar.Tab tab,
	      FragmentTransaction fragmentTransaction) {
	  }
	  
	  //This method controls the buttons in the top right of the app
      @Override
      public boolean onOptionsItemSelected(MenuItem item) {
                  switch (item.getItemId()) {
                  case R.id.settings:
                      Toast.makeText(this, getString(R.string.settings_menu),
                      Toast.LENGTH_LONG).show();
                      return true;
                  case R.id.version:
                	  PackageInfo pInfo;
                	  String version="unknown";
					try {
						pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
						version = pInfo.versionName;
					} catch (NameNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                      Toast.makeText(this,"Version:"+version,
                      Toast.LENGTH_LONG).show();
                      return true;                               
                  case R.id.advanced:
                      Toast.makeText(this, getString(R.string.advanced_menu),
                      Toast.LENGTH_LONG).show();
                      if (advanced_mode) {
                    	     Toast.makeText(this, getString(R.string.advanced_off),
                                     Toast.LENGTH_LONG).show();
                    	  advanced_mode=false;
                      }
                      else {
                    	  advanced_mode=true;
                          Toast.makeText(this, getString(R.string.advanced_on),
                                  Toast.LENGTH_LONG).show();  
                      }
                      Intent intent = getIntent();
                      finish();
                      startActivity(intent);
                      return true;
					  case R.id.selectfile:
						  Toast.makeText(this, getString(R.string.select_menu),
								  Toast.LENGTH_LONG).show();
						  Intent selectIntent = new Intent()
								  .setType("*/*")
								  .setAction(Intent.ACTION_GET_CONTENT);
						  startActivityForResult(Intent.createChooser(selectIntent, "Select a file"), 123);
						  return true;
                  case R.id.support:
                	  if (db.numberOfRowsEAP()>0)
                	  {
                		  if (profiles.size()>0 && profiles!=null)
                		  if (profiles.get(0).hasHelpdeskInfo())
                		  {
							  String displayName="";
							  String helpEmail="";
							  String helpPhone="";
							  String helpURL="";
                			  if (profiles.get(0).getHelpdeskURL()!=null) { helpURL=profiles.get(0).getHelpdeskURL().toString(); }
							  if (profiles.get(0).getHelpdeskPhoneNumber()!=null) { helpPhone=profiles.get(0).getHelpdeskPhoneNumber();}
							  if (profiles.get(0).getSupportEmails()!=null) { helpEmail=profiles.get(0).getSupportEmails(); }
							  if (profiles.get(0).getDisplayName()!=null) { displayName=profiles.get(0).getDisplayName();}
                			  String message=getString(R.string.support_message2,displayName);
                			  message+=" ";
                			  message+=getString(R.string.support_message3)+"\n";
                			  if (helpEmail.length()>0) message+=getString(R.string.support_email)+helpEmail+"\n";
                			  if (helpPhone.length()>0) message+=getString(R.string.support_phone)+helpPhone+"\n";
                			  if (helpURL.length()>0) message+=getString(R.string.support_web)+helpURL;
                			  alertUser(message,getString(R.string.support_title),this);
                		  }
                		  else alertUser(getString(R.string.support_message1),getString(R.string.support_title),this);
                	  }
                	  else alertUser(getString(R.string.support_message1),getString(R.string.support_title),this);
                      return true;
                  case R.id.about:
                			  alertUser(getString(R.string.about_text),getString(R.string.support_title),this);
                      return true;
                      
//                  case R.id.exit:
//                      Intent intent = new Intent(Intent.ACTION_MAIN);
//                      intent.addCategory(Intent.CATEGORY_HOME);
//                      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                      startActivity(intent);
//                      finish();
//                      //System.exit(0);
//                      return true;
                  }
                  return false;
      }


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==123 && resultCode==RESULT_OK) {
			Uri selectedfile = data.getData();
			eduroamCAT.debug("Got eap-config uri of:"+selectedfile);
			//content://com.android.providers.downloads.documents/document/8
			Intent download = new Intent(this, EAPMetadata.class);
			download.setData(selectedfile);
			startActivity(download);
			eduroamCAT.debug("started activity");
		}
	}

  	@Override
  	public void onDestroy()
  	{
  		Log.d("eduroamCAT", "***** mainActivity onDestroy()");
  		try
  		{
  			unregisterReceiver(wifiStatus);
  		}
  		catch (Exception e)
  		{
  			debug("Already unregistered reciever...");
  		}
  		super.onDestroy();
  		finish();
  	}
  	
  	@Override
  	protected void onResume() 
  	{
  		registerReceiver(wifiStatus, intentFilter);	
  		Log.d("eduroamCAT", "***** mainActivity onResume()");
  		debug("num rows wifi="+db.numberOfRowsWiFi());
  		super.onResume();
  	}
  	
  	@Override
  	protected void onRestart() 
  	{
  		registerReceiver(wifiStatus, intentFilter);	
  		Log.d("eduroamCAT", "***** mainActivity onRestart()");
  		super.onRestart();
  	}

  	@Override
  	protected void onPause() 
  	{
  		Log.d("eduroamCAT", "***** mainActivity onPause()");
  		unregisterReceiver(wifiStatus);
  		super.onPause();
  	}
  	
  	@Override
  	protected void onStart() {
  		registerReceiver(wifiStatus, intentFilter);	
  		Log.d("eduroamCAT", "***** mainActivity onStart()");
  		super.onStart();
  	}

}
