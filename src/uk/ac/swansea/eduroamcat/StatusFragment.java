//*******************
//Author: Gareth Ayres <g.j.ayres@swansea.ac.uk>
//SENSE Project
//https://github.com/GEANT/CAT-Android
//*******************
package uk.ac.swansea.eduroamcat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

public class StatusFragment extends Fragment implements OnClickListener {
	
	ToggleButton toggleWifi;
	ToggleButton verboseDebug;
	Button quickConnect,duplicate;
	public static boolean verbose = false;
	static TextView textStatus, statusInfo;
	static int debugSize = 0;
	static boolean wifiOn = true;
    private static String debugText = "";

    Activity activity = getActivity();
    
    public static void setDebug(String msg)
    {
    	eduroamCAT.debug("USER TEXT:"+msg);
    	msg=msg.concat("\n");
    	debugText=debugText.concat(msg);
    	//debugText=debugText+msg+"\n";
    	updateText();
    }
    
    public static Boolean isWiFiToggled()
    {
    	return wifiOn;
    }

    public static String getDebug()
    {
    	return debugText;
    }
    
    public String latestSummary()
    {
        //Update the status inforamtion
    	String summaryText="";
    	if (getActivity()!=null)
    	try {
        String currentSSID=eduroamCAT.wifiCon.getCurrentSSID();
        String bSSID=eduroamCAT.wifiCon.getCurrentBSSID();
        String rss=eduroamCAT.wifiCon.getCurrentRSS();
        String reason=eduroamCAT.wifiCon.getFailReason();
        String failReason="";
        if (reason!=null) failReason=getActivity().getString(R.string.status_failure)+reason+"<br/>";
        String linkSpeed=eduroamCAT.wifiCon.getLinkSpeed()+"Mbs";
        String ip=eduroamCAT.wifiCon.getIPAddress();
        String mac=eduroamCAT.wifiCon.getDeviceWiFiMac();
        String supState=eduroamCAT.wifiCon.getSupplicantState();
        if (supState==null || supState.length()<1) supState="Unknown";
        String supDetailed = eduroamCAT.wifiCon.getDetailedSupplicantState();
        if (supDetailed==null || supDetailed.length()<1) supDetailed="Unknown";
        summaryText="<h2>"+getActivity().getString(R.string.status_title)+"<h2/>";
        summaryText+="SSID:<b><font color=\"blue\">"+currentSSID+"</font></b> BSSID :<b>"+bSSID+"</b><br/>Signal Strength:<b>"+rss+"dBm</b> Link Speed:<b>"+linkSpeed+"</b><br/>";
        summaryText+=getActivity().getString(R.string.status_connection)+"<b>"+eduroamCAT.wifiCon.getWifiState()+"</b> ("+eduroamCAT.wifiCon.getWifiStateDetailed()+")<br/>";
        summaryText+=getActivity().getString(R.string.status_supplicant)+"<b>"+supState+"</b>("+supDetailed+")<br/>";
        summaryText+=getActivity().getString(R.string.status_wifi)+"<b>"+eduroamCAT.wifiCon.checkWifiEnabled()+"</b>"+getActivity().getString(R.string.status_supplicant_ok)+"<b>"+ eduroamCAT.wifiCon.isSupplicantOK()+"</b><br/>";
        summaryText+=failReason;
			if (eduroamCAT.currentapiVersion<23) summaryText+=getActivity().getString(R.string.status_ip)+"<b><font color=\"blue\">"+ip+"</font></b>"+getActivity().getString(R.string.status_mac)+"<b>"+mac+"</b> <br/>";
			else summaryText+=getActivity().getString(R.string.status_ip)+"<b><font color=\"blue\">"+ip+"</font></b><br/>";
        summaryText+="<br/>";
        Spanned statusSummary = Html.fromHtml(summaryText);
        if (statusInfo!=null) statusInfo.setText(statusSummary);
    	}
    	catch (Exception e)
    	{
    		eduroamCAT.debug("latestSummary Exception:"+e);
    	}
        return summaryText;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
    		
        super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_status, container, false);
        textStatus = (TextView) v.findViewById(R.id.textStatus);
        textStatus.setMovementMethod(new ScrollingMovementMethod());
        statusInfo = (TextView) v.findViewById(R.id.statusInfo);
        verboseDebug = (ToggleButton) v.findViewById(R.id.toggleVerbose);
        verboseDebug.setOnClickListener(new OnClickListener() {                       

            public void onClick(View v) {
            	if (verbose)
   			 {
   				 eduroamCAT.debug("debug Toggle OFF");
   			 }
   			 else
   			 {
   				 eduroamCAT.debug("debug Toggle ON");
   			 }
   			 verbose =!verbose;
            }
            }
        );
        
        //quick connect button
        quickConnect = (Button) v.findViewById(R.id.quickConnect);
        quickConnect.setOnClickListener(new OnClickListener() {                       

            public void onClick(View v) {
            	eduroamCAT.debug("Quick Connect Pressed");
    			if (eduroamCAT.profiles!=null && eduroamCAT.wifiProfile!=null) {
    				eduroamCAT.reconnect();
            }
    			  else
    			  {
    				eduroamCAT.debug("No profiles found");
    				//Toast.makeText(v.getContext(), "No profiles found to use. Please install a profile first.",3).show();
    				eduroamCAT.alertUser(getString(R.string.profile_install_missing),getString(R.string.profile_failed),getActivity());
    			  }
    			//eduroamCAT.wifiCon.checkSSID("eduroam");
            }
            }
        );
        
        toggleWifi = (ToggleButton) v.findViewById(R.id.toggleWifi);
        toggleWifi.setOnClickListener(this);
        if (!eduroamCAT.wifiCon.checkWifiEnabled()) 
        	{
        		//if (toggleWifi.isChecked()) toggleWifi.toggle();
        		toggleWifi.setChecked(false);
        		wifiOn = false;
        		eduroamCAT.debug("WiFi OFF on detailed Activity Create");
        	}  
        else
        {
        	toggleWifi.setChecked(true);
        	eduroamCAT.debug("WiFi ON on detailed Activity Create");
        	wifiOn = true;
        }
        
        //duplicate button
        duplicate = (Button) v.findViewById(R.id.duplicateButton);
        if (eduroamCAT.isAdvancedMode())
        {
        	duplicate.setVisibility(View.VISIBLE);
        	duplicate.setOnClickListener(new OnClickListener() {                       

                public void onClick(View v) {
                	eduroamCAT.debug("Duplicate Pressed");
        			if (eduroamCAT.profiles!=null && eduroamCAT.wifiProfile!=null) {
        				AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        				alert.setTitle("Duplicate");
        				alert.setMessage("What SSID to duplicate eduroam to?");

        				// Set an EditText view to get user input 
        				final EditText input = new EditText(getActivity());
        				alert.setView(input);

        				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        				public void onClick(DialogInterface dialog, int whichButton) {
        				  String value = input.getText().toString();
        				  ProfilesStorage db = new ProfilesStorage(getActivity());
        					//Build WiFi Profile
        					//default to eduroam settings
        				   WiFiProfile wifiProfile_dup = null;
        				   wifiProfile_dup = new WiFiProfile();
        					if (eduroamCAT.wifiProfile!=null) {
        						wifiProfile_dup.isOK();
        						wifiProfile_dup.setSSID(value);
        						wifiProfile_dup.setAuthType("WPA2");
        						wifiProfile_dup.setEncryptionType("CCMP");
        						wifiProfile_dup.setAutojoin(true);
        						wifiProfile_dup.setSSIDPriority(db.numberOfRowsWiFi()+1);
        						if (wifiProfile_dup.hasError()==false) {
        							eduroamCAT.debug("Duplicate WiFi Profile OK:"+value);
        							//add to DB
        							long result = db.insertWiFi("0", wifiProfile_dup.getSSID(), wifiProfile_dup.getAuthType(),wifiProfile_dup.getEncryptionType(),wifiProfile_dup.getSSIDPriority(), 1);
        							eduroamCAT.debug("DB INSET INTO WIFI:"+result);
        						}
        						else eduroamCAT.debug("WiFi Profile Error!");
        					}
        				  }
        				});

        				alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        				  public void onClick(DialogInterface dialog, int whichButton) {
        				    // Canceled.
        				  }
        				});

        				alert.show();
        				//ConnectFragment.duplicate();
                }
        			  else
        			  {
        				eduroamCAT.debug("No profiles found");
        				eduroamCAT.alertUser(getString(R.string.profile_install_missing),getString(R.string.profile_failed),getActivity());
        			  }
        			//eduroamCAT.wifiCon.checkSSID("eduroam");
                }
                }
            );
        	
        }
        else duplicate.setVisibility(View.GONE);
        
        latestSummary();
        return v;
    }
    
    //button clicks
	public void onClick(View view) 
	{
		switch (view.getId())
		  {
		  case R.id.toggleVerbose:
			 if (verbose)
			 {
				 eduroamCAT.debug("debug Toggle OFF");
			 }
			 else
			 {
				 eduroamCAT.debug("debug Toggle ON");
			 }
			 verbose =!verbose;
		   break;
		  case R.id.toggleWifi:
			  if (wifiOn)
			  {
				  //turn wifi off
				  eduroamCAT.debug("wifi Toggle OFF");
				  eduroamCAT.wifiCon.setWifiOFF();
			  }
			  else
			  {
				  //turn wifi on
				  eduroamCAT.debug("wifi Toggle ON");
				  eduroamCAT.wifiCon.setWifiON();
			  }
			  wifiOn=!wifiOn;
		   break;
		  default:
		   break;
		  }
		updateText();
	}

	public static void updateText() 
	{
		if (textStatus!=null){
			//Log.d("eduroamcat", "debugsize="+debugSize+" and getDebug="+getDebug().length());
//			if (debugSize<getDebug().length()) 
//			{
				textStatus.setText("");
				textStatus.append(getDebug()+"\n");
				//textStatus.scrollTo(0,textStatus.getBottom()-50);
//				debugSize = textStatus.getText().length();
//			}
		}
	}

}