//*******************
//Author: Gareth Ayres <g.j.ayres@swansea.ac.uk>
//SENSE Project
//https://github.com/GEANT/CAT-Android
//*******************
package uk.ac.swansea.eduroamcat;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import android.annotation.TargetApi;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiEnterpriseConfig.Eap;
import android.net.wifi.WifiEnterpriseConfig.Phase2;
import android.net.wifi.WifiManager;

@TargetApi(18)
public class WifiConfigAPI18 {
	
	WifiManager wifi;
	
    public WifiConfigAPI18()
    {
    	wifi = eduroamCAT.getWifiManager();
    }
    
    private void debug(String msg)
    {
    	StatusFragment.setDebug(msg);
    }
        
  
    public boolean saveEapConfig(String userName,String passString, AuthenticationMethod aAuth)
    //public void saveEapConfig(String userName,String passString, String sSID)
    {
  
	        /*Create a WifiConfig*/
	        WifiConfiguration selectedConfig = new WifiConfiguration();
	        WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
	        	        	        
	        //get Authentication Method
	        if (aAuth.isError()) 
	        	{
	        		eduroamCAT.debug("AuthenticationMethod error");
	        		return false; 
	        	}
	        
	        /*AP Name*/
	        selectedConfig.SSID = "\""+eduroamCAT.wifiProfile.getSSID()+"\"";
	        debug("Configuring connection to:"+eduroamCAT.wifiProfile.getSSID());

	        /*Priority*/
	        selectedConfig.priority = eduroamCAT.wifiProfile.getSSIDPriority();
	        debug("Setting SSID priority to "+eduroamCAT.wifiProfile.getSSIDPriority());

	        /*Enable Hidden SSID*/
	        //selectedConfig.hiddenSSID = true;
	        
	        selectedConfig.allowedAuthAlgorithms.clear();
	        selectedConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
	        
	        /*Key Mgmnt*/
	        selectedConfig.allowedKeyManagement.clear();
	        //selectedConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
	        selectedConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);

	        /*Group Ciphers*/
	        selectedConfig.allowedGroupCiphers.clear();
	        if (eduroamCAT.wifiProfile.getEncryptionType().equals("CCMP")) {
	        	selectedConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
	        	selectedConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
	        }
	        if (eduroamCAT.wifiProfile.getEncryptionType().equals("TKIP")) selectedConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
	        debug("Setting Encryption to:"+eduroamCAT.wifiProfile.getEncryptionType());

	        /*Pairwise ciphers*/
	        selectedConfig.allowedPairwiseCiphers.clear();
	        if (eduroamCAT.wifiProfile.getEncryptionType().equals("CCMP")) selectedConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
	        if (eduroamCAT.wifiProfile.getEncryptionType().equals("TKIP")) selectedConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP	);
	        
	        /*Protocols*/
	        selectedConfig.allowedProtocols.clear();
	        if (eduroamCAT.wifiProfile.getAuthType().equals("WPA2")) selectedConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
	        if (eduroamCAT.wifiProfile.getAuthType().equals("WPA")) selectedConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
	        debug("Setting Authentication to:"+eduroamCAT.wifiProfile.getAuthType());
	        
	        //http://stackoverflow.com/questions/18016446/setting-the-eap-method-in-enterpriseconfig-on-android-4-3
	        //http://www.iana.org/assignments/eap-numbers/eap-numbers.xhtml
	        
	        /*EAP method*/
	        String outter="";
	        eduroamCAT.debug("Outter:"+aAuth.getOuterEAPType());
	        if (aAuth.getOuterEAPType() == 25) {
	        	outter="PEAP";
	        	enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.PEAP);
	        }
	        else if (aAuth.getOuterEAPType() == 21) {
	        	outter="TTLS";
	        	enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.TTLS);
	        }
	        else if (aAuth.getOuterEAPType() == 13) {
	        	outter="TLS";
	        	enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.TLS);
	        }
			else if (aAuth.getOuterEAPType() == 52) {
				outter="PWD";
				enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.PWD);
			}
			else {
	        	outter="ERROR";
	        	eduroamCAT.debug("ERROR:no outter eap type");
	        	enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.PEAP);
	        	return false; 
	        } 
	        
	        debug("Setting Outter EAP Method to:"+outter);
	        if (enterpriseConfig.getEapMethod()==Eap.PEAP) debug("Set to PEAP OK");
	        if (enterpriseConfig.getEapMethod()==Eap.PWD) debug("Set to PWD OK");
	        if (enterpriseConfig.getEapMethod()==Eap.TLS) debug("Set to TLS OK");
	        if (enterpriseConfig.getEapMethod()==Eap.TTLS) debug("Set to TTLS OK");
			if (enterpriseConfig.getEapMethod()==Eap.PWD) debug("Set to PWD OK");
	        
	        /*EAP Phase 2*/
	        eduroamCAT.debug("Inner="+aAuth.getInnerEAPType());
	        String phase2="none";
	        if (aAuth.getInnerEAPType() == 26) { 
	        	enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.MSCHAPV2);  
	        	phase2="MSCHAPv2";
	        	eduroamCAT.debug("Setting to "+phase2);
	        }
	        else if (aAuth.getInnerEAPType() == 6) {
	        	enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.GTC); 
	        	phase2="GTC";
	        	eduroamCAT.debug("Setting to "+phase2);
	        }
	        else if (aAuth.getInnerEAPType() == 1) {
	        	enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.PAP); 
	        	phase2="PAP";
	        	eduroamCAT.debug("Setting to "+phase2);
	        }	        
	        else if (aAuth.getInnerNonEAPType()==0) {
	        	phase2="NONE";
	        	eduroamCAT.debug("Setting to "+phase2);
	        }
	        else { 
	        	eduroamCAT.debug("ERROR:no inner eap type"); 
	        	phase2="ERROR"; 
	        	return false; 
	        } 
	        
	        debug("Set Phase2 to:"+phase2);
	        if (enterpriseConfig.getPhase2Method()==Phase2.MSCHAPV2) debug("Set to MSCHAPv2 OK");
	        if (enterpriseConfig.getPhase2Method()==Phase2.GTC) debug("Set to GTC OK");
	        if (enterpriseConfig.getPhase2Method()==Phase2.PAP) debug("Set to PAP OK");

	        /*Anon ID*/
	        if (aAuth.getAnonID().length()>0) enterpriseConfig.setAnonymousIdentity(aAuth.getAnonID());
	        debug("Set Anon to:"+aAuth.getAnonID());
	        
	        /*Cert*/
	        if (aAuth.getCAcert() != null && aAuth.getCAcert().toString().length()>0)
	        try 
	        {
				debug("Setting cert:"+aAuth.getCAcert().toString());
	        	eduroamCAT.debug("Installing cert...");
	        	enterpriseConfig.setCaCertificate((X509Certificate) aAuth.getCAcert());
	        	eduroamCAT.debug("Cert installed="+enterpriseConfig.getCaCertificate());
	        }
	        catch (IllegalArgumentException e) {
				eduroamCAT.debug("cert install error:\n" + e);
				eduroamCAT.debug("for cert:\n" + aAuth.getCAcert().toString());
				eduroamCAT.debug("Length:\n" + aAuth.getCAcert().toString().length());
			}
	        
	        //test comma delimited!
	        if (aAuth.getServerIDs().size()>0)
	        {
	        	String subjectMatch="";
	        	if (aAuth.getServerIDs().size()>1)
	        	{
	        		String subjectMatch_next=aAuth.getServerIDs().get(0);
	        		String subjectMatch_new="";
	        		if (subjectMatch_next.indexOf(".")>0){
	        			subjectMatch_new=subjectMatch_next.substring(subjectMatch_next.indexOf("."));
	        			subjectMatch=subjectMatch_new;
	        		}
	        		for (int serverCount=0; serverCount<aAuth.getServerIDs().size(); serverCount++)
	        		{
	        			subjectMatch_next=aAuth.getServerIDs().get(serverCount);
	        			if (subjectMatch_next.indexOf(".")>0)
	        			subjectMatch_new=subjectMatch_next.substring(subjectMatch_next.indexOf("."));
	        			if (subjectMatch.equals(subjectMatch_new)) continue;
	        			else {
	        				//error with serverIDs
	        				StatusFragment.setDebug("ServerID error with profile:"+subjectMatch+" and "+subjectMatch_new);
	        				eduroamCAT.debug("ServerID error with profile:"+subjectMatch+" and "+subjectMatch_new);
	        				subjectMatch="";
	        				break;
	        			}
	        		}
	        	}
	        	else subjectMatch=aAuth.getServerIDs().get(0);
	        		
	        	enterpriseConfig.setSubjectMatch(subjectMatch);
	        	eduroamCAT.debug("subjectMatch="+subjectMatch);
	        	//enterpriseConfig.setSubjectMatch("bouncer.swan.ac.uk;radauth.swan.ac.uk;radauth2.swan.ac.uk");
	        	//enterpriseConfig.setSubjectMatch("C=GB, ST=Wales, L=Swansea, O=Swansea University, OU=ISS/emailAddress=postmaster@swansea.ac.uk, CN=Swansea University Certificate Authority");
	        	//enterpriseConfig.setSubjectMatch("radauth.swan.ac.uk bouncer.swan.ac.uk radauth2.swan.ac.uk");
	        	//enterpriseConfig.setSubjectMatch("radauth.swan.ac.uk");
	        }
	        
	        //User Cert
            if (aAuth.getOuterEAPType()==13)
			if (aAuth.getClientPrivateKey() != null)
			try
			{
				debug("Setting client cert:"+aAuth.getClientPrivateKey().toString());
				eduroamCAT.debug("Installing client cert...");
				//if api >26
				//enterpriseConfig.setClientKeyEntry(aAuth.getClientPrivateKey(), aAuth.getClientChain());
				//else
				enterpriseConfig.setClientKeyEntry(aAuth.getClientPrivateKey(), aAuth.getClientCert());
				eduroamCAT.debug("Client Cert installed="+enterpriseConfig.getClientCertificate());
			}
			catch (IllegalArgumentException e) {
				eduroamCAT.debug("cert install error:\n" + e);
				eduroamCAT.debug("for cert:\n" + aAuth.getClientPrivateKey().toString());
				eduroamCAT.debug("Length:\n" + aAuth.getClientPrivateKey().toString().length());
			}
	        
	        /*User and pass if not TLS*/
			if (aAuth.getOuterEAPType()!=13) {
				enterpriseConfig.setIdentity(userName);
				debug("Using Username:"+userName);
				enterpriseConfig.setPassword(passString);
			}

			//just identity for TLS
			if (aAuth.getOuterEAPType()==13) {
				if (userName!=null) {
					if (userName.length() == 0)
						if (aAuth.getClientPrivateKeySubjectCN() != null && aAuth.getClientPrivateKeySubjectCN().length() > 0) {
							userName = aAuth.getClientPrivateKeySubjectCN();
						}
				}
                else
                {
                    if ( aAuth.getClientPrivateKeySubjectCN() != null && aAuth.getClientPrivateKeySubjectCN().length()>0) {
                        userName = aAuth.getClientPrivateKeySubjectCN();
                    }
                }
				enterpriseConfig.setIdentity(userName);
				debug("Using Username:" + userName);
			}
	        
	        selectedConfig.enterpriseConfig=enterpriseConfig;
	        
	        //Add network profile
		int res = wifi.addNetwork(selectedConfig);
		boolean enabled = false;
		if (res<0) {
	        	//failure
	        	debug("Adding profile ("+eduroamCAT.wifiProfile.getSSID()+") FAILED, returned " + res );
	        }
	        else
	        {
	        	debug("Successfully added ("+eduroamCAT.wifiProfile.getSSID()+"), Network id=" + res );
				try {
					boolean c = wifi.saveConfiguration();
					debug("Saving configuration... " + c);
					enabled = wifi.enableNetwork(res, true);
					debug("Setting (" + eduroamCAT.wifiProfile.getSSID() + ") to Enabled returned " + enabled);
				}
				catch (Exception e)
				{
					eduroamCAT.debug("caught error with save eap config and enable " + e);
				}
	        }
	        
	        debug("FINISHED SETUP FOR IDENTITY:"+selectedConfig.enterpriseConfig.getIdentity());
	        if (selectedConfig.status==WifiConfiguration.Status.CURRENT) debug("Current****");
	        if (selectedConfig.status==WifiConfiguration.Status.DISABLED) debug("Disabled****");
	        if (selectedConfig.status==WifiConfiguration.Status.ENABLED) debug("Enabled****");
	        
	        return enabled;

    }
}
