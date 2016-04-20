package uk.ac.swansea.eduroamcat;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


import org.json.JSONArray;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;

//Supplicant Configuration Discovery Process
public class SCAD  extends AsyncTask<String, Integer, String> {
	
	public static final float MAX_DISTANCE = 30000;
	String locationProvider = LocationManager.NETWORK_PROVIDER;
	LocationManager locationManager;
	public double lat,longx=0;
	JSONObject json = null;
	String jsonString = "";
	static public ArrayList <IdP> IdPs = new ArrayList<IdP>();
	Location lastKnownLocation;
	Boolean hasAccuracy=true;
	String lang= Locale.getDefault().getLanguage();
    LocationListener locationListener;
    boolean network_enabled = false;
	Activity activity;
	GEOIP geoip;
		
	public SCAD(Activity activity)
	{
		//set location
		this.activity=activity;
		eduroamCAT.debug("Location Service setup....");
		locationManager = (LocationManager) activity.getSystemService(activity.LOCATION_SERVICE);
        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };
		if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER))
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {
			eduroamCAT.debug("Location Service Disabled....");
		}

		ConfigureFragment.setupSCAD();
		//locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
		lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
		if (lastKnownLocation!=null) {
            lat = lastKnownLocation.getLatitude();
            longx = lastKnownLocation.getLongitude();
            eduroamCAT.debug("Last Known Location=" + lat + "," + longx);
            eduroamCAT.debug("Last Known Location Accuracy=" + lastKnownLocation.getAccuracy());
			hasAccuracy=lastKnownLocation.hasAccuracy();
        }
        else {
			geoip = new GEOIP(activity);
			geoip.execute();
			lat = 0;
			longx = 0;
			hasAccuracy = false;
			eduroamCAT.debug("Location Service failed....");
		}
	}
	
	public void stopLocationLookups()
	{
		//locationManager.removeUpdates(locationListener);
	}

	public void setLocation(Double latx, Double longx)
	{
		this.lat=latx;
		this.longx=longx;
	}

//	public String getAllProviders()
//	{
//		String result="";
//		eduroamCAT.debug("size on get all=" + IdPs.size());
//
//		//sort profiles by distance
//		Collections.sort(IdPs, new Comparator<IdP>(){
//		    public int compare(IdP s1, IdP s2) {
//		        return (int) ((int) s1.distance - s2.distance);
//		    }
//		});
//
//        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
//        String androidID="";
//        if (currentapiVersion == 23) androidID="android_marshmallow";
//        else if (currentapiVersion == 22) androidID="android_lollipop";
//        else if (currentapiVersion == 21) androidID="android_lollipop";
//        else if (currentapiVersion == 20) androidID="android_kitkat";
//        else if (currentapiVersion == 19) androidID="android_kitkat";
//        else if (currentapiVersion == 18) androidID="android_43";
//        else androidID="android_legacy";
//
//        if (!IdPs.isEmpty())
//		for (int h=0; h<IdPs.size(); h++)
//		{
//            IdP temp = IdPs.get(h);
//            if (temp.distance<MAX_DISTANCE) {
//                result += "<b>" + temp.title + "</b><br/>";
//                float distance1 = temp.getDistance();
//                result += "Distance Away=<b>" + distance1 + "Km</b><br/>";
//                if (temp.profileID.size()>0)
//                {
//                	//for size of profile
//                	for (int p=0; p<temp.profileID.size(); p++)
//            		{
//                    	String temp_display = temp.profileDisplay.get(p);
//                    	if (temp_display.length()<2) temp_display = "eduroam";
//						//test if redirect in place on profile.
//                        String redirect="";
//                        if (p<=temp.profileRedirected.size()-1 && !temp.profileRedirect.isEmpty()) {
//                            eduroamCAT.debug("MASTER REDIRECTED for =" + temp.profileID.get(p) + " " + temp.profileRedirected.get(p));
//                            redirect =temp.profileRedirected.get(p);
//                        }
//                        if (redirect.length()>1) result+="<a href=\""+temp.profileRedirect+"\">"+temp_display + " : Click Here to Download</a><br/><br/>";
//                        else result += "<a href=\"https://cat.eduroam.org/user/API.php?action=downloadInstaller&id="+androidID+"&profile="+temp.profileID.get(p)+"&lang="+lang+"\">" +
//                		temp_display + " : Click Here to Download</a><br/><br/>";
//						//eduroamCAT.debug("result="+result);
//            		}
//                }
//                else
//                	result+="Getting profile link from cat.eduroam.org...<br/><br/>";
//            }
//		}
//		 if (IdPs.isEmpty()) {
//			 result+="<h1>No configs found within "+MAX_DISTANCE / 1000 +"KMs</h1>";
//		     ConfigureFragment.removeSCAD();
//		 }
//		 return result;
//	}

	
	public List getNearbyInstitutions()
	{
		List list10 = null;
		return list10;
	}
	
	public boolean testCATwebsite()
	{
		
		return false;
	}
	
//	@Override
//    protected void onPreExecute() {
//        super.onPreExecute();
//        Spanned idp_nearby = Html.fromHtml("<h1>SCAD Discovery...</h1>");
//        if (ConfigureFragment.idptext!=null) ConfigureFragment.idptext.setText(idp_nearby);
//        if (ConfigureFragment.scadProgress!=null) ConfigureFragment.scadProgress.setVisibility(View.VISIBLE);
//    }
	
	private void addIdP(Double latnow, Double lonnow, String title, int id, float [] distance)
	{
	    if (distance[0]>0 && id>0 && title.length()>0 && distance[0]<MAX_DISTANCE)
    	{
    		IdP aidp = new IdP(title,id,distance[0]);
			ViewProfiles.adapter.add(aidp);
			aidp.execute();
    	}
	}
	
	@Override
	protected void onPostExecute(String result) {
        super.onPostExecute(result);
        //eduroamCAT.debug("RESULT="+result);
        if (result.length()>0)
        try {
			if (hasAccuracy==false && geoip.hasLocation()) {
				lat = geoip.getLatitude();
				longx = geoip.getLongitude();
			}
        	if (ConfigureFragment.idptext!=null) ConfigureFragment.idptext.setVisibility(View.VISIBLE);
        	if (ConfigureFragment.scadProgress!=null) ConfigureFragment.scadProgress.setVisibility(View.GONE);
			JSONArray allJSON = new JSONArray(result);
			eduroamCAT.debug("JSON Length"+allJSON.length());
			//JSONArray jArr = jsonObj.getJSONArray("geo");
				for (int i=0; i < allJSON.length(); i++) {
					String title="";
					double latnow=0;
					double lonnow=0;
					int id=0;
					float [] distance = new float[1];
				    JSONObject item = allJSON.getJSONObject(i);
				    if (item.has("geo")) {
				    title=item.getString("title");
				    id = item.getInt("id");
				    if (item.optJSONArray("geo") != null) {
				    	//multiple lat/long pairs
				    	JSONArray geo = item.optJSONArray("geo");
				    	for (int j=0; j < geo.length(); j++)
				    	{
					    	JSONObject geo_multi = geo.getJSONObject(j);
					    	latnow=geo_multi.getDouble("lat");
					    	lonnow=geo_multi.getDouble("lon");
					    	Location.distanceBetween(lat, longx, latnow, lonnow, distance);
							addIdP(latnow, lonnow, title, id, distance);
				    	}

				    }
				    else {
				    	//one lat/long pair
				    	JSONObject geo = item.getJSONObject("geo");
				    	latnow=geo.getDouble("lat");
				    	lonnow=geo.getDouble("lon");
				    	Location.distanceBetween(lat, longx, latnow, lonnow, distance);
				    	addIdP(latnow, lonnow, title, id, distance);
				    } 
				    }
				}
		} catch (JSONException e) {
			e.printStackTrace();
			//eduroamCAT.alertUser("Config Discovery Failed","SCAD Failure",activity);
		}
		//sort
		Collections.sort(IdPs, new Comparator<IdP>(){
			public int compare(IdP s1, IdP s2) {
				return (int) ((int) s1.distance - s2.distance);
			}
		});
        eduroamCAT.debug("Number of IdPs="+IdPs.size());
        if (IdPs.size()<1) {
			String locationServiceCheck="";
			if (!hasAccuracy) locationServiceCheck="<font color=\"red\">Insufficient location accuracy.</font>";
            if (!network_enabled) locationServiceCheck="<font color=\"red\">No location service active. Please turn on Location Services and restart the app for auto discovery to work.</font>";
        	Spanned idp_nearby = Html.fromHtml("<h1>No Configs Nearby</h1>No configs were automatically discovered within "+MAX_DISTANCE / 1000 +"KMs.<br/>"+locationServiceCheck);
	        if (ConfigureFragment.idptext!=null) ConfigureFragment.idptext.setText(idp_nearby);
	        if (ConfigureFragment.scadProgress!=null) ConfigureFragment.scadProgress.setVisibility(View.VISIBLE);
        }
        //stop location service
        locationManager.removeUpdates(locationListener);
		ViewProfiles.adapter.notifyDataSetChanged();
    }
	
	@Override
	protected String doInBackground(String... aurl) {
		String str = "";
		HttpResponse response;
		HttpClient myClient = new DefaultHttpClient();
		HttpPost myConnection = new HttpPost("https://cat.eduroam.org/user/API.php?action=listAllIdentityProviders&lang="+lang);
		try {
			eduroamCAT.debug("Getting list of all providers from API");
			response = myClient.execute(myConnection);
			//eduroamCAT.debug("Response="+response.getStatusLine());
			str = EntityUtils.toString(response.getEntity(), "UTF-8");
			//eduroamCAT.debug("Content="+str);
		} catch (Exception e) {
			eduroamCAT.debug(e.getMessage());
			return "Can not contact cat.eduroam.org";
		}
		this.jsonString=str;
		return str;
	}
}
