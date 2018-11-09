//*******************
//Author: Gareth Ayres <g.j.ayres@swansea.ac.uk>
//SENSE Project
//https://github.com/GEANT/CAT-Android
//*******************
package uk.ac.swansea.eduroamcat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import org.json.JSONArray;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.Toast;

//Supplicant Configuration Discovery Process
public class SCAD  extends AsyncTask<String, Integer, String> {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 0;
    public static float MAX_DISTANCE = 30000;
	String locationProvider = LocationManager.NETWORK_PROVIDER;
	LocationManager locationManager;
	public double lat,longx=0;
	JSONObject json = null;
	String jsonString = "";
	static public ArrayList <IdP> IdPs = new ArrayList<IdP>();
	Location lastKnownLocation;
	Boolean hasAccuracy=false;
	String lang= Locale.getDefault().getLanguage();
    LocationListener locationListener;
    boolean network_enabled = false;
	Activity activity;
	GEOIP geoip;
	String search="";

	public SCAD(Activity activity,String search)
	{
		//set location
		this.activity=activity;
		this.search=search;
		//check for location permission as result of new tark sdk above sdk 23
        if (ContextCompat.checkSelfPermission(this.activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
			eduroamCAT.debug("No Location permissions");
            // Permission is not granted
            ActivityCompat.requestPermissions(this.activity,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

            //try geoip...
			//geoip = new GEOIP(activity);
			//geoip.execute();
			startLocationService();
			hasAccuracy = false;
			eduroamCAT.debug("Location Service permission not grantted, trying GEOIP....");
        }
        else {
			startLocationService();
        }
	}

	public void startLocationService()
	{
		if (ContextCompat.checkSelfPermission(this.activity, Manifest.permission.ACCESS_COARSE_LOCATION)
				== PackageManager.PERMISSION_GRANTED) {
			eduroamCAT.debug("Location Service setup....");
			locationManager = (LocationManager) activity.getSystemService(activity.LOCATION_SERVICE);
			// Define a listener that responds to location updates

			locationListener = new LocationListener() {
				public void onLocationChanged(Location location) {
					// Called when a new location is found by the network location provider.
				}

				public void onStatusChanged(String provider, int status, Bundle extras) {
				}

				public void onProviderEnabled(String provider) {
                    hasAccuracy = true;
				}

				public void onProviderDisabled(String provider) {
                    hasAccuracy = false;
				}
			};

			if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)) {
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener); }
				else {
                eduroamCAT.debug("No Network_Provider...");
                hasAccuracy = false;
            }

			try {
				network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			} catch (Exception ex) {
				eduroamCAT.debug("Location Service Disabled....");
                hasAccuracy = false;
			}

			ConfigureFragment.setupSCAD();
			//locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
			if (search.length() < 3) {
				lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
				if (lastKnownLocation != null) {
					lat = lastKnownLocation.getLatitude();
					longx = lastKnownLocation.getLongitude();
					eduroamCAT.debug("Last Known Location=" + lat + "," + longx);
					eduroamCAT.debug("Last Known Location Accuracy=" + lastKnownLocation.getAccuracy());
					hasAccuracy = lastKnownLocation.hasAccuracy();
				} else {
					geoip = new GEOIP(activity);
					geoip.execute();
					//lat = 0;
					//longx = 0;
					hasAccuracy = false;
					eduroamCAT.debug("Location Service failed, trying GEOIP....");
				}
			}

			if (search.length() > 2) {
				lat = 0;
				longx = 0;
				MAX_DISTANCE = 999999999;
			}
		}
		geoip = new GEOIP(activity);
		geoip.execute();
		hasAccuracy = false;
		eduroamCAT.debug("Location Service no permitted, trying GEOIP....");
	}

	public void setLocation(Double latx, Double longx)
	{
		this.lat=latx;
		this.longx=longx;
	}

	private void addIdP(Double latnow, Double lonnow, String title, int id, float [] distance)
	{
	    if (distance[0]>0 && id>0 && title.length()>0 && distance[0]<MAX_DISTANCE)
    	{
			if (isIdPUniuque(id))
			{
				IdP aidp = new IdP(title,id,distance[0]);
				if (search.length()<3) {
					if (ViewProfiles.adapter.getCount()<20) {
						ViewProfiles.adapter.add(aidp);
						aidp.execute();
					}
				}
				else
				{
					if (aidp.title.toLowerCase().contains(search.toLowerCase()))
					{
						if (ViewProfiles.adapter.getCount()<20) {
							ViewProfiles.adapter.add(aidp);
							aidp.execute();
						}
					}
				}
			}
    	}
	}

	private boolean isIdPUniuque(int id)
	{
		for (IdP ipd : IdPs)
		{
			if (ipd.id==id) return false;
		}
		return true;
	}
	
	@Override
	protected void onPostExecute(String result) {
        super.onPostExecute(result);
        //eduroamCAT.debug("SACD RESULT="+result);
        if (result.length()>0 && geoip !=null)
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
        //eduroamCAT.debug("Number of IdPs="+IdPs.size());
        if (IdPs.size()<1) {
			IdP tmpidp = new IdP(activity.getString(R.string.manual_search_fail),0,0);
			tmpidp.profileRedirect="0";
			ViewProfiles.adapter.add(tmpidp);
			String locationServiceCheck="";
			if (!hasAccuracy) locationServiceCheck="<font color=\"red\">"+activity.getString(R.string.scad_geoip_insufficient)+"</font>";
            if (!network_enabled) locationServiceCheck="<font color=\"red\">"+activity.getString(R.string.scad_geoip_noloc)+"</font>";
        	String idp_nearby = "<h1>"+activity.getString(R.string.scad_geoip_no_configs_title)+"</h1>";
			if (search.length()==0) {
				int maxdistance = 0;
				maxdistance = (int) MAX_DISTANCE / 1000;
				idp_nearby += activity.getString(R.string.scad_geoip_no_configs_message, maxdistance);
			}
			idp_nearby+="<br/>"+locationServiceCheck;
			Spanned idp_nearby2=Html.fromHtml(idp_nearby);
	        if (ConfigureFragment.idptext!=null) ConfigureFragment.idptext.setText(idp_nearby2);
	        if (ConfigureFragment.scadProgress!=null) ConfigureFragment.scadProgress.setVisibility(View.VISIBLE);
        }
		else
		{
			String loadingProfiles = activity.getString(R.string.scad_geoip_success);
			Spanned idp_nearby = Html.fromHtml("<h1>"+activity.getString(R.string.scad_title)+"</h1>" + loadingProfiles);
			if (ConfigureFragment.idptext!=null) ConfigureFragment.idptext.setText(idp_nearby);
			if (ConfigureFragment.scadProgress!=null) ConfigureFragment.scadProgress.setVisibility(View.VISIBLE);
		}
        //stop location service
        if (locationManager !=null) locationManager.removeUpdates(locationListener);
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
