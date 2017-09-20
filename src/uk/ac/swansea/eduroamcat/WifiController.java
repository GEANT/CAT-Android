//*******************
//Author: Gareth Ayres <g.j.ayres@swansea.ac.uk>
//SENSE Project
//https://github.com/GEANT/CAT-Android
//*******************
package uk.ac.swansea.eduroamcat;

import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.List;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiEnterpriseConfig.Eap;
import android.net.wifi.WifiEnterpriseConfig.Phase2;

public class WifiController 
{
 eduroamCAT eduroamcat;
 WifiManager wifi;
 List <WifiConfiguration> currentConfigs;
 List <ScanResult> scan;
 private String[] ssid_array;
 ConnectivityManager connectivity;
 
 
 	public WifiController(eduroamCAT eduraomcat, Object connect)
 	{
 		this.eduroamcat=eduraomcat;
 		wifi = eduroamCAT.getWifiManager();
 		connectivity = (ConnectivityManager) connect;
 	}
 	
    private void debug(String msg)
    {
    	StatusFragment.setDebug(msg);
    }
    
	public boolean checkWifiEnabled()
	{
		if (!wifi.isWifiEnabled()) 
		{
			return false;
		}
		else return true;
	}
	
	public boolean isWifiEnabled()
	{
		if (!StatusFragment.isWiFiToggled()) return false;
		if (!wifi.isWifiEnabled()) 
		{
			eduroamcat.debug("isWifiEnabled = wifi disabled");
			//updateText("wifi disabled!");
			if (!wifi.setWifiEnabled(true))	
			{
				StatusFragment.setDebug("Wifi Enabled failed");
				//Toast.makeText(su1x, "Wifi Disabled!",3).show();
				return false;
			}
			else 
			{
				if (wifi.isWifiEnabled())
				{
					StatusFragment.setDebug("Wifi Enable success!");
					eduroamcat.debug("isWifiEnabled = wifi enable success");
					return true;
					//updateText("wifi enable success!");
				}
				else
				{
					StatusFragment.setDebug("Wifi Enabled failed but reported success");
					return false;
				}
			}
		}
		else 
			{
			eduroamcat.debug("isWifiEnabled = wifi enabled");
			if (StatusFragment.verbose) StatusFragment.setDebug("Wifi Enabled");
				return true;
			}
	}
	
	public boolean isSupplicantOK()
	{
		//Check supplicant status
		if (!wifi.pingSupplicant())
		{
			debug("Supplicant busy");
			//updateText("Supplicant disabled!");
			//Toast.makeText(su1x, "Wifi Supplicant unreachable!",3).show();
			return false;
		}
		else
		{
			if (StatusFragment.verbose) debug("Supplicant OK");
			return true;
		}
	}
	
	public boolean setWifiON()
	{
		eduroamcat.debug("SetWifiON");
		if (!wifi.setWifiEnabled(true))
		{
			debug("Wifi Enabled failed");
			return false;
		}
		else 
		{
			debug("Wifi Enabled Success");
			return true;
		}
	}
	
	public boolean setWifiOFF()
	{
		eduroamcat.debug("SetWifiOFF");
		if (!wifi.setWifiEnabled(false))
		{
			debug("Wifi Disable failed");
			return false;
		}
		else 
		{
			debug("Wifi Disabled Success");
			return true;
		}
	}	
	
//	public String getDHCPStatus()
//	{
//		String dHCPStatus="failed";
//		try{
//			ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo netInfo = cm.getNetworkInfo(0);
//            if (netInfo != null && netInfo.getState()==NetworkInfo.State.CONNECTED) {
//                status= true;
//            }else {
//                netInfo = cm.getNetworkInfo(1);
//                if(netInfo!=null && netInfo.getState()==NetworkInfo.State.CONNECTED)
//                    status= true;
//            }
//        }catch(Exception e){
//            e.printStackTrace();  
//            return false;
//        }
//		return dHCPStatus;
//	}

	//get current SSID
	public String getCurrentSSID()
	{
		String ssid="";
		ssid = eduroamCAT.getWifiManager().getConnectionInfo().getSSID();
		if (ssid==null) ssid="Unknown";
		if (ssid.equals("0x")) ssid="N/A";
		return ssid;
	}
	
	//get current BSSID
	public String getCurrentBSSID()
	{
		String ssid="";
		ssid = eduroamCAT.getWifiManager().getConnectionInfo().getBSSID();
		if (ssid==null) ssid="Unknown";
		if (ssid.equals("00:00:00:00:00")) ssid="N/A";
		return ssid;
	}
	
	//get current RSS
	public String getCurrentRSS()
	{
		String rss="";
		rss = String.valueOf((eduroamCAT.getWifiManager().getConnectionInfo().getRssi()));
		if (rss==null) rss="Unknown";
		if (rss.equals("-200")) rss="0";
		return rss;
	}

	//get device MACAdderss
	public String getDeviceWiFiMac()
	{
		String mac="Unknown";
		mac = String.valueOf((eduroamCAT.getWifiManager().getConnectionInfo().getMacAddress()));
		if (mac==null) mac="Unknown";
		return mac;
	}	


	//get link speed
	public String getLinkSpeed()
	{
		String speed="Unknown";
		speed=String.valueOf(eduroamCAT.getWifiManager().getConnectionInfo().getLinkSpeed());
		if (speed==null) speed="Unknown";
		if (speed.equals("-1")) speed="N/A";	;
		return speed;
	}
	
	
	public String getWifiState()
	{
		String status="failed";
		//NetworkInfo activeNetwork = connectivity.getActiveNetworkInfo();
		//boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
		//boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
		NetworkInfo wifiInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		State supState = wifiInfo.getState();
		if (supState==NetworkInfo.State.CONNECTED) return "Connected";
		if (supState==NetworkInfo.State.CONNECTING) return "Connecting";
		if (supState==NetworkInfo.State.DISCONNECTED) return "Disconnected";
		if (supState==NetworkInfo.State.DISCONNECTING) return "Disconnecting";
		if (supState==NetworkInfo.State.SUSPENDED) return "Suspended";
		if (supState==NetworkInfo.State.UNKNOWN) return "Unknown";
		return status;
	}
	
	public String getWifiStateDetailed()
	{
		String status="failed";
		//NetworkInfo activeNetwork = connectivity.getActiveNetworkInfo();
		//boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
		//boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
		//NetworkInfo wifiInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo wifiInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		DetailedState advancedStatus = wifiInfo.getDetailedState();
		return advancedStatus.toString();
	}
	
	public String getFailReason()
	{
		String reason="";
		//NetworkInfo activeNetwork = connectivity.getActiveNetworkInfo();
		//boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
		//boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
		NetworkInfo wifiInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		reason = wifiInfo.getReason();
		return reason;
	}
	
	public String getIPAddress()
	{
		WifiInfo wifiInfo = eduroamCAT.getWifiManager().getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		// Convert little-endian to big-endianif needed
	    if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
	        ipAddress = Integer.reverseBytes(ipAddress);
	    }
	    
	    if (ipAddress!=0) {
	    byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

	    String ipAddressString;
	    try {
	        ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
	    } catch (UnknownHostException ex) {
	        eduroamCAT.debug("Unable to get host address.");
	        ipAddressString = null;
	    }
	    if (ipAddressString==null) ipAddressString="none";
	    return ipAddressString;	
	    }
	    else return "none";
	}
	
	public String getSupplicantState()
	{
		String suplicantState="";
		SupplicantState supState = eduroamCAT.getWifiManager().getConnectionInfo().getSupplicantState();
		return supState.toString();
	}
	
	public String getDetailedSupplicantState()
	{
		String suplicantState="";
		SupplicantState supState = eduroamCAT.getWifiManager().getConnectionInfo().getSupplicantState();
		//eduroamCAT.getWifiManager().getConnectionInfo();
		NetworkInfo.DetailedState detailed = WifiInfo.getDetailedStateOf(supState);
		return detailed.toString();
	}
	
	
	public String[] getSSIDs()
	{
		if (wifi.isWifiEnabled()) 
		{
			scan = wifi.getScanResults();
			eduroamcat.debug("SCAN: "+scan.size()+" BSSIDs available");
			if (scan.size()>0)
			{
				ssid_array=new String[scan.size()];
				int count=0;
				eduroamcat.debug("array initialised to:"+ssid_array.length);
				for (ScanResult scan_result : scan)
				{
					ssid_array[count] = scan_result.SSID+"["+scan_result.capabilities.toString()+"]";
					eduroamcat.debug("Settings"+scan_result.capabilities.toString());
					count++;
				}
			return ssid_array;
			}
			else
			{
				ssid_array=new String[1];
				ssid_array[0]="No SSIDs found";
				return ssid_array;
			}
		}
		else
		{
			ssid_array=new String[1];
			ssid_array[0]="Wifi Disabled";
			return ssid_array;
		}
	}
	
	public String checkSSID(String sSID)
	{
		String result="";
		if (wifi.isWifiEnabled()) 
		{
			List <WifiConfiguration> networks = wifi.getConfiguredNetworks();
			currentConfigs = wifi.getConfiguredNetworks();
			if (currentConfigs !=null ) debug("Found "+currentConfigs.size()+" ssid profiles on device");
			else debug("Found 0 ssid profiles on device");
			boolean removed = false;
			for (WifiConfiguration currentConfig : currentConfigs) 
			{
				if (currentConfig.SSID.contains(sSID)) 
					{
						ConnectFragment.setStatus("Found SSID:"+sSID);
						StatusFragment.setDebug("Found SSID="+sSID+" with "+currentConfig.allowedGroupCiphers);
						eduroamCAT.debug("Found SSID="+sSID);
						eduroamCAT.debug("AUTHS:"+currentConfig.allowedAuthAlgorithms);
						//if (currentConfig.allowedAuthAlgorithms.get(WifiConfiguration.AuthAlgorithm.OPEN))
						eduroamCAT.debug("CYPHERS:"+currentConfig.allowedGroupCiphers);
						eduroamCAT.debug("KEYMGNT:"+currentConfig.allowedKeyManagement);
						eduroamCAT.debug("PAIRWISE:"+currentConfig.allowedPairwiseCiphers);
						eduroamCAT.debug("PROTOS:"+currentConfig.allowedProtocols);
						eduroamCAT.debug("ANON:"+currentConfig.enterpriseConfig.getAnonymousIdentity());
						eduroamCAT.debug("EAPMETHOD:"+currentConfig.enterpriseConfig.getEapMethod());
				        if (currentConfig.enterpriseConfig.getEapMethod()==Eap.PEAP) eduroamCAT.debug("Set to PEAP");
				        if (currentConfig.enterpriseConfig.getEapMethod()==Eap.PWD) eduroamCAT.debug("Set to PWD");
				        if (currentConfig.enterpriseConfig.getEapMethod()==Eap.TLS) eduroamCAT.debug("Set to TLS");
				        if (currentConfig.enterpriseConfig.getEapMethod()==Eap.TTLS) eduroamCAT.debug("Set to TTLS");
						eduroamCAT.debug("PHASE2:"+currentConfig.enterpriseConfig.getPhase2Method());
				        if (currentConfig.enterpriseConfig.getPhase2Method()==Phase2.MSCHAPV2) eduroamCAT.debug("Set to MSCHAPv2 OK");
				        if (currentConfig.enterpriseConfig.getPhase2Method()==Phase2.GTC) eduroamCAT.debug("Set to GTC OK");
				        if (currentConfig.enterpriseConfig.getPhase2Method()==Phase2.PAP) eduroamCAT.debug("Set to GTC PAP");
						eduroamCAT.debug("ID:"+currentConfig.enterpriseConfig.getIdentity());
						//eduroamCAT.debug("Dump="+currentConfig.toString());
						if (currentConfig.enterpriseConfig.getCaCertificate()!=null)
							eduroamCAT.debug("CA cert="+currentConfig.enterpriseConfig.getCaCertificate().toString());
						else eduroamCAT.debug("No CA cert found");
					}
			}
			return "dD";
		}
		else
		{
			return "WiFi Disabled!";
		}
	}
	
	public String checkEduroam()
	{
		String message="";
		if (wifi.isWifiEnabled()) 
		{
			currentConfigs = wifi.getConfiguredNetworks();
			for (WifiConfiguration currentConfig : currentConfigs) 
			{
				if (currentConfig.SSID.equals("\"eduroam\""))
					{
						message+="<font color=\"green\">Found SSID:"+currentConfig.SSID;
						if (currentConfig.allowedGroupCiphers.toString().contains(",")) message+=" with mixed mode";
						else if (currentConfig.allowedGroupCiphers.toString().contains("2")) message+=" with CCMP";
						else if (currentConfig.allowedGroupCiphers.toString().contains("2")) message+=" with TKIP";
						message+="</font> <br/>";
						//message+="<h2>EAP Settings</h2>";
						if (currentConfig.enterpriseConfig.getAnonymousIdentity().length()>0) message+="Anon ID:<font color=\"green\">"+currentConfig.enterpriseConfig.getAnonymousIdentity()+"</font>";
						else message+="<font color=\"blue\">Anon ID missing (optional)</font>";
						message+="<br/>User ID:<font color=\"green\">"+currentConfig.enterpriseConfig.getIdentity()+"</font>";
						message+="<br/>EAP Method:";
				        if (currentConfig.enterpriseConfig.getEapMethod()==Eap.PEAP) message+="<font color=\"green\">PEAP</font> with ";
				        else if (currentConfig.enterpriseConfig.getEapMethod()==Eap.PWD) message+="<font color=\"green\">PWD</font> ";
				        else if (currentConfig.enterpriseConfig.getEapMethod()==Eap.TLS) message+="<font color=\"green\">TLS</font>";
				        else if (currentConfig.enterpriseConfig.getEapMethod()==Eap.TTLS) message+="<font color=\"green\">TTLS</font> with ";
				        else message+="<font color=\"red\">EAP Method error</font>";
				        if (currentConfig.enterpriseConfig.getEapMethod()!=Eap.TLS)
				        {
				        if (currentConfig.enterpriseConfig.getPhase2Method()==Phase2.MSCHAPV2) message+="Phase2:<font color=\"green\">MSCHAPv2</font>";
				        else if (currentConfig.enterpriseConfig.getPhase2Method()==Phase2.GTC) message+="Phase2:<font color=\"green\">GTC</font>";
				        else if (currentConfig.enterpriseConfig.getPhase2Method()==Phase2.PAP) message+="Phase2:<font color=\"green\">PAP</font>";
                        else if (currentConfig.enterpriseConfig.getEapMethod()==Eap.PWD) message+="";
                        else if (currentConfig.enterpriseConfig.getEapMethod()==Eap.TLS) message+="";
				        else message+="<font color=\"red\">Phase2 Missing</font>";
				        }
				        eduroamCAT.debug("CA="+currentConfig.enterpriseConfig.getCaCertificate());
						//if (currentConfig.enterpriseConfig.getCaCertificate()!=null)
						//	message+="<br/><font color=\"green\">CA cert="+currentConfig.enterpriseConfig.getCaCertificate().toString()+"</font>";
						//else message+="<br/><font color=\"red\">No CA certificate found</font>";
				        int nullpoint=0;
				        //eduroamCAT.debug(currentConfig.enterpriseConfig.toString());
				        nullpoint=currentConfig.enterpriseConfig.toString().indexOf("ca_cert NULL");
				        //eduroamCAT.debug("nullpoint="+nullpoint);
				        if (nullpoint==-1){
				        	int start,finish=0;
				        	start=currentConfig.enterpriseConfig.toString().indexOf("ca_cert");
				        	finish=currentConfig.enterpriseConfig.toString().indexOf("\"", start+10);
				        	if (start>0 && finish>0) message+="<br/><font color=\"green\">CA Certificate OK";
				        }
				        else
				        {
							if (currentConfig.enterpriseConfig.getEapMethod()==Eap.PWD) message+="<font color=\"black\">CA Certificate unrequired</font>";
							else message+="<br/><font color=\"red\">No CA certificate found</font>";
				        }
				        if (currentConfig.enterpriseConfig.getSubjectMatch()!=null)
				        {
				        	if (currentConfig.enterpriseConfig.getSubjectMatch().length()>0) message+="<br/>Server Subject Match=<font color=\"green\">"+currentConfig.enterpriseConfig.getSubjectMatch()+"</font>";
							else message+="<br/><font color=\"red\">Server Subject Match missing</font>";
				        }
				        else message+="<br/><font color=\"red\">Server Subject Match missing</font>";
					}
			}
		}
		return message;
	}
	
	public String checkEduroamSSID()
	{
		String message="";
		if (wifi.isWifiEnabled()) 
		{
			currentConfigs = wifi.getConfiguredNetworks();
			if (currentConfigs!=null)
			for (WifiConfiguration currentConfig : currentConfigs) 
			{
				if (currentConfig.SSID != null && !currentConfig.SSID.isEmpty())
				if (currentConfig.SSID.equals("\"eduroam\""))
					{
						message+="<font color=\"black\">Found SSID "+currentConfig.SSID;
						if (currentConfig.allowedGroupCiphers.toString().contains(",")) message+=" with mixed mode";
						else if (currentConfig.allowedGroupCiphers.toString().contains("2")) message+=" with CCMP";
						else if (currentConfig.allowedGroupCiphers.toString().contains("2")) message+=" with TKIP";
						message+="</font>";
					}
			}
		}
		return message;
	}
	
	public String checkEduroamAnon()
	{
		String message="";
		if (wifi.isWifiEnabled()) 
		{
			currentConfigs = wifi.getConfiguredNetworks();
			if (currentConfigs != null)
			for (WifiConfiguration currentConfig : currentConfigs) 
			{
				if (currentConfig.SSID.equals("\"eduroam\""))
					{
						if (currentConfig.enterpriseConfig.getAnonymousIdentity().length()>0) message+="Anon ID=<font color=\"black\">"+currentConfig.enterpriseConfig.getAnonymousIdentity()+"</font>";
						else if (currentConfig.enterpriseConfig.getEapMethod()==Eap.PWD) message+="<font color=\"black\">Anon ID unrequired</font>";
						else message+="<font color=\"#000000\">Anon ID missing (optional)</font>";
					}
			}
		}
		return message;
	}
	
	public String checkEduroamUser()
	{
		String message="";
		if (wifi.isWifiEnabled()) 
		{
			currentConfigs = wifi.getConfiguredNetworks();
			if (currentConfigs!=null)
			for (WifiConfiguration currentConfig : currentConfigs) 
			{
				if (currentConfig.SSID.equals("\"eduroam\""))
					{
					if (currentConfig.enterpriseConfig.getIdentity().length()>0)
						message+="User ID=<font color=\"black\">"+currentConfig.enterpriseConfig.getIdentity()+"</font>";
					else message+="User ID <font color=\"#000001\">MISSING</font>";
					}
			}
		}
		return message;
	}
	
	public String checkEduroamEAP()
	{
		String message="";
		if (wifi.isWifiEnabled()) 
		{
			currentConfigs = wifi.getConfiguredNetworks();
			for (WifiConfiguration currentConfig : currentConfigs) 
			{
				if (currentConfig.SSID.equals("\"eduroam\""))
				{
					message+="EAP Method=";
					if (currentConfig.enterpriseConfig.getEapMethod()==Eap.PEAP) message+="<font color=\"black\">PEAP</font> with ";
					else if (currentConfig.enterpriseConfig.getEapMethod()==Eap.PWD) message+="<font color=\"black\">PWD</font>";
					else if (currentConfig.enterpriseConfig.getEapMethod()==Eap.TLS) message+="<font color=\"black\">TLS</font>";
					else if (currentConfig.enterpriseConfig.getEapMethod()==Eap.TTLS) message+="<font color=\"black\">TTLS</font> with ";
					else message+="<font color=\"#000001\">EAP Method error</font>";
					if (currentConfig.enterpriseConfig.getEapMethod()!=Eap.TLS)
					{
						if (currentConfig.enterpriseConfig.getPhase2Method()==Phase2.MSCHAPV2) message+="Phase2:<font color=\"black\">MSCHAPv2</font>";
						else if (currentConfig.enterpriseConfig.getPhase2Method()==Phase2.GTC) message+="Phase2:<font color=\"black\">GTC</font>";
						else if (currentConfig.enterpriseConfig.getPhase2Method()==Phase2.PAP) message+="Phase2:<font color=\"black\">PAP</font>";
                        else if (currentConfig.enterpriseConfig.getEapMethod()==Eap.PWD) message+="";
                        else if (currentConfig.enterpriseConfig.getEapMethod()==Eap.TLS) message+="";
						else message+="<font color=\"#000001\">Phase2 Missing</font>";
					}
				}
			}
		}
		return message;
	}
	
	public String checkEduroamCA()
	{
		String message="";
		if (wifi.isWifiEnabled()) 
		{
			currentConfigs = wifi.getConfiguredNetworks();
			for (WifiConfiguration currentConfig : currentConfigs) 
			{
				if (currentConfig.SSID.equals("\"eduroam\""))
				{
					//BUG with getCACertificate
					//eduroamCAT.debug("CA="+currentConfig.enterpriseConfig.getCaCertificate().toString());
					//eduroamCAT.debug("tostring="+currentConfig.enterpriseConfig.toString());
					int nullpoint=0;
					nullpoint=currentConfig.enterpriseConfig.toString().indexOf("ca_cert NULL");
					//eduroamCAT.debug("nullpoint"+nullpoint);
					if (nullpoint==-1){
						//int start,finish=0;
						//start=currentConfig.enterpriseConfig.toString().indexOf("ca_cert");
						//finish=currentConfig.enterpriseConfig.toString().indexOf("\"", start+10);
						//if (start>0 && finish>0)
						message+="<font color=\"black\">CA Certificate OK</font>";
					}
					else
					{
						message+="<font color=\"#000001\">No CA certificate found</font>";
					}
					if (message.length()<1)
					{
						if (currentConfig.enterpriseConfig.getEapMethod()==Eap.PWD) message+="<font color=\"black\">CA Certificate unrequired</font>";
						else message+="<font color=\"#000001\">No CA certificate found</font>";
					}
				}
			}
		}
		if (message.length()<1) message+="<font color=\"#000001\">No CA certificate found</font>";
		return message;
	}	
	
	public String checkEduroamSubject()
	{
		String message="";
		if (wifi.isWifiEnabled()) 
		{
			currentConfigs = wifi.getConfiguredNetworks();
			for (WifiConfiguration currentConfig : currentConfigs) 
			{
				if (currentConfig.SSID.equals("\"eduroam\""))
				{
					if (currentConfig.enterpriseConfig.getSubjectMatch()!=null)
					{
						if (currentConfig.enterpriseConfig.getSubjectMatch().length()>0) message+="Server Subject Match=<font color=\"black\">"+currentConfig.enterpriseConfig.getSubjectMatch()+"</font>";
						else message+="<font color=\"#000001\">Server Subject Match missing</font>";
					}
					else message+="<font color=\"#000001\">Server Subject Match missing</font>";
				}
			}
		}
		return message;
	}	
	
    public boolean deleteProfile(String sSID, boolean override)
    {
		boolean removed = false;
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion < 23 || override) {
			List<WifiConfiguration> currentConfigs;
			currentConfigs = wifi.getConfiguredNetworks();
			if (currentConfigs != null)
				if (currentConfigs.size() > 0) {
					debug("Found " + currentConfigs.size() + " ssid profiles on device");
					for (WifiConfiguration currentConfig : currentConfigs) {
						if (currentConfig.SSID.contains(sSID)) {
							removed = wifi.removeNetwork(currentConfig.networkId);
							debug("Removed a profile for " + sSID);
						}
					}
				}
			return removed;
		}
		else
		{
			return false;
		}
    }
	
}
