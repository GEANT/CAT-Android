//*******************
//Author: Gareth Ayres <g.j.ayres@swansea.ac.uk>
//SENSE Project
//https://github.com/GEANT/CAT-Android
//*******************
package uk.ac.swansea.eduroamcat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class WifiStatus extends BroadcastReceiver
{
	NetworkInfo wifiStatus;
	NetworkInfo.State status;
	NetworkInfo.DetailedState advancedStatus;
	NetworkInfo other;
	ConnectivityManager connectivity;
	private WifiManager wifim;
	WifiInfo info;
	public SupplicantState state;
	Boolean installedOK;

	public WifiStatus(Object connect)
	{
		//empty vars
		eduroamCAT.debug("WifiStatus created");
		connectivity = (ConnectivityManager) connect;
		wifim = eduroamCAT.getWifiManager();
	}
	
	//monitor wifi state change broadcasts 
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		String str = intent.getAction();
		wifiStatus = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		status = wifiStatus.getState();
		//eduroamCAT.debug("Broadcast=" + status.toString());

		if (ConnectFragment.getProfileInstalled() == Boolean.TRUE)
		if (status.toString().equals("DISCONNECTED"))
		{
			ConnectFragment.setStatus(status.toString()+" from SSID "+wifim.getConnectionInfo().getSSID());
		}
		else {
			String tempSSID = wifim.getConnectionInfo().getSSID();
			if (wifim != null) {
				tempSSID = tempSSID.replace("\"", "");
				if (tempSSID.equals(eduroamCAT.appSSID) && ConnectFragment.getProfileInstalled()) {
					eduroamCAT.debug("************************Installed and CONNECTED");
					Activity activity = (Activity) context;
					eduroamCAT.notifyConnected(activity, context);
				}
				if (status.toString() != null) ConnectFragment.setStatus(status.toString() + " to SSID " + wifim.getConnectionInfo().getSSID());
			}
		}
		eduroamCAT.setSummary();

		advancedStatus = wifiStatus.getDetailedState();
	    info = wifim.getConnectionInfo();
		state = info.getSupplicantState();
		NetworkInfo.DetailedState detailed = WifiInfo.getDetailedStateOf(state);
		eduroamCAT.setState(state.toString());
		if (StatusFragment.verbose)
		{
			StatusFragment.setDebug("DEBUG: "+advancedStatus.toString());
			StatusFragment.setDebug("DEBUG Supp: "+state.toString());
			StatusFragment.setDebug("DEBUG Supp(Extra): "+detailed.toString());	
		}
		if (wifiStatus.isConnected() && StatusFragment.verbose)
		{
			StatusFragment.setDebug("Wifi Connected to:"+wifim.getConnectionInfo().getSSID());
			StatusFragment.setDebug("DEBUG: Connected:"+wifim.getConnectionInfo().toString());
			//StatusFragment.setDebug("DEBUG: DHCP=:"+wifim.getDhcpInfo().toString());
			//StatusFragment.setDebug("DEBUG: DHCP:"+wifim.getConnectionInfo().getIpAddress());
			StatusFragment.setDebug("*********");
			}
		if (wifiStatus.isConnected() && !StatusFragment.verbose)
		{
			//StatusFragment.setDebug("Wifi Connected to:"+wifim.getConnectionInfo().getSSID());
		}
		//eduroamCAT.debug("detailed="+detailed.toString());
		//eduroamCAT.debug("state="+state.toString());
		if (detailed.toString().contains("SCANNING") || detailed.toString().contains("CONNECTING"))
			ConnectFragment.setStatus("Trying SSID "+wifim.getConnectionInfo().getSSID());

	}

}
