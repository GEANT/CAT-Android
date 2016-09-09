//*******************
//Author: Gareth Ayres <g.j.ayres@swansea.ac.uk>
//SENSE Project
//https://github.com/GEANT/CAT-Android
//*******************
package uk.ac.swansea.eduroamcat;

public class WiFiProfile {
	
	private boolean error=true;
	private String errorMessage="No Profile";
	private String ssid="";
	private String authType;
//	private String groupCypher;
//	private String keyManagement;
//	private String cypherType;
	private String encType;
	private int ssidPriority=999;
	private boolean autoJoin = true;

	
	//set error message
	public void setConfigError(String errorMessage)
	{
		this.error=true;
		this.errorMessage=errorMessage;
	}
	
	//Return if error or not
	public boolean hasError()
	{
		return this.error;
	}
	
	//Set no error
	public void isOK()
	{
		this.error=false;
	}
	
	//return error message if message exists
	public String getError()
	{
		if (errorMessage.length()>0) return errorMessage;
		else return "";
	}
	
	//set EAPIdP ID string
	public void setSSID(String ssidx)
	{
		if (ssidx.length()>0){
			this.ssid=ssidx;
		}
		else setConfigError("Error with SSID length:="+ssidx);
	}
	
	//return EAPIdP ID String
	public String getSSID()
	{
		return ssid;
	}
	
	//set Auth Type
	public void setAuthType(String auth)
	{
		if  (auth.length()>0)
		{
			this.authType=auth;
		}
		else setConfigError("Error with auth length:="+auth);
	}
	
	//get auth
	public String getAuthType()
	{
		return authType;
	}
	
	//set Encryption Type
	public void setEncryptionType(String enc)
	{
		if  (enc.length()>0)
		{
			this.encType=enc;
		}
		else setConfigError("Error with encryption type length:="+enc);
	}
	
	//get Encryption Type
	public String getEncryptionType()
	{
		return encType;
	}
	
	//set SSID Priority
	public void setSSIDPriority(int priority)
	{
		if  (priority>-1)
		{
			this.ssidPriority=priority;
		}
		else setConfigError("Error with ssid priority:="+priority);
	}
	
	//get ssid priority
	public int getSSIDPriority()
	{
		return ssidPriority;
	}
	
	//set SSID auto Join
	public void setAutojoin(boolean join)
	{
		this.autoJoin=join;
	}
	
	//get ssid autojoin
	public boolean isAutoJoin()
	{
		return autoJoin;
	}	

}