//*******************
//Author: Gareth Ayres <g.j.ayres@swansea.ac.uk>
//SENSE Project
//https://github.com/GEANT/CAT-Android
//*******************
package uk.ac.swansea.eduroamcat;

import java.security.Key;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ConfigureFragment extends Fragment implements OnClickListener {
	
	static TextView textConfig;
	static Button removeButton,scadButton;
	static TextView idptext;
	static ProgressBar scadProgress;
	static TextView summaryView;
	static SCAD scad;
	static int viewdProfiles=0;
            
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
    	View v = inflater.inflate(R.layout.fragment_configure, container, false);
		summaryView = (TextView) v.findViewById(R.id.configSummaryText);
		idptext = (TextView) v.findViewById(R.id.idp_list);
		scadProgress = (ProgressBar) v.findViewById(R.id.scadProgress);
    	removeButton = (Button) v.findViewById(R.id.removeButton);
		removeButton.setOnClickListener(this);
		scadButton = (Button) v.findViewById(R.id.scadButton);
		scadButton.setOnClickListener(this);
        idptext.setVisibility(View.INVISIBLE);
        scadProgress.setVisibility(View.INVISIBLE);
        //Build WiFi Settings Text
        String wifiText = "";
        //get WiFi settings from WiFiProfile
        if (eduroamCAT.wifiProfile.hasError())
        {
        	wifiText="<h2><font color=\"red\">"+getString(R.string.wifiProfileMissing_text1)+"</font></h2><p>"+getString(R.string.wifiProfileMissing_text2)+"</p>";
        }
        else
        {
        	/*wifiText="<h2>WiFi Profile</h2>"+
        			 "<b>SSID</b>="+eduroamCAT.wifiProfile.getSSID()+"<br/>"+
        			 "<b>Authentication</b>="+eduroamCAT.wifiProfile.getAuthType()+"<br/>"+
        			 "<b>Encryption</b>="+eduroamCAT.wifiProfile.getEncryptionType()+"<br/>";*/
        }

		String summary_template="";
		
        if (eduroamCAT.profiles!=null && eduroamCAT.profiles.size()>0)
		{
			ConfigProfile aProfile = eduroamCAT.profiles.get(0);
			if (aProfile.isError()==false) {
				
				String supportHTML="";
				if (eduroamCAT.profiles.get(0).hasHelpdeskInfo())
				{
					String web = "";
					if (aProfile.getHelpdeskURL()!=null) web = aProfile.getHelpdeskURL().toString();
					supportHTML="<h2>"+getString(R.string.supportHTML_text1)+"</h2>"+
								"<b>"+getString(R.string.supportHTML_text_email)+"</b><font color=\"blue\">" + aProfile.getSupportEmails() + "</font><br/>" +
								"<b>"+getString(R.string.supportHTML_text_phone)+"</b><font color=\"blue\">" + aProfile.getHelpdeskPhoneNumber("") + "</font><br/>" +
								"<b>"+getString(R.string.supportHTML_text_tou)+"</b><font color=\"blue\">" + aProfile.getTermsOfUse() + "</font><br/>" +
								"<b>"+getString(R.string.supportHTML_text_web)+"</b><font color=\"blue\">" + web+ "</font><br/>";
				}
				
				String authMethods="";
				if (aProfile.getNumberAuthenticationMethods()>0)
				{
					for (int i=0;i<aProfile.getNumberAuthenticationMethods();i++)
					{
						if (aProfile.getAuthenticationMethod(i).isError()) {
							eduroamCAT.debug("AUTH METHOD "+i+" has error");
							eduroamCAT.debug("ERROR="+aProfile.getAuthenticationMethod(i).getError());
							continue;
						}
						AuthenticationMethod aAuthMethod = aProfile.getAuthenticationMethod(i);
						int count=i+1;
						authMethods = authMethods.concat("<h3>"+getString(R.string.authMethod_text_title)+ count +"</h3>");
						String outer="";
						String inner="";
						if (aAuthMethod.getOuterEAPType()==25) outer="/PEAP";
						if (aAuthMethod.getOuterEAPType()==21) outer="/TTLS";
						if (aAuthMethod.getOuterEAPType()==13) outer="/TLS";
						authMethods = authMethods.concat("<b>"+getString(R.string.authMethod_text_eapmethod)+"</b> <font color=\"blue\">"+aAuthMethod.getOuterEAPType()+outer+"</font><br/>");
						if (aAuthMethod.getInnerEAPType()>0)
							if (aAuthMethod.getInnerEAPType()==1) inner="/PAP";
							if (aAuthMethod.getInnerEAPType()==26) inner="/MSCHAPv2";
							if (aAuthMethod.getInnerEAPType()==6) inner="/GTC";
							authMethods = authMethods.concat("<b>"+getString(R.string.authMethod_text_innereapmethod)+"</b><font color=\"blue\"> "+aAuthMethod.getInnerEAPType()+inner+"</font><br/>");
						if (aAuthMethod.getAnonID().length()>0)
							authMethods = authMethods.concat("<b>"+getString(R.string.authMethod_text_outter)+"</b><font color=\"blue\"> "+aAuthMethod.getAnonID()+"</font><br/>");
						if (aAuthMethod.getServerIDs().size()>0)
						{
							ArrayList<String> serverID = aAuthMethod.getServerIDs();
							for (int s=0;s<aAuthMethod.getServerIDs().size();s++)
							{
								String aServerID = serverID.get(s);
								if (aServerID.length()>0)
									authMethods = authMethods.concat("<b>"+getString(R.string.authMethod_text_server)+"</b><font color=\"green\"> "+aServerID+"</font><br/>");
							}
						}
						Certificate tempcert = aAuthMethod.getCAcert();
						if (tempcert!=null)
						{
							String certString = tempcert.toString();
							int start=certString.indexOf("CN=");
							int finish=certString.indexOf("Validity");
							if (start>0 && finish>0) {
								certString = certString.substring(start, finish);
								authMethods = authMethods.concat("<b>"+getString(R.string.authMethod_text_certcn)+"</b><font color=\"purple\"> "+certString+"</font><br/>");
							}
						}

						if (aAuthMethod.getOuterEAPType()==13) {
                            X509Certificate acert;
                            acert=aAuthMethod.getClientCert();
							if (acert != null) {
							    String expiry = acert.getNotAfter().toString();
							    String issuer = acert.getSubjectDN().getName();
							    int start,finish=0;
                                start=issuer.indexOf("CN=");
                                if (start<2) start=issuer.indexOf("E=");
                                if (start>0 && issuer.length()>3) {
									finish = issuer.indexOf(",", start);
									if (finish<1) finish=issuer.length();
									issuer = issuer.substring(start + 3, finish);
								}
								if (expiry.length()>0 && issuer.length()>0) {
									authMethods = authMethods.concat("<b>"+getString(R.string.authMethod_text_clientcertcn)+"</b><font color=\"purple\"> "+issuer+"</font><br/>");
                                    authMethods = authMethods.concat("<b>"+getString(R.string.authMethod_text_clientcertexp)+"</b><font color=\"blue\"> "+expiry+"</font><br/>");
								}
                                //aAuthMethod.getClientPrivateKeySubjectCN();
							}
						}
					}
					LayoutParams params = summaryView.getLayoutParams();
					params.height = 1000;
					summaryView.setLayoutParams(params);
					removeButton.setVisibility(View.VISIBLE);
					removeButton.setEnabled(true);
					scadButton.setVisibility(View.GONE);
				}
				else
				{
					authMethods = authMethods.concat("<h3>"+getString(R.string.authMethod_text_error1)+"</h3>");
					authMethods = authMethods.concat(getString(R.string.authMethod_text_error2));
				}
			
				summary_template = "<h1>"+getString(R.string.summary_text_title)+"</h1><br/>" +
				"<p><b>"+getString(R.string.summary_text_provider)+"</b><font color=\"red\">" + aProfile.getDisplayName() + "</font><br/>" +
				"<b>"+getString(R.string.summary_text_description)+"</b><font color=\"blue\">" + aProfile.getDescription() + "</font><br/>" +		
				//"<b>IdentityProvider:</b><font color=\"blue\">" + aProfile.getEAPIdP_ID() + "</font><br/>" +
				wifiText +
				authMethods +
				supportHTML;
			}
			else {
				summary_template = "<h1>" + getString(R.string.profileMissing_title) + "</h1><br/> " +
						"<p><b>" + getString(R.string.profileMissing_text1) + "</b>" + aProfile.getError() + "<br/>" + "</p>";
			}
		}
		else
		{
				summary_template = "<font color=\"red\"><h2>"+getString(R.string.eapprofileMissing_title)+"</h2></font><br/>";
				if (eduroamCAT.wifiCon.isWifiEnabled() && eduroamCAT.wifiCon.getCurrentSSID().length()>0)
				{
					summary_template+=getString(R.string.eapprofileMissing_text1,eduroamCAT.wifiCon.getCurrentSSID());
					summary_template+="<br/>"+getString(R.string.eapprofileMissing_text2)+"<br> ";
					String catURL="<a href=\"https://cat.eduroam.org\">https://cat.eduroam.org</a>";
					summary_template+=getString(R.string.eapprofileMissing_text3,catURL);
					summary_template+="<br/>"+getString(R.string.eapprofileMissing_text4);
					if (eduroamCAT.wifiCon.getCurrentSSID().contains("eduroam")) {
						summary_template+="<h1>"+getString(R.string.manualChecks_title)+"</h1>";
						summary_template+=eduroamCAT.wifiCon.checkEduroam();
					}
				}
				else
				{
					summary_template+=getString(R.string.summary_text1);
					summary_template+="<br/>"+getString(R.string.summary_text2);
				}

			if (viewdProfiles<1) {
				Intent profiles = new Intent(getActivity(), ViewProfiles.class);
				getActivity().startActivity(profiles);
				viewdProfiles++;
			}

		}
	    Spanned idp_summary = Html.fromHtml(summary_template);
	    summaryView.setText(idp_summary);
	    summaryView.setMovementMethod(new ScrollingMovementMethod());
	    summaryView.setMovementMethod(LinkMovementMethod.getInstance());

	    return v;
    }
    
    public static void setupSCAD()
    {
		if (removeButton!=null) removeButton.setVisibility(View.GONE);
		if (idptext!=null) idptext.setVisibility(View.VISIBLE);
		if (scadProgress!=null) scadProgress.setVisibility(View.VISIBLE);
    }
    
    public static void removeSCAD() {
		removeButton.setVisibility(View.VISIBLE);
        idptext.setVisibility(View.GONE);
        scadProgress.setVisibility(View.GONE);
    }

	//Remove button press
	public void removeClick()
	{
		eduroamCAT.debug("remove button click");
		new AlertDialog.Builder(getActivity())
	    .setTitle(getString(R.string.removeProfile_title))
	    .setMessage(getString(R.string.removeProfile_message))
	    .setPositiveButton(getString(R.string.button_yes), new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	        	ProfilesStorage db = new ProfilesStorage(getActivity());
	        	eduroamCAT.debug("YES Remove Profile");
	    		eduroamCAT.wifiProfile.setConfigError("Discarded by user");
	    		if (eduroamCAT.profiles!=null)
	    		{
	    			for(int s=0; s<eduroamCAT.profiles.size(); s++)
	    			eduroamCAT.profiles.get(s).setConfigError("Discarded by user");
	    		}
	    		eduroamCAT.debug("Removing from profiles and DB");
	    		eduroamCAT.profiles.clear();
	    		db.deleteWiFi();
	    		db.deleteAllProfiles();
	    		db.close();
	    		eduroamCAT.wifiCon.deleteProfile("eduroam",true);
	    		if (eduroamCAT.profiles.isEmpty()) 
	    		{
	    			eduroamCAT.debug("*****************REMOVED PROFILES********************");
	    			TextView summaryView = (TextView)  getView().findViewById(R.id.configSummaryText);  
	    			String summary_template = "<font color=\"red\"><h2>"+getString(R.string.eapprofileMissing_title)+"</h2></font><br/>";
	    			Spanned idp_summary = Html.fromHtml(summary_template);
	    			summaryView.setText(idp_summary);
	    		}
	        }
	     })
	    .setNegativeButton(getString(R.string.button_no), new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	        	eduroamCAT.debug("NO remove Profile");
	        }
	     })
	     .show();
	}

	//Remove button press
	public void scadClick() {
		eduroamCAT.debug("SCAD button click");
//		scad = new SCAD(getActivity());
//		scad.execute();
		Intent profiles = new Intent(getActivity(), ViewProfiles.class);
		getActivity().startActivity(profiles);
	}

	public void onClick(View view) {
		switch (view.getId())
		  {
			  case R.id.removeButton:
				  removeClick();
				  break;
			  case R.id.scadButton:
				  scadClick();
				  break;
		  default:
		   break;
		  }
	}

	@Override()
	public void onResume()
	{
		scadButton.setOnClickListener(this);
		removeButton.setOnClickListener(this);
		super.onResume();
	}
}