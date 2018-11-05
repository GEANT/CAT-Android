//*******************
//Author: Gareth Ayres <g.j.ayres@swansea.ac.uk>
//SENSE Project
//https://github.com/GEANT/CAT-Android
//*******************
package uk.ac.swansea.eduroamcat;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import android.content.res.Resources;
import android.location.Location;
import android.util.Base64;
import android.webkit.URLUtil;

public class ConfigProfile {
	
	private String EAPIdP_ID="";
	private boolean error=false;
	private String errorMessage="";
	private ArrayList<String> displayName=new ArrayList<String>();
	private ArrayList<String> displayNameLang=new ArrayList<String>();
	private ArrayList<String> description=new ArrayList<String>();
	private Location location = new Location("");
	private byte[] logo;
	private ArrayList<String> terms=new ArrayList<String>(); 
	private ArrayList<String> emails=new ArrayList<String>();
	private ArrayList<String> phone=new ArrayList<String>();
	private ArrayList<URL> web=new ArrayList<URL>();
	private ArrayList<AuthenticationMethod> authenticationMethods=new ArrayList<AuthenticationMethod>();
	private boolean hasHelpdeskInfo=false;
	
	//set error message
	public void setConfigError(String errorMessage)
	{
		this.error=true;
		this.errorMessage=errorMessage;
	}

	//remove error
	public void clearConfigError()
	{
		this.error=false;
		this.errorMessage="";
	}
	
	//Return if error or not
	public boolean isError()
	{
		return this.error;
	}
	
	//return error message if message exists
	public String getError()
	{
		if (errorMessage.length()>0) return errorMessage;
		else return "";
	}
	
	//set EAPIdP ID string
	public void setEAPIdP_ID(String ID)
	{
		if (ID.length()>0){
			this.EAPIdP_ID=ID;
		}
		else setConfigError(Resources.getSystem().getString(R.string.error_with)+"EAPIdp_ID length:ID="+ID);
	}
	
	//return EAPIdP ID String
	public String getEAPIdP_ID()
	{
		return EAPIdP_ID;
	}
	
	//set display name with lang
	public void setDisplayName(String adisplayName, String aLang)
	{
		if (adisplayName.length()>0 && aLang.length()>0){
			this.displayName.add(adisplayName);
			this.displayNameLang.add(aLang);
		}		
		else 
			{
			if (adisplayName.length()>0){
				this.displayName.add(adisplayName);
				this.displayNameLang.add("en");
			}
			else setConfigError(Resources.getSystem().getString(R.string.error_with)+"Display Name and Lang="+displayName);
			}
	}
	
	
	//return last display Name
	public String getDisplayName()
	{
		if (displayName.size()>0)
		return displayName.get(displayName.size()-1);
		else return "";
	}
	
	//return display Name
	public String getDisplayNameByLang(String aLang)
	{
		int count = displayNameLang.indexOf(aLang);
		return displayName.get(count);
	}
	
	//set description
	public void setDescription(String aDescription,String aLang)
	{
		if (aDescription.length()>0){
			this.description.add(aDescription);
		}
		//else setConfigError("Error with description="+aDescription);
		else this.description.add("");
	}

	//return description
	public String getDescription()
	{
		if (description.size()>0)
		return description.get(description.size()-1);
		else return "";
	}
	
	//set location lat/long
	public void setLocation(double lat, double longx)
	{
		if (lat!=0 && longx!=0)
		{
			this.location.setLatitude(lat);
			this.location.setLongitude(longx);
		}
	}

	//return location
	public Location getLocation()
	{
		return location;
	}
	
    private static byte[] decodeImage(String imageDataString) {
        return Base64.decode(imageDataString,Base64.DEFAULT);
    }
	
	//set byte array of base64 decoded image of logo of provider
	public void setLogo(String image,String mime, String encoding)
	{
		if (mime.equals("image/jpeg") && encoding.equals("base64")) {
			byte[] imageDataBytes = decodeImage(image);
			//imageView1.setImageBitmap( BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
			this.logo=imageDataBytes;
		}
	}
	
	//return logo as byte array
	public byte[] getLogo()
	{
		return logo;
	}
	
	//set terms
	public void setTermsOfUse(String aTerm)
	{
		if (aTerm.length()>0){
			this.terms.add(aTerm);
		}
		//else setConfigError("Error with terms="+aTerm);
	}

	//return terms
	public String getTermsOfUse()
	{
		if (terms.size()>0)
		return terms.get(terms.size()-1);
		else return "";
	}
	
	//set support email
	public void setHelpdeskEmail(String email, String lang)
	{
		if (email.length()>0 && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
			emails.add(email);
		}
		//else setConfigError("Error with helpdesk email="+email);
	}

	//return phone numbers
	public String getSupportEmails()
	{
		if (emails.size()>0) return emails.get(emails.size()-1);
		else return "";
	}
	
	//set phone number
	public void setHelpdeskPhone(String phoneNumber, String lang)
	{
		if (phoneNumber.length()>0){
			phone.add(phoneNumber);
		}
		//else setConfigError("Error with phone number="+phone);
	}

	//return phone numbers
	public String getHelpdeskPhoneNumber(String lang)
	{
		if (phone.size()>0) return phone.get(phone.size()-1);
		else return "";
	}
	
	//return phone numbers
	public String getHelpdeskPhoneNumber()
	{
		if (phone.size()>0) return phone.get(phone.size()-1);
		else return "";
	}
	
	//new AuthenticationMethod
	public int addAuthenticationMethod(AuthenticationMethod newAuthMethod)
	{
		authenticationMethods.add(newAuthMethod);
		return authenticationMethods.indexOf(newAuthMethod);
	}
	
	//set support URL
	public void setHelpdeskURL(String aURL, String lang)
	{
		if (URLUtil.isValidUrl(aURL) && aURL.length()>0){
			URL tmp;
			try {
				tmp = new URL (aURL);
				web.add(tmp);
				tmp=null;
			} catch (MalformedURLException e) {
				setConfigError(Resources.getSystem().getString(R.string.error_with)+"URL format="+aURL);
			}
		}
		//else setConfigError("Error with URL="+aURL);
	}

	//return support URL
	public URL getHelpdeskURL()
	{
		if (web.size()>0) return web.get(web.size()-1);
		else return null;
	}
	
	//get an Authentication Method
	public AuthenticationMethod getAuthenticationMethod(int index)
	{
		eduroamCAT.debug("auth methods list size="+authenticationMethods.size());
		if (authenticationMethods.size()>0) return authenticationMethods.get(index);
		else return null;
	}

	//remove authentication method
	public Boolean removeAuthenticationMethod(int index)
	{
		eduroamCAT.debug("Removing auth method="+index);
		if (authenticationMethods.size()>=index) { authenticationMethods.remove(index); return true; }
		else return false;
	}
	
	//get number of Authentication Methods
	public int getNumberAuthenticationMethods()
	{
		return authenticationMethods.size();
	}
	
	//set helpdesk info
	public void setHelpdeskInfo(boolean has)
	{
		this.hasHelpdeskInfo=has;
	}
	
	//get helpdesk info
	public boolean hasHelpdeskInfo()
	{
		return this.hasHelpdeskInfo;
	}
}
