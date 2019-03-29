//*******************
//Author: Gareth Ayres <g.j.ayres@swansea.ac.uk>
//SENSE Project
//https://github.com/GEANT/CAT-Android
//*******************
package uk.ac.swansea.eduroamcat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.widget.Toast;

public class EAPMetadata extends Activity {

	static Button discard,install;
	ProfilesStorage db = new ProfilesStorage(this);
	String keyPass=""; //default to nothing to start (optional)
	//global clietn cert value for retry
	static NodeList clientCert;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;
	
	public boolean testExternalStorage()
	{
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    else return false;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					this.recreate();
				} else {
					Toast.makeText(this, this.getString(R.string.storagePermission), Toast.LENGTH_LONG).show();
				}
				return;
			}
		}
	}
	
		@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Show the Up button in the action bar.
		setupActionBar();
		if (getIntent().getBooleanExtra("EXIT", false)) 
		{
		        finish();
		}
		eduroamCAT.debug("Starting onCreate for EAPMetadata class...");
		setContentView(R.layout.activity_eapmetadata);
        Intent configIntent = getIntent();
        InputStream configIn = null;
        boolean configFileError = false;
        String pathToDownload ="";
        eduroamCAT.debug("Got eap-config:"+configIntent.getDataString());

        //check real-time permissions for storage
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            eduroamCAT.debug("No External Storage permissions");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            configFileError=true;
			Toast.makeText(this, this.getString(R.string.storagePermission), Toast.LENGTH_LONG).show();
        }
        else configFileError=false;

        if (configIntent.getDataString().contains("http://") || configIntent.getDataString().contains("https://"))
        {
        	//download file to sdcard
        	if (testExternalStorage()) {
        	try {
				//delte file if already exists
				String path = Environment.getExternalStorageDirectory().getPath() + "/EAPConfig/";
				File file = new File(path);
				File outputFile = new File(file, "eduroam.eap-config");
            if (outputFile.exists())
            {
                Boolean deleteResult = outputFile.delete();
                eduroamCAT.debug("delete file before download:" + deleteResult);
            }
				//if (downloadEAPFile(configIntent.getDataString(),"eduroam.eap-config")) configFileError=false;
        		//else configFileError=true;
				new DownloadEAPConfig().execute(configIntent.getDataString());
        		pathToDownload=Environment.getExternalStorageDirectory().getPath()+"/EAPConfig/eduroam.eap-config";
				eduroamCAT.debug("Downloaded to:"+pathToDownload);
			} catch (Exception e) {
				eduroamCAT.debug("Error downloading file:"+configIntent.getDataString());
				configFileError=true;
				e.printStackTrace();
			}
        	}
        	else 
        		{
        			eduroamCAT.debug("Error reading external storage:"+configIntent.getDataString());
        			configFileError=true;
        		}
        }
        String aBuffer = "";
        if (configFileError==false) {
        int loop=0;
        while (loop<10) {
		try {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			if (pathToDownload.length()>0) 
				{
					eduroamCAT.debug("downloaded?:"+eduroamCAT.downloaded);
					if (!eduroamCAT.downloaded) { loop++; continue; }
					configIn = new FileInputStream(pathToDownload);
					eduroamCAT.debug("using path:"+pathToDownload);
				}
			else {
				configIn = getContentResolver().openInputStream(configIntent.getData());
				eduroamCAT.debug("Using intent get data");
			}
			//http://stackoverflow.com/questions/14364091/retrieve-file-path-from-caught-downloadmanager-intent <---- change for download files in 4.2
			
			BufferedReader myReader = new BufferedReader(new InputStreamReader(configIn));
			String aDataRow = "";

			try {
				while ((aDataRow = myReader.readLine()) != null) {
					aBuffer += aDataRow + "\n";
				}
			} catch (IOException e) {
				eduroamCAT.debug("Config File read error.");
				//finish();
				configFileError=true;
				e.printStackTrace();
			}
			
			if (configFileError==false) {
			//Build Array of Vales from profile
			try {
				eduroamCAT.profiles=parseProfile(aBuffer);
			} catch (SAXException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				//finish();
			} catch (ParserConfigurationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				//finish();
			}
			
			//Build WiFi Profile
			//default to eduroam settings
			if (eduroamCAT.wifiProfile==null) eduroamCAT.wifiProfile = new WiFiProfile();
			if (eduroamCAT.wifiProfile!=null && db.numberOfRowsWiFi()<1) {
				eduroamCAT.wifiProfile.isOK();
				eduroamCAT.wifiProfile.setSSID("eduroam");
				eduroamCAT.wifiProfile.setAuthType("WPA2");
				eduroamCAT.wifiProfile.setEncryptionType("CCMP");
				eduroamCAT.wifiProfile.setAutojoin(true);
				eduroamCAT.wifiProfile.setSSIDPriority(1);
				if (eduroamCAT.wifiProfile.hasError()==false) {
					eduroamCAT.debug("WiFi Profile OK");
					//add to DB
					long result = db.insertWiFi("0", eduroamCAT.wifiProfile.getSSID(), eduroamCAT.wifiProfile.getAuthType(),eduroamCAT.wifiProfile.getEncryptionType(),eduroamCAT.wifiProfile.getSSIDPriority(), 1);
					eduroamCAT.debug("DB INSET INTO WIFI:"+result);
				}
				else eduroamCAT.debug("WiFi Profile Error!");
			}
			else 
				{
					eduroamCAT.debug("WiFi Profile Found in DB!");
		  			eduroamCAT.wifiProfile=db.getWiFi(0);
		  			eduroamCAT.debug("SSID FOUND="+eduroamCAT.wifiProfile.getSSID());
					//load????
					//eduroamCAT.debug("Exiting...!");
					//finish();
				}
			break;
			}
			else
			{
				eduroamCAT.debug("Config File error");
			}

		} catch (IOException e1) {
			eduroamCAT.debug("Config File access error");
			e1.printStackTrace();
			
		}
		loop++;
        }
        }
		
		TextView summaryView = (TextView) findViewById(R.id.configSummary);
		String summary_template="";
		//check if more than zero profiles in profiles		
		if (eduroamCAT.profiles!=null && eduroamCAT.wifiProfile!=null)
		{
			try {
				ConfigProfile aProfile = eduroamCAT.profiles.get(eduroamCAT.profiles.size()-1);
			
			if (aProfile.isError()==false) {
				
				String supportHTML="";
				if (eduroamCAT.profiles.get(eduroamCAT.profiles.size()-1).hasHelpdeskInfo())
				{
					String web = "";
					if (aProfile.getHelpdeskURL()!=null) web = aProfile.getHelpdeskURL().toString();
					supportHTML="<h2>"+getString(R.string.supportHTML_text1)+"</h2>"+
								"<b>"+getString(R.string.supportHTML_text_email)+"</b><font color=\"blue\">" + aProfile.getSupportEmails() + "</font><br/>" +
								"<b>"+getString(R.string.supportHTML_text_phone)+"</b><font color=\"blue\">" + aProfile.getHelpdeskPhoneNumber("") + "</font><br/>" +
								"<b>"+getString(R.string.supportHTML_text_tou)+"</b><font color=\"blue\">" + aProfile.getTermsOfUse() + "</font><br/>" +
								"<b>"+getString(R.string.supportHTML_text_web)+"</b><font color=\"blue\">" + web + "</font><br/>";
				}
				
				String wifiSettings="";
				//wifiSettings="<h2>WiFi Settings</h2> SSID=<font color=\"red\"><b>"+eduroamCAT.wifiProfile.getSSID()+"</b></font> Auth:<b>"+eduroamCAT.wifiProfile.getAuthType()+"</b> Encryption=<b>"+eduroamCAT.wifiProfile.getEncryptionType() + "</b><br/>";
				
				String authMethods="";
				if (aProfile.getNumberAuthenticationMethods()>0)
				{
					eduroamCAT.debug("************************************************:got auth method");
					for (int i=0;i<aProfile.getNumberAuthenticationMethods();i++)
					{
						if (aProfile.getAuthenticationMethod(i).isError()) continue;
						AuthenticationMethod aAuthMethod = aProfile.getAuthenticationMethod(i); 
						int count=i+1;
						authMethods = authMethods.concat("<h3>"+getString(R.string.authMethod_text_title) + count +"</h3>");
						String outer="";
						String inner="";
						if (aAuthMethod.getOuterEAPType()==25) outer="/PEAP";
						if (aAuthMethod.getOuterEAPType()==21) outer="/TTLS";
						if (aAuthMethod.getOuterEAPType()==13) outer="/TLS";
						if (aAuthMethod.getOuterEAPType()==52) outer="/PWD";
						authMethods = authMethods.concat("<b>"+getString(R.string.authMethod_text_eapmethod)+"</b> <font color=\"blue\">"+aAuthMethod.getOuterEAPType()+outer+"</font><br/>");
						if (aAuthMethod.getInnerEAPType()>0)
							if (aAuthMethod.getInnerEAPType()==1) inner="/PAP";
							if (aAuthMethod.getInnerEAPType()==26) inner="/MSCHAPv2";
							if (aAuthMethod.getInnerEAPType()==6) inner="/GTC";
							authMethods = authMethods.concat("<b>"+getString(R.string.authMethod_text_innereapmethod)+"</b><font color=\"blue\"> "+aAuthMethod.getInnerEAPType()+inner+"</font><br/>");
						if (aAuthMethod.getAnonID().length()>0)
							authMethods = authMethods.concat("<b>"+getString(R.string.authMethod_text_server)+"</b><font color=\"blue\"> "+aAuthMethod.getAnonID()+"</font><br/>");
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
					}
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
				wifiSettings +
				authMethods +
				supportHTML;
				
			}
			else
			{
				summary_template = "<h1>"+getString(R.string.profileMissing_title)+"</h1><br/>" +
						"<p><b>"+getString(R.string.profileMissing_text1)+"</b>" + aProfile.getError() + "<br/>" +"</p>";
			}

		} catch (Exception e)
		{
			summary_template = "<h1>"+getString(R.string.config_file_error)+"</h1>"+getString(R.string.config_file_text);
		}
		}
		else
		{
			//no profiles 
			eduroamCAT.debug("profiles null");
			summary_template = "<h1>"+getString(R.string.config_file_error)+"</h1><br/>" +
					"<p><b>"+getString(R.string.config_file_text2)+"<br/></p>";
		}
		
		Spanned sp_summary = Html.fromHtml(summary_template);
		summaryView.setText(sp_summary);
		summaryView.setMovementMethod(new ScrollingMovementMethod());
		//summaryView.setScrollBarStyle(BIND_ABOVE_CLIENT);
	}
	
	//Discard button press
	public void onDiscardClick(View view)
	{
		
		if (eduroamCAT.profiles!=null)
		{
			eduroamCAT.wifiProfile.setConfigError(getString(R.string.config_discarded));
			for(int s=0; s<eduroamCAT.profiles.size(); s++) {
				eduroamCAT.profiles.get(s).setConfigError(getString(R.string.config_discarded));
			}
		}
		//same as above for eap profile.
		finish();
		System.exit(0);
	}
	
	//Install profiles to DB
	public void commitProfile()
	{
		eduroamCAT.debug("committing...");
		if (eduroamCAT.profiles!=null) { 
			if (eduroamCAT.profiles.size()>0 && eduroamCAT.wifiProfile.hasError()==false)
			{  	
				//String wifiTmp=eduroamCAT.wifiProfile.getSSID()+":("+eduroamCAT.wifiProfile.getAuthType()+"/"+eduroamCAT.wifiProfile.getEncryptionType();
				String wifiTmp=eduroamCAT.wifiProfile.getSSID()+":";
				eduroamCAT.debug("committing2:profile size="+eduroamCAT.profiles.size());
				for (int profilescount=0; profilescount<eduroamCAT.profiles.size();profilescount++)
				{
					eduroamCAT.debug("committing count:"+profilescount);
					db.deleteAllProfiles();
					ConfigProfile aProfile = eduroamCAT.profiles.get(profilescount);
					//if (aProfile.getLogo().toString()==null) aProfile.setLogo("a", "a", "a");				
					String authMethod="";
					//add to DB
					//db.insertEAP(aProfile.getEAPIdP_ID(), aProfile.getDisplayName(), "", aProfile.getLogo().toString(), aProfile.getTermsOfUse(), aProfile.getSupportEmails(), aProfile.getHelpdeskPhoneNumber(), aProfile.getHelpdeskURL().toString());
					String web = "";
					if (aProfile.getHelpdeskURL()!=null) web = aProfile.getHelpdeskURL().toString();
					db.insertEAP(aProfile.getEAPIdP_ID(), aProfile.getDisplayName(), "",aProfile.getDescription(),"", aProfile.getTermsOfUse(), aProfile.getSupportEmails(), aProfile.getHelpdeskPhoneNumber(), web);
					eduroamCAT.debug("Inserting EAP Profile:"+aProfile.getEAPIdP_ID());
					if (aProfile.getNumberAuthenticationMethods()>0)
					{
						eduroamCAT.debug("committing auth method:"+aProfile.getNumberAuthenticationMethods());
						for (int i=0;i<aProfile.getNumberAuthenticationMethods();i++)
						{
							eduroamCAT.debug("committing auth count:"+i);
							if (aProfile.getAuthenticationMethod(i).isError()) 
								{
									eduroamCAT.debug("AUTH METHOD "+i+" has error");
									continue;
								}
							AuthenticationMethod aAuthMethod = aProfile.getAuthenticationMethod(i);
							String serverIDs="";
							for (int s=0; s<aAuthMethod.getServerIDs().size(); s++)
							{
								serverIDs += aAuthMethod.getServerIDs().get(s)+";";
							}
							db.insertAuth(aProfile.getEAPIdP_ID(), aAuthMethod.getOuterEAPType(), aAuthMethod.getInnerEAPType(), aAuthMethod.getInnerNonEAPType(), aAuthMethod.getCAencoding(),aAuthMethod.getCAFormat(), serverIDs, aAuthMethod.getOrignalClientCert(), aAuthMethod.getClientCertEncoding(), aAuthMethod.getClientCertFormat(), aAuthMethod.getClientCertPass(),aAuthMethod.getAnonID(), 1, aAuthMethod.getOrignalCACert());
							eduroamCAT.debug("Inserting Auth method for EAP Profile:"+aProfile.getEAPIdP_ID());
						}
					}
					else
					{
						eduroamCAT.debug("No Auth Profiles found for commit...");
					}
				}
			}
			else
			{
				eduroamCAT.debug("No EAP Profiles found for commit...");
			}
		}
		else
		{
			eduroamCAT.debug("No Profiles Found to commit.");
		}

	}
	
	//Discard button press
	public void onInstallClick(View view)
	{
		if (eduroamCAT.profiles!=null)
		if (eduroamCAT.profiles.size()>0)
			if (!eduroamCAT.profiles.get(eduroamCAT.profiles.size()-1).isError())
			{
				String terms=eduroamCAT.profiles.get(eduroamCAT.profiles.size()-1).getTermsOfUse();
				String message=getString(R.string.profile_install_confirm);
				if (terms.length()>0) message=getString(R.string.profile_install_confirm2)+terms;
				new AlertDialog.Builder(this)
				.setTitle(getString(R.string.profile_install_title))
				.setMessage(message)
				.setPositiveButton(getString(R.string.button_yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) { 
						eduroamCAT.debug("YES Install Profile");
						Intent start = new Intent(getApplicationContext(),eduroamCAT.class);
						commitProfile();
						//finish()
						startActivity(start);
						finish();
					}
				})
				.setNegativeButton(getString(R.string.button_no), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) { 
						eduroamCAT.debug("NO Install Profile");
						eduroamCAT.profiles.clear();
						eduroamCAT.wifiProfile.hasError();
						//destroy instead of error?
					}
				})
				//.setIcon(R.drawable.ic_dialog_alert)
				.show();
				// finish();
			}

		if (eduroamCAT.profiles!=null)
		if (eduroamCAT.profiles.size()>0)
			if (eduroamCAT.profiles.get(eduroamCAT.profiles.size()-1).isError())
			{
				//if eap-tls, request pin again
				int lastAuthMethod = 0;
				lastAuthMethod = eduroamCAT.profiles.get(eduroamCAT.profiles.size()-1).getNumberAuthenticationMethods();
				if (eduroamCAT.profiles.get(eduroamCAT.profiles.size()-1).getAuthenticationMethod(lastAuthMethod-1).getOuterEAPType()==13)
					if (eduroamCAT.profiles.get(eduroamCAT.profiles.size()-1).getAuthenticationMethod(lastAuthMethod-1).getClientPrivateKey()==null)
					{
						requestKeypass(getString(R.string.pinDialog),getString(R.string.pinDialog),this, clientCert);
					}
			}
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

    public static void requestKeypass(String message, String title, final Activity activ, final NodeList clientCertx)
    {
		// Set an EditText view to get user input
		final EditText input = new EditText(activ);

		new AlertDialog.Builder(activ)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
						String pin = input.getText().toString();
						eduroamCAT.debug("PIN=" + pin);
						String certstring = "";
						Element Clientcert = null;
						if (clientCertx.getLength()>0) clientCert = clientCertx;
						if (clientCert.getLength() > 0) {
							for (int s = 0; s < clientCert.getLength(); s++) {
								Clientcert = (Element) clientCert.item(s);
								String tmp = null;
								tmp = Clientcert.getTextContent().trim();

								//get recent eap-tld profile, and add the client sert to any eap-tls auth methods
								if (eduroamCAT.profiles != null)
									if (eduroamCAT.profiles.size() > 0) {
										//get last profile added
										ConfigProfile aProfile = eduroamCAT.profiles.get(eduroamCAT.profiles.size() - 1);
										if (aProfile.getNumberAuthenticationMethods() > 0) {
											eduroamCAT.debug("eap-tls auth methods:" + aProfile.getNumberAuthenticationMethods());
											for (int i = 0; i < aProfile.getNumberAuthenticationMethods(); i++) {
												AuthenticationMethod aAuthMethod = aProfile.getAuthenticationMethod(i);
												eduroamCAT.debug("got auth method with eap inner=" + aAuthMethod.getOuterEAPType());
												if (aAuthMethod.getOuterEAPType() == 13)
													try {
														eduroamCAT.debug("adding client cert:" + tmp + "with pin " + pin);
														if (aAuthMethod.loadClientCert(tmp, Clientcert.getAttribute("format"), Clientcert.getAttribute("encoding"), pin)) {
															aAuthMethod.setClientCertPass(pin);
															aAuthMethod.clearConfigError();
															aProfile.removeAuthenticationMethod(i);
															aProfile.addAuthenticationMethod(aAuthMethod);
															eduroamCAT.profiles.set(eduroamCAT.profiles.size() - 1, aProfile);
															eduroamCAT.profiles.get(eduroamCAT.profiles.size() - 1).clearConfigError();
														} else {
															eduroamCAT.profiles.get(eduroamCAT.profiles.size() - 1).setConfigError("Client cert error");
															Toast.makeText(activ, activ.getString(R.string.pinFailed), Toast.LENGTH_LONG).show();
														}
													} catch (KeyStoreException e) {
														e.printStackTrace();
														eduroamCAT.profiles.get(eduroamCAT.profiles.size() - 1).setConfigError("Client cert error");
														Toast.makeText(activ, activ.getString(R.string.pinFailed), Toast.LENGTH_LONG).show();
													}
											}
										}
									}
							}
						}
					}
                })
				.setNegativeButton(R.string.discard_button, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						//cancel install of
						eduroamCAT.debug("User discard...");
					}
				})
				.setView(input)
                .show();
    }
	
	public ArrayList<ConfigProfile> parseProfile(String config) throws IOException, ParserConfigurationException, SAXException
	{
		ArrayList<ConfigProfile> profiles = new ArrayList<ConfigProfile>();
		//parse XML
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder configBuilder = factory.newDocumentBuilder();
	    InputSource is = new InputSource(new StringReader(config));
	    Document parsedConfig = configBuilder.parse(is);
	    
	    eduroamCAT.debug("Builder="+configBuilder.toString());
	    if (parsedConfig.getXmlVersion()=="1.0") eduroamCAT.debug("Version OK");
	    else { eduroamCAT.debug("Version Invalid"); return profiles; }
	    
	    
	    //Iterate each EAPIdP in EAPIdPList
	    //Element root = parsedConfig.getDocumentElement();
	    NodeList EAPIdP_list = parsedConfig.getElementsByTagName("EAPIdentityProvider");
	    for(int i=0; i<EAPIdP_list.getLength(); i++)
	    {
	    	ConfigProfile config1x = new ConfigProfile();
	    	//FIRST GET EAPIdP Name/ID
	    	Element EAPIdP_ID = (Element) EAPIdP_list.item(i);
	    	config1x.setEAPIdP_ID(EAPIdP_ID.getAttribute("ID"));
	    	eduroamCAT.debug("Got EAPIdP_ID="+config1x.getEAPIdP_ID());
	    	
	    	//***************************************************************AUTHENTICATION METHODS
		    NodeList authMethods = EAPIdP_ID.getElementsByTagName("AuthenticationMethod");
		    if (authMethods.getLength()>0)
		    for(int j=0; j<authMethods.getLength(); j++)
		    {
		    	eduroamCAT.debug("*********Authenticationmethod #"+j);
		    	AuthenticationMethod newAuthMethod = new AuthenticationMethod();
		    	Element authElement = (Element) authMethods.item(j);
		    	
		    	//Get EAPMethod
		    	NodeList authList = authElement.getElementsByTagName("EAPMethod");
		    	if (authList.getLength()>0)
			    {
		    		Element eapMethod = (Element) authList.item(0);
		    		//eduroamCAT.debug("eapMethod="+eapMethod.getTextContent().trim());
		    		newAuthMethod.setOuterEAPType(Integer.parseInt(eapMethod.getTextContent().trim()));
		    	}
		    	else newAuthMethod.setConfigError(getString(R.string.error_with)+"Outer EAP Type");
		    	eduroamCAT.debug("Got OuterEAPType="+newAuthMethod.getOuterEAPType());
		    	
//		    	//get server side credential
		    	NodeList serverSideCredentials = authElement.getElementsByTagName("ServerSideCredential");
		    	if (serverSideCredentials.getLength()>0)
			    {
		    		eduroamCAT.debug("Got ServerSideCredentials");
			    	
		    		//get CA cert
			    	NodeList CA = authElement.getElementsByTagName("CA");
			    	String certstring="";
			    	Element CAcert=null;
			    	if (CA.getLength()>0) {
			    	for(int s=0; s<CA.getLength(); s++)
			    	{
			    		CAcert = (Element) CA.item(s);
			    		String tmp = null;
			    		tmp = CAcert.getTextContent().trim();
			    		if (newAuthMethod.setCAcert(tmp,CAcert.getAttribute("format"),CAcert.getAttribute("encoding")))
			    		break;
			    	}
			    	eduroamCAT.debug("******************************************");
			    	}

				   	//get server ids
				   	NodeList serverID = authElement.getElementsByTagName("ServerID");
				   	if (serverID.getLength()>0)
				   	for(int s=0; s<serverID.getLength(); s++)
				    { 
				   		Element aserverID = (Element) serverID.item(s);
				   		newAuthMethod.addServerID(aserverID.getTextContent());
				    }
				   	//else config1x.setConfigError("No server IDs");
				   	
			    }
		    	//****************************************************************INNER
			    NodeList inner = authElement.getElementsByTagName("InnerAuthenticationMethod");
			    if (inner.getLength()>0)
			    for(int k=0; k<inner.getLength(); k++)
			    {
		    	
			    	//get inner eap type
			    	Element innerElement = (Element) inner.item(k);
			    	
			    	NodeList innerEAPTypelist = innerElement.getElementsByTagName("EAPMethod");
			    	if (innerEAPTypelist.getLength()==1) 
			    	{
			    		Element iEAPType = (Element) innerEAPTypelist.item(0);
			    		eduroamCAT.debug("InnerEAPMethod="+innerEAPTypelist.getLength());
			    		newAuthMethod.setInnerEAPType(Integer.parseInt(iEAPType.getTextContent().trim()));
			    	} 
			    	else {
			    		NodeList innerEAPTypelist2 = innerElement.getElementsByTagName("NonEAPAuthMethod");
				    	if (innerEAPTypelist2.getLength()==1) 
				    	{
				    		Element iEAPType = (Element) innerEAPTypelist2.item(0);
				    		eduroamCAT.debug("InnerNonEAPAuthMethod="+innerEAPTypelist2.getLength());
				    		newAuthMethod.setInnerEAPType(Integer.parseInt(iEAPType.getTextContent().trim()));
				    	} 
			    	}
		    		if (newAuthMethod.getInnerEAPType()==0 && newAuthMethod.getInnerNonEAPType()==0)
		    		{ 
		    			if (newAuthMethod.getInnerEAPType()==0) newAuthMethod.setInnerEAPType(-1);
		    			else newAuthMethod.setInnerNonEAPType(-1);
		    		}
		    		eduroamCAT.debug("Got InnerEAPType="+newAuthMethod.getInnerEAPType());
	   	
			    }
			    
			  //****************************************************************ClientSideCredentials
			    NodeList clientSide = authElement.getElementsByTagName("ClientSideCredential");
			    if (clientSide.getLength()>0) {
					eduroamCAT.debug("ClientSideCredential cert="+clientSide.toString());
			    	Element clientSideElement = (Element) clientSide.item(0);
			    	Boolean allow_save = true;
			    	allow_save = Boolean.valueOf(clientSideElement.getAttribute("allow_save"));
			    	for(int k=0; k<clientSide.getLength(); k++)
			    	{
			    		eduroamCAT.debug("Got ClientSideCredential"+clientSide.getLength());
			    		Element innerElement = (Element) clientSide.item(k);
			    		NodeList anonID = innerElement.getElementsByTagName("OuterIdentity");
			    		if (anonID.getLength()>0)
			    		{
			    			Element anonIDvalue = (Element) anonID.item(0);
			    			newAuthMethod.setAnonID(anonIDvalue.getTextContent(),allow_save);
			    			eduroamCAT.debug("Got anonID="+anonIDvalue.getTextContent());
			    		}

						//get Client cert
                        //get keypass from user
						clientCert = authElement.getElementsByTagName("ClientCertificate");
                        if (clientCert.getLength()>0) requestKeypass(getString(R.string.pinDialog),getString(R.string.pinDialog),this,clientCert);
			    	}
			    }
			    
			    //add new authentication mode to config
			    config1x.addAuthenticationMethod(newAuthMethod);
		    }
		    else
		    {
		    	config1x.setConfigError(getString(R.string.error_with)+"Outer Authentication Method");
		    }
		    
		    eduroamCAT.debug("AuthenticatoinMethod Count="+config1x.getNumberAuthenticationMethods());

			//***************************************************************CredentialApplicability
			NodeList credApplic = EAPIdP_ID.getElementsByTagName("CredentialApplicability");
			if (credApplic.getLength()>0)
				for(int s=0; s<credApplic.getLength(); s++)
				{
					Element cred = (Element) credApplic.item(s);
					eduroamCAT.debug("Got CredentialApplicability");
					NodeList IEEE80211 = cred.getElementsByTagName("IEEE80211");
					if (IEEE80211.getLength()>0)
						for(int m=0; m<IEEE80211.getLength(); m++)
						{
							Element IEEE80211prop = (Element) IEEE80211.item(m);
							NodeList ssid,minRSNProto;
                            String ssidValue="",minRSNProtoValue="";
							ssid = IEEE80211prop.getElementsByTagName("SSID");
							if (ssid.getLength()>0)
                            {
                                Element ssidElement = (Element) ssid.item(0);
                                ssidValue=ssidElement.getTextContent();
                            }
                            minRSNProto = IEEE80211prop.getElementsByTagName("MinRSNProto");
                            if (minRSNProto.getLength()>0)
                            {
                                Element minRSNProtoElement = (Element) minRSNProto.item(0);
                                minRSNProtoValue = minRSNProtoElement.getTextContent();
                            }
							eduroamCAT.debug("Got SSID with proto:"+ssidValue+"/"+minRSNProtoValue);

							//if (IEEE80211prop.getTextContent().length()>0)	config1x.setHelpdeskEmail(IEEE80211prop.getTextContent(), ssif);
						}
				}
		    
	    	//***************************************************************PROVIDER INFO
		    NodeList providerInfo = EAPIdP_ID.getElementsByTagName("ProviderInfo");
		    if (providerInfo.getLength()==1)
		    {
		    	Element providerElement = (Element) providerInfo.item(0);
		    	
		    	NodeList displayNameList = providerElement.getElementsByTagName("DisplayName");
			   	if (displayNameList.getLength()>0)
			   	for(int s=0; s<displayNameList.getLength(); s++)
			   	{
			   		Element displayName = (Element) displayNameList.item(s);
			   		if (displayName.getTextContent().length()>0) 
			   		{
			   			String displayNameLang = "";
			   			displayNameLang = displayName.getAttribute("lang");
			   			if (displayNameLang.length()>0) config1x.setDisplayName(displayName.getTextContent().trim(),displayNameLang);
			   			else config1x.setDisplayName(displayName.getTextContent().trim(),"en");
			   		}
		    		eduroamCAT.debug("Got Display Name:"+config1x.getDisplayName());
			   	}
			   	
		    	NodeList descriptionList = providerElement.getElementsByTagName("Description");
			   	if (descriptionList.getLength()>0)
			   	for(int s=0; s<descriptionList.getLength(); s++)
			   	{
			   		Element description = (Element) descriptionList.item(s);
			   		if (description.getTextContent().length()>0) 
			   		{
			   			String descriptionLang = "";
			   			descriptionLang = description.getAttribute("lang");
			   			if (descriptionLang.length()>0) config1x.setDescription(description.getTextContent().trim(),descriptionLang);
			   			else config1x.setDescription(description.getTextContent().trim(),"en");
			   		}
		    		eduroamCAT.debug("Got Description:"+config1x.getDescription());
			   	}
	    	//***************************************************************PROVIDER LOCATION
		    	NodeList locationList = providerElement.getElementsByTagName("ProviderLocation");
		    	double longitude=0, latitude=0;
			   	if (locationList.getLength()>0)
			   	{
			   	for(int s=0; s<locationList.getLength(); s++)
			   	{
			   		Element location = (Element) locationList.item(s);
					NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
			   		NodeList latList = location.getElementsByTagName("Latitude");
			   		if (latList.getLength()>0) {
			   			Element lat = (Element) latList.item(0);
						try {
							Number number = format.parse(lat.getTextContent());
							longitude = number.doubleValue();
						}
						catch (ParseException e) {
							eduroamCAT.debug("latitude parse error");
						}
			   		}
			   		
			   		NodeList longList = location.getElementsByTagName("Longitude");
			   		if (longList.getLength()>0) {
						Element location2 = (Element) longList.item(0);
						try {
							Number number = format.parse(location2.getTextContent());
							longitude = number.doubleValue();
						}
						catch (ParseException e) {
							eduroamCAT.debug("longitude parse error");
						}
			   		}
			   	}
		   		
			   	if (latitude!=0 && longitude!=0)
			   	{
					config1x.setLocation(latitude,longitude);
			   		eduroamCAT.debug("Got Location:"+config1x.getLocation());
			   	}
			   	}
		    
		    //***************************************************************PROVIDER LOCATION
		    	NodeList logoList = providerElement.getElementsByTagName("ProviderLogo");
			   	if (logoList.getLength()>0)
			   	for(int s=0; s<logoList.getLength(); s++)
			   	{
			   		Element logo = (Element) logoList.item(s);
			   		String logoMime = "", logoEncoding="";
			   		logoMime = logo.getAttribute("mime");
			   		logoEncoding = logo.getAttribute("encoding");
			   		if (logoMime.length()>0 && logoEncoding.length()>0) 
			   			config1x.setLogo(logo.getTextContent(),logoMime,logoEncoding);
		    		eduroamCAT.debug("Got logo:"+config1x.getLogo());
			   	}
	    	//***************************************************************TERMS OF USE
		    	NodeList touList = providerElement.getElementsByTagName("TermsOfUse");
			   	if (touList.getLength()>0)
			   	for(int s=0; s<touList.getLength(); s++)
			   	{
			   		Element tou = (Element) touList.item(s);
			   		if (tou.getTextContent().length()>0) config1x.setTermsOfUse(tou.getTextContent().trim());
		    		eduroamCAT.debug("Got Terms of Use:"+config1x.getTermsOfUse());
			   	}

	    	//***************************************************************HELPDESK
	    		    	
	    NodeList helpdeskList = providerElement.getElementsByTagName("Helpdesk");
	    if (helpdeskList.getLength()>0) {
	    for(int h=0; i<helpdeskList.getLength(); i++)
	    {
	    	  Element helpdeskItem = (Element) helpdeskList.item(h);
	    	  
		   		NodeList emailList = helpdeskItem.getElementsByTagName("EmailAddress");
		   		if (emailList.getLength()>0) 
		   			for(int m=0; m<emailList.getLength(); m++)
		   			{
		   				Element aemail = (Element) emailList.item(m);
		   				String lang="";
		   				lang = aemail.getAttribute("lang");
		   				if (aemail.getTextContent().length()>0)	config1x.setHelpdeskEmail(aemail.getTextContent(), lang);
		   			}

		   		NodeList webList = helpdeskItem.getElementsByTagName("WebAddress");
		   		if (webList.getLength()>0)
		   			for(int m=0; m<webList.getLength(); m++)
		   			{
		   				Element aweb = (Element) webList.item(m);
		   				String lang="";
		   				lang = aweb.getAttribute("lang");
		   				if (aweb.getTextContent().length()>0) config1x.setHelpdeskURL(aweb.getTextContent(), lang);
		   			}
		   		
		   		NodeList phoneList = helpdeskItem.getElementsByTagName("Phone");
		   		if (phoneList.getLength()>0) 
		   			for(int m=0; m<phoneList.getLength(); m++)
		   			{
		   				Element aphone = (Element) phoneList.item(m);
		   				String lang="";
		   				lang = aphone.getAttribute("lang");
		   				if (aphone.getTextContent().length()>0) config1x.setHelpdeskPhone(aphone.getTextContent(), lang);
		   			}
	    }
	    config1x.setHelpdeskInfo(true);
	    eduroamCAT.debug("Got Helpdesk Email="+config1x.getSupportEmails());
	    eduroamCAT.debug("Got Helpdesk Web="+config1x.getHelpdeskURL());
	    eduroamCAT.debug("Got Helpdesk Phone="+config1x.getHelpdeskPhoneNumber(""));
	    }
		    }
		    profiles.add(config1x);
		    }
	    	

	    eduroamCAT.debug("Number of profiles="+profiles.size());
		return profiles;
	}
	


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		
		return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

  	@Override
  	public void onDestroy()
  	{
  		Log.d("eduroamCAT", "***** EAPMetadata onDestroy()");
  		super.onDestroy();
  		//finish();
  	}
  	
  	@Override
  	protected void onResume() 
  	{
  		Log.d("eduroamCAT", "***** EAPMetadata onResume()");
  		super.onResume();
  	}
  	
  	@Override
  	protected void onRestart() 
  	{
  		Log.d("eduroamCAT", "***** EAPMetadata onRestart()");
  		super.onRestart();
  	}

  	@Override
  	protected void onPause() 
  	{
  		Log.d("eduroamCAT", "***** EAPMetadata onPause()");
  		//finish();
  		super.onPause();
  	}
  	
  	@Override
  	protected void onStart() {
  		Log.d("eduroamCAT", "***** EAPMetadata onStart()");
  		super.onStart();
  	}
	
}
