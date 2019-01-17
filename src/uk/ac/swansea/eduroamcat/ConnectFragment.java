//*******************
//Author: Gareth Ayres <g.j.ayres@swansea.ac.uk>
//SENSE Project
//https://github.com/GEANT/CAT-Android
//*******************
package uk.ac.swansea.eduroamcat;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class ConnectFragment extends Fragment implements OnClickListener
{
            
	static TextView progressText;
	static Spinner authMethodSpinner,ssidSpinner;
	private static String[] no_authmethods_array = new String[1];
	private static String[] no_ssid_array = new String[1];
	static EditText username,password;
	static Button connect,rescan;
	AlertDialog alert;
	private static WifiManager wifim;
	Activity activity = getActivity();
	static TextView state1,state2,state3,state4,state5,state6,warning,passText,userText;
	static ImageView state1_image,state2_image,state3_image,state4_image,state5_image,state6_image;
	static LinearLayout feedback,state5_layout,state2_layout,state3_layout, state6_layout;
	private static WifiManager wifi = null;
	static Boolean installedOK = false;
	
    public static void setStatus(String status)
    {
    	if (progressText!=null && status!=null) {
			progressText.setText("Status:" + status);
			showCurrentState();
//    	if (status.contains("CONNECTED"))
//    	{
//    		//prog.setVisibility(0);
//    		//prog.setProgress(prog.getMax());
//    		String cssid = wifim.getConnectionInfo().getSSID();
//    		if (!cssid.equals("0x")) progressText.append("["+cssid+"]");
//    	}
//    	else
//    	{
//    		//prog.setVisibility(1);
//    	}
		}
    }

	//set installed boolean
	static public void setProfileInstalled(Boolean installed)
	{
		installedOK = installed;
	}

	//get installed boolean
	static public Boolean getProfileInstalled()
	{
		return installedOK;
	}


	//show state of current eduroam config
    public static void showCurrentState()
    {
    	feedback.setVisibility(View.VISIBLE);
    	if (wifi==null) wifi = eduroamCAT.getWifiManager();
    	String summary_template="";
    	String state2_text="";
    	String state3_text="";
    	String state4_text="";
    	String state5_text="";
    	String state6_text="";
		boolean pwdortls=false;
    	List <WifiConfiguration> currentConfigs;
    		currentConfigs = wifi.getConfiguredNetworks();
    		int found=0;
    		if (currentConfigs!=null)
    		for (WifiConfiguration currentConfig : currentConfigs) 
    		{
    			if (currentConfig.SSID!=null)
    			if (currentConfig.SSID.equals("\"eduroam\""))
    			{
    				found++;
    				//set ticks to visible.
    					summary_template=eduroamCAT.wifiCon.checkEduroamSSID();
    					if (summary_template.contains("\"#000001\"")) state1_image.setImageResource(R.drawable.cross);
    					else if (summary_template.contains("\"#000000\"")) state1_image.setImageResource(R.drawable.question);
    					else if (summary_template.contains("\"black\"")) state1_image.setImageResource(R.drawable.tick);
    					else  state1_image.setImageResource(R.drawable.cross);
    					state2_text=eduroamCAT.wifiCon.checkEduroamAnon();
    					if (state2_text.contains("\"#000001\"")) state2_image.setImageResource(R.drawable.cross);
    					else if (state2_text.contains("\"#000000\"")) state2_image.setImageResource(R.drawable.question);
    					else if (state2_text.contains("\"black\"")) state2_image.setImageResource(R.drawable.tick);
    					else  state2_image.setImageResource(R.drawable.cross);
    					state3_text=eduroamCAT.wifiCon.checkEduroamUser();
    					if (state3_text.contains("\"#000001\"")) state3_image.setImageResource(R.drawable.cross);
    					else if (state3_text.contains("\"#000000\"")) state3_image.setImageResource(R.drawable.question);
    					else if (state3_text.contains("\"black\"")) state3_image.setImageResource(R.drawable.tick);
    					else  state3_image.setImageResource(R.drawable.cross);
    					state4_text=eduroamCAT.wifiCon.checkEduroamEAP();
    					if (state4_text.contains("\"#000001\"")) state4_image.setImageResource(R.drawable.cross);
    					else if (state4_text.contains("\"#000000\"")) state4_image.setImageResource(R.drawable.question);
    					else if (state4_text.contains("\"black\"")) state4_image.setImageResource(R.drawable.tick);
    					else  state4_image.setImageResource(R.drawable.cross);
						if (currentConfig.enterpriseConfig.getEapMethod()!= WifiEnterpriseConfig.Eap.PWD) {
							pwdortls=false;
							state5_text = eduroamCAT.wifiCon.checkEduroamCA();
							if (state5_text.contains("\"#000001\""))
								state5_image.setImageResource(R.drawable.cross);
							else if (state5_text.contains("\"#000000\""))
								state5_image.setImageResource(R.drawable.question);
							else if (state5_text.contains("\"black\""))
								state5_image.setImageResource(R.drawable.tick);
							else state5_image.setImageResource(R.drawable.cross);
						}
						else pwdortls=true;
    					state6_text=eduroamCAT.wifiCon.checkEduroamSubject();
    					if (state6_text.contains("\"#000001\"")) state6_image.setImageResource(R.drawable.cross);
    					else if (state6_text.contains("\"#000000\"")) state6_image.setImageResource(R.drawable.question);
    					else if (state6_text.contains("\"black\"")) state6_image.setImageResource(R.drawable.tick);
    					else  state6_image.setImageResource(R.drawable.cross);
    			}
    		}
    		if (found>0) {
			Spanned idp_summary = Html.fromHtml(summary_template);
			state1.setText(idp_summary);
			idp_summary = Html.fromHtml(state2_text);
			state2.setText(idp_summary);
			idp_summary = Html.fromHtml(state3_text);
			state3.setText(idp_summary);
			idp_summary = Html.fromHtml(state4_text);
			state4.setText(idp_summary);
				if (!pwdortls) {
					idp_summary = Html.fromHtml(state5_text);
					state5.setText(idp_summary);
				}
				else {
					//ca cert feedback
					state5_image.setVisibility(View.GONE);
					state5.setVisibility(View.GONE);
					state5_layout.setVisibility(View.GONE);
					//annon id feedback
					state2_image.setVisibility(View.GONE);
					state2.setVisibility(View.GONE);
					state2_layout.setVisibility(View.GONE);
					//subject match feedback
					state6_image.setVisibility(View.GONE);
					state6.setVisibility(View.GONE);
					state6_layout.setVisibility(View.GONE);
				}
			idp_summary = Html.fromHtml(state6_text);
			state6.setText(idp_summary);
			//state1.setMovementMethod(new ScrollingMovementMethod());
    		}
    		else feedback.setVisibility(View.GONE);
    		
    		//remove password field if TLS
    		if (eduroamCAT.profiles!=null)
      		  if (eduroamCAT.profiles.size()>0 && eduroamCAT.profiles!=null)
      		  if (!eduroamCAT.profiles.get(0).isError())
      		  {
      			  if (eduroamCAT.profiles.get(0).getAuthenticationMethod(0).getOuterEAPType()==13)
      			  {
      				  password.setVisibility(View.GONE);
      				  passText.setVisibility(View.GONE);
      				  //remove anon identity warn for TLS
					  state2_image.setVisibility(View.GONE);
					  state2.setVisibility(View.GONE);
					  state2_layout.setVisibility(View.GONE);
					  //remove user id warning
					  state3_image.setVisibility(View.GONE);
					  state3.setVisibility(View.GONE);
					  state3_layout.setVisibility(View.GONE);
      				  username.setVisibility(View.GONE);
      				  userText.setVisibility(View.GONE);
      			  }
      		  }
    }
        
	public String getStoredUsername()
	{
		ProfilesStorage db = new ProfilesStorage(getActivity());
		String username="";
			if (db.numberOfRowsUSER()>0)
			{  	
				username=db.getUser();
				if (username.isEmpty()) username = "";
			}
			else
			{
				eduroamCAT.debug("No usernames found in DB");
			}
			db.close();
			return username;
	}
	
    public static void setAuthSpinnerVisible(boolean vis)
    {
    	if (vis) authMethodSpinner.setVisibility(View.VISIBLE);
    	else authMethodSpinner.setVisibility(View.GONE);
    }
    
    public static void setSSIDSpinnerVisible(boolean vis)
    {
    	if (vis) ssidSpinner.setVisibility(View.VISIBLE);
    	else ssidSpinner.setVisibility(View.GONE);
    }

    public void setAuthMethodSpinner()
    {
    	no_authmethods_array[0]="";
    	String[] authmethods_array=null;
    	ArrayList<String> spinnerItems = new ArrayList<String>();
        //get availabel auth methods and put them in an array
    	eduroamCAT.debug("Auth Spinner init");
    	if (eduroamCAT.profiles!=null) { 
		if (eduroamCAT.profiles.size()>0 && eduroamCAT.wifiProfile.hasError()==false)
		{  	
			eduroamCAT.debug("connect button:"+connect.isEnabled());
			connect.setEnabled(true);
			//String wifiTmp=eduroamCAT.wifiProfile.getSSID()+":("+eduroamCAT.wifiProfile.getAuthType()+"/"+eduroamCAT.wifiProfile.getEncryptionType();
			ConfigProfile aProfile = eduroamCAT.profiles.get(0);
			String authMethod="";
			if (aProfile.getNumberAuthenticationMethods()>0 && aProfile.isError()==false)
			{
				for (int i=0;i<aProfile.getNumberAuthenticationMethods();i++)
				{
					if (aProfile.getAuthenticationMethod(i).isError()) continue;
					//AuthenticationMethod aAuthMethod = aProfile.getAuthenticationMethod(i); 
					int count = i +1;
					authMethod = " Auth Method #" + count;
					spinnerItems.add(authMethod);
				}
			}
			else
			{
				no_authmethods_array[0]=getString(R.string.eapprofileMissing_title);
				connect.setEnabled(false);
			}
		}
		else
		{
			no_authmethods_array[0]=getString(R.string.eapprofileMissing_title);
		}
		}
		else
		{
			no_authmethods_array[0]=getString(R.string.eapprofileMissing_title);
		}
    	
    	authmethods_array=null;
		//convert spinnerItems to String[]
    	if (spinnerItems.size()>0)
    	{
    		authmethods_array=new String[spinnerItems.size()];
		for (int i=0;i<spinnerItems.size();i++)
		{
			authmethods_array[i]=spinnerItems.get(i);
		}
    	}
		
    	if (authmethods_array==null)authmethods_array= no_authmethods_array;
    	eduroamCAT.debug("got auth methods array size:"+authmethods_array.length);
        
    	if (no_authmethods_array[0].length()>0)  authmethods_array= no_authmethods_array;
        
    	ArrayAdapter <String>adapter = new ArrayAdapter <String>(this.getActivity(), android.R.layout.simple_spinner_item, authmethods_array);
    	authMethodSpinner.setAdapter(adapter);

    }
    
    public void setSSIDSpinner()
    {
    	ProfilesStorage db = new ProfilesStorage(getActivity());
    	no_ssid_array[0]="";
    	String[] ssid_array=null;
    	ArrayList<String> spinnerItems2 = new ArrayList<String>();
        //get availabel auth methods and put them in an array
    	eduroamCAT.debug("ssid Spinner init");
    	if (eduroamCAT.wifiProfile!=null) { 
		if (eduroamCAT.wifiProfile.hasError()==false)
		{  	
			String ssidd="";
			ArrayList <String> ssids = db.getWiFi_all();
			if (ssids!=null)
				for (int i=0;i<ssids.size() ;i++)
				{
					ssidd = ssids.get(i);
					spinnerItems2.add(ssidd);
				}
		}
		else
		{
			no_authmethods_array[0]=getString(R.string.eapprofileMissing_title);
		}
		}
		else
		{
			no_authmethods_array[0]=getString(R.string.eapprofileMissing_title);
		}
    	
    	ssid_array=null;
		//convert spinnerItems to String[]
    	if (spinnerItems2.size()>0)
    	{
    		ssid_array=new String[spinnerItems2.size()];
		for (int i=0;i<spinnerItems2.size();i++)
		{
			ssid_array[i]=spinnerItems2.get(i);
		}
    	}
		
    	if (ssid_array==null)ssid_array= no_authmethods_array;
    	eduroamCAT.debug("got ssid array size:"+ssid_array.length);
        
    	if (no_authmethods_array[0].length()>0)  ssid_array= no_authmethods_array;
        
    	ArrayAdapter <String>adapter2 = new ArrayAdapter <String>(this.getActivity(), android.R.layout.simple_spinner_item, ssid_array);
    	ssidSpinner.setAdapter(adapter2);
    }

    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fnt
        
        super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
        //get wifimanager
        wifim = eduroamCAT.getWifiManager();
        //test key store access, and prompt if needed.
        //testKeystore();
        View v = inflater.inflate(R.layout.fragment_connect, container, false);
        progressText = (TextView) v.findViewById(R.id.progressText);
        authMethodSpinner = (Spinner) v.findViewById(R.id.ssids);
        ssidSpinner = (Spinner) v.findViewById(R.id.ssids2);
        state1 = (TextView) v.findViewById(R.id.stateText1);
        state1_image = (ImageView) v.findViewById(R.id.imageView1);
        state2 = (TextView) v.findViewById(R.id.stateText2);
        state2_image = (ImageView) v.findViewById(R.id.imageView2);
        state3 = (TextView) v.findViewById(R.id.stateText3);
        state3_image = (ImageView) v.findViewById(R.id.imageView3);
        state4 = (TextView) v.findViewById(R.id.stateText4);
        state4_image = (ImageView) v.findViewById(R.id.imageView4);
        state5 = (TextView) v.findViewById(R.id.stateText5);
        state5_image = (ImageView) v.findViewById(R.id.imageView5);
        state6 = (TextView) v.findViewById(R.id.stateText6);
        state6_image = (ImageView) v.findViewById(R.id.imageView6);
        feedback = (LinearLayout) v.findViewById(R.id.feedback);
		state5_layout = (LinearLayout) v.findViewById(R.id.state5_layout);
		state2_layout = (LinearLayout) v.findViewById(R.id.state2_layout);
		state3_layout = (LinearLayout) v.findViewById(R.id.state3_layout);
		state6_layout = (LinearLayout) v.findViewById(R.id.state6_layout);
        //ssids.setOnItemClickListener(this);
        warning = (TextView) v.findViewById(R.id.textView3);
        username = (EditText) v.findViewById(R.id.username);
        password = (EditText) v.findViewById(R.id.password);
        passText = (TextView) v.findViewById(R.id.textView2);
        userText = (TextView) v.findViewById(R.id.textView1);
        connect = (Button) v.findViewById(R.id.connect);
        connect.setOnClickListener(this);
        //rescan = (Button) v.findViewById(R.id.rescan);
        //rescan.setOnClickListener(this);
        //prog.setEnabled(false);
        //prog.incrementProgressBy(10);
        setAuthMethodSpinner();
        setSSIDSpinner();
        showCurrentState();
        username.requestFocus();
        ProfilesStorage db = new ProfilesStorage(getActivity());
        if (db.getReadableDatabase()!=null) if (getStoredUsername().length()>0) username.setText(getStoredUsername());
        db.close();
        if (eduroamCAT.isAdvancedMode()) setAuthSpinnerVisible(true);
        else setAuthSpinnerVisible(false);
        if (eduroamCAT.isAdvancedMode()) 
        { 
        	setSSIDSpinnerVisible(true); 
        	ssidSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					// TODO Auto-generated method stub
					ProfilesStorage db = new ProfilesStorage(getActivity());
					if (db.numberOfRowsWiFi()>0) {
						eduroamCAT.debug("spinner clicked:"+ssidSpinner.getSelectedItem().toString());
						WiFiProfile temp = db.getWiFi(ssidSpinner.getSelectedItem().toString());
						if (temp.hasError()==false) eduroamCAT.wifiProfile=temp;
					}
				}

				public void onNothingSelected(AdapterView<?> parent) {
					// TODO Auto-generated method stub
					
				}

        	});
        }
        else setSSIDSpinnerVisible(false);
        isNetworkOnline();
        //progressText.setText("Status:\n");
        //return inflater.inflate(R.layout.fragment_connect, container, false);
        return v;
    }
    
    //this shouldnt be here, should be in wificontroller
    public boolean isNetworkOnline() {
        boolean status=false;
        try{
            ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.getState()==NetworkInfo.State.CONNECTED) {
                status= true;
                String sSID=eduroamCAT.wifiCon.getCurrentSSID();
                setStatus(getString(R.string.wifi_toggle_on)+":"+eduroamCAT.state+":"+sSID);
            }else {
                netInfo = cm.getNetworkInfo(1);
                if(netInfo!=null && netInfo.getState()==NetworkInfo.State.CONNECTED)
                    status= true;
                	String sSID=eduroamCAT.wifiCon.getCurrentSSID();
                	setStatus(getString(R.string.wifi_toggle_on)+":"+eduroamCAT.state+":"+sSID);
            }
        }catch(Exception e){
            e.printStackTrace();  
            return false;
        }
        if (!status) setStatus(getString(R.string.wifi_toggle_off)+":"+eduroamCAT.state);
        return status;

        }  
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 37) {
            if (resultCode == Activity.RESULT_OK) {                

            }
            //eduroamCAT.alertUser("result="+resultCode, "KeyStore Access", getActivity());
        }

   }

	public void queryRemoveSSID(String message, String title, Activity activ)
	{
		new AlertDialog.Builder(activ)
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton(activ.getString(R.string.button_yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
					}
				})
				.setNegativeButton(activ.getString(R.string.button_no), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

					}
				})
				.show();
	}

    //test if keystore locked or not
    public void testKeystore(String message, String title, Activity activ)
    {
		new AlertDialog.Builder(activ)
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton(activ.getString(R.string.button_yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						try {
							startActivityForResult(new Intent("com.android.credentials.UNLOCK"),37);
    					} catch (ActivityNotFoundException e) {
    		    			eduroamCAT.debug("No UNLOCK activity: " + e.getMessage());
    					}
						//Intent intent = new Intent("android.app.action.SET_NEW_PASSWORD");
						//startActivity(intent);
					}
				})
				.setNegativeButton(activ.getString(R.string.button_no), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

					}
				})
				.show();
    }
    
    public static boolean duplicate()
    {
    	if (eduroamCAT.profiles!=null && eduroamCAT.wifiProfile!=null)
    		if (eduroamCAT.profiles.size()>0 && eduroamCAT.wifiProfile.hasError()==false)
    		{  			
    			String selectedAuthmethods="";
    			int pos=0,authMethodNumber=-1;
    			pos = authMethodSpinner.getSelectedItem().toString().indexOf('#');
    			selectedAuthmethods=authMethodSpinner.getSelectedItem().toString();
    			eduroamCAT.debug("using select auth method:"+selectedAuthmethods);
    			String authMethod=selectedAuthmethods.substring(pos+1);
    			eduroamCAT.debug("using auth method #:"+authMethod);
    			try
    			{
    				authMethodNumber = Integer.parseInt(authMethod);
    			}
    			catch (Exception e)
    			{
    				eduroamCAT.debug("Error converting auth method number #:"+authMethod);
    			}
    			AuthenticationMethod theAuthMethod;
    			theAuthMethod=eduroamCAT.profiles.get(0).getAuthenticationMethod(authMethodNumber-1);
    			boolean installed = eduroamCAT.wifiConfig18.saveEapConfig(username.getText().toString(), password.getText().toString(),theAuthMethod);
    			if (installed){
    				//eduroamCAT.alertUser("Profile Installed! Check Status Tab for Connection Information","Success",this.getActivity());
    				eduroamCAT.debug("Installed TRUE");
    				//setStatus(getString(R.string.profile_installed));
    				//TODO
    			}
    		}
    	return true;
    }
    
    public void clickConnect(View v)
    {
    	eduroamCAT.debug("Connect Pressed");
    	setStatus("Connecting...");
		setProfileInstalled(false);
    	//hide keyboard
    	InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE); 
    	inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
    	username.requestFocus();
    	warning.setTextColor(Color.parseColor("#ff0000"));
    	warning.setText(getString(R.string.profile_install));
		  int failures=0;
			if (eduroamCAT.profiles!=null && eduroamCAT.wifiProfile!=null)
			  if (eduroamCAT.profiles.size()>0 && eduroamCAT.wifiProfile.hasError()==false)
			  {  			
				  String selectedAuthmethods="";
				  int pos=0,authMethodNumber=-1;
				  pos = authMethodSpinner.getSelectedItem().toString().indexOf('#');
				  selectedAuthmethods=authMethodSpinner.getSelectedItem().toString();
				  eduroamCAT.debug("using select auth method:"+selectedAuthmethods);
				  String authMethod=selectedAuthmethods.substring(pos+1);
				  eduroamCAT.debug("using auth method #:"+authMethod);
				  try
				  {
					  authMethodNumber = Integer.parseInt(authMethod);
				  }
				  catch (Exception e)
				  {
					  eduroamCAT.debug("Error converting auth method number #:"+authMethod);
				  }
				  eduroamCAT.debug("start auth method="+authMethodNumber);
				  eduroamCAT.debug("and "+eduroamCAT.profiles.get(0).getNumberAuthenticationMethods());
				  if (authMethodNumber>0)
				  {
					  //open key store, if not open
					  //eduroamCAT.debug("Keystore Access="+testKeystore());
					  //************
					  while (authMethodNumber<=eduroamCAT.profiles.get(0).getNumberAuthenticationMethods())
					  {
						  eduroamCAT.debug("trying auth method "+authMethodNumber);
					  AuthenticationMethod theAuthMethod;
					  theAuthMethod=eduroamCAT.profiles.get(0).getAuthenticationMethod(authMethodNumber-1);
					  //remove any exisisting profiles for SSID
					  if (eduroamCAT.wifiProfile!=null && !eduroamCAT.wifiProfile.hasError())
						  eduroamCAT.debug("Delete=" + eduroamCAT.wifiCon.deleteProfile(eduroamCAT.wifiProfile.getSSID(),false));
					  eduroamCAT.debug("Got Auth Method:"+theAuthMethod.getOuterEAPType()+"/"+theAuthMethod.getInnerEAPType());
					  //add new profile
                          String usernameValue="";
                          if (username.getText()!=null) usernameValue = username.getText().toString();
                          if (usernameValue.length()==0 && theAuthMethod.getOuterEAPType()==13) usernameValue=theAuthMethod.getClientPrivateKeySubjectCN();
                          eduroamCAT.debug("Setting Identity to: "+usernameValue);
					  boolean installed = eduroamCAT.wifiConfig18.saveEapConfig(usernameValue, password.getText().toString(),theAuthMethod);
					  if (installed){
						  //eduroamCAT.alertUser("Profile Installed! Check Status Tab for Connection Information","Success",this.getActivity());
						  eduroamCAT.debug("Installed TRUE");
						  setStatus(getString(R.string.profile_installed));
						  warning.setText(getString(R.string.profile_installed));
						  warning.setTextColor(Color.parseColor("#00ff00"));
						  ProfilesStorage db = new ProfilesStorage(getActivity());
						  db.insertUSER(usernameValue);
						  db.close();
//						  if (theAuthMethod.getOuterEAPType()==13) {
////							  eduroamCAT.alertUser(getString(R.string.profile_tls_message), "TLS", getActivity());
////						  }
						  showCurrentState();
						  setProfileInstalled(true);
						  //SOCIAL MEDIA SUCCESS
//					  		String tweetUrl = "https://twitter.com/intent/tweet?text=I have successfully connected to #eduroam &url="
//					                + "https://www.eduroam.org";
//					  			Uri uri = Uri.parse(tweetUrl);
//					  			startActivity(new Intent(Intent.ACTION_VIEW, uri));
					  	  ////////////////////////////
						  break;
					  }
					  else {
						  if (authMethodNumber<eduroamCAT.profiles.get(0).getNumberAuthenticationMethods())
						  {
							  if (eduroamCAT.profiles.get(0).getNumberAuthenticationMethods()>1) eduroamCAT.alertUser("AuthMethod #"+authMethodNumber+" failed. trying next...","Failed",this.getActivity());
							  else eduroamCAT.alertUser("AuthMethod #"+authMethodNumber+" failed.",getString(R.string.profile_failed),this.getActivity());
							  authMethodNumber++;
							  failures++;
							  continue;
						  }
						  else
						  {
							eduroamCAT.debug("Installed FALSE");
							  setProfileInstalled(false);
							  int currentapiVersion = android.os.Build.VERSION.SDK_INT;
							  if (currentapiVersion > 22 && eduroamCAT.wifiCon.checkEduroamSSID().length()>0) {
								  queryRemoveSSID(getString(R.string.profile_failed_marshmallow), getString(R.string.profile_failed), this.getActivity());
							  }
							  else {
								  testKeystore(getString(R.string.profile_failed_message),getString(R.string.profile_failed),this.getActivity());
							  }
						  	setStatus("Profile failed.");
						  	warning.setText(getString(R.string.profile_install_failed));
						  	warning.setTextColor(Color.parseColor("#ff0000"));
						  	failures++;
						  	break;
						  }
					  }
					  }
				  }
				  else
				  {
					  eduroamCAT.debug("Error with detecting auth method");
					  eduroamCAT.alertUser("Profile missing. Please install a profile first.",getString(R.string.profile_failed),this.getActivity());
					  setStatus(getString(R.string.profile_install_failed));
					  warning.setText(getString(R.string.profile_install_failed));
					  warning.setTextColor(Color.parseColor("#ff0000"));
				  }  				  
			  }
			  else
			  {
				eduroamCAT.debug("No profiles found");
				//Toast.makeText(v.getContext(), "No profiles found to use. Please install a profile first.",3).show();
				eduroamCAT.alertUser(getString(R.string.profile_install_missing), getString(R.string.profile_failed), this.getActivity());
				setStatus(getString(R.string.profile_install_missing_message));
				warning.setText(getString(R.string.profile_install_missing_message));
				warning.setTextColor(Color.parseColor("#ff0000"));
				//wifim.reconnect();
			  }
			
			if (failures==eduroamCAT.profiles.get(0).getNumberAuthenticationMethods())
			{
				eduroamCAT.alertUser(getString(R.string.profile_failed_all),getString(R.string.profile_failed),this.getActivity());
			}
			
			eduroamCAT.wifiCon.checkSSID("eduroam");
			showCurrentState();
    }
    
    //button clicks
  	public void onClick(View view) 
  	{
  		switch (view.getId())
  		  {
  		  case R.id.connect:
  			  clickConnect(view);
  		   break;
//  		  case R.id.rescan:
//  			setAuthMethodSpinner();
//  			setStatus("Checking profile...");
//  			eduroamCAT.debug("Keystore Access="+testKeystore());
//  			eduroamCAT.debug("reScan Pressed");
//  			eduroamCAT.wifiCon.checkSSID("eduroam");
//  		  break;
  			  
  		  default:
  		   break;
  		  }
  	}     
  	
      @Override()
      public void onResume()
       {         
    	  showCurrentState();
    	  super.onResume();
       }  
}