//*******************
//Author: Gareth Ayres <g.j.ayres@swansea.ac.uk>
//SENSE Project
//https://github.com/GEANT/CAT-Android
//*******************
package uk.ac.swansea.eduroamcat;

import android.app.Activity;
import android.os.AsyncTask;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by AyresGJ on 26/02/2016.
 * GeoIP lookup using https://cat.eduroam.org/user/API.php?lang=pl&action=locateUser
 */
public class GEOIP extends AsyncTask<String, Integer, String> {

    String country="";
    String region="";
    public Double latx,longx;
    private Boolean hasLocation=false;
    Activity activity;


    public GEOIP(Activity activity)
    {
        this.activity=activity;
    }

    //set user facing text to warn GeoIP starting. May incur permissions request.
    protected void onPreExecute() {
        super.onPreExecute();
        eduroamCAT.debug("starting geoip...");
        String loadingProfiles = activity.getString(R.string.scad_geoip_trying);
        Spanned idp_nearby = Html.fromHtml("<h1>"+activity.getString(R.string.scad_geoip_title)+"</h1>" + loadingProfiles);
        if (ConfigureFragment.idptext!=null) ConfigureFragment.idptext.setText(idp_nearby);
        if (ConfigureFragment.scadProgress!=null) ConfigureFragment.scadProgress.setVisibility(View.VISIBLE);
    }

    //deal with result
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        eduroamCAT.debug("GeoIP result:"+result);
        if (result.length() > 0) {
            try {
                if (ConfigureFragment.idptext != null)
                    ConfigureFragment.idptext.setVisibility(View.VISIBLE);
                JSONObject item = new JSONObject(result);
                //eduroamCAT.debug("JSON GEOIP Length " + item.length());
                String country = "";
                country = item.getString("country");
                    if (item.has("geo")) {
                        if (item.optJSONArray("geo") != null) {
                            //multiple lat/long pairs
                            JSONArray geo = item.optJSONArray("geo");
                            for (int j = 0; j < geo.length(); j++) {
                                JSONObject geo_multi = geo.getJSONObject(j);
                                latx = geo_multi.getDouble("lat");
                                longx = geo_multi.getDouble("lon");
                            }
                        } else {
                            //one lat/long pair
                            JSONObject geo = item.getJSONObject("geo");
                            latx = geo.getDouble("lat");
                            longx = geo.getDouble("lon");
                        }
                        //eduroamCAT.debug("GEOIP GOT: " + latx + " and " + longx);
                        hasLocation=true;
                        ViewProfiles.adapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                String error_message = "";
                if (result.length() > 0) error_message = "Error=" + result;
                Spanned idp_nearby = Html.fromHtml("<h1>"+activity.getString(R.string.scad_geoip_failed)+"</h1>" + error_message);
                if (ConfigureFragment.idptext != null)
                    ConfigureFragment.idptext.setText(idp_nearby);
                if (ConfigureFragment.scadProgress != null)
                    ConfigureFragment.scadProgress.setVisibility(View.VISIBLE);
                //eduroamCAT.alertUser("GeoIP Failed","GeoIP Failed",activity);
            }
        }
        else
        {
            eduroamCAT.debug("NO result for GEOIP lookup");
        }

        if (result.length()>0)
        {
            ConfigureFragment.setupSCAD();
            String loadingProfiles = activity.getString(R.string.scad_geoip_success);
            Spanned idp_nearby = Html.fromHtml("<h1>"+activity.getString(R.string.scad_geoip_title)+"</h1>" + loadingProfiles);
            if (ConfigureFragment.idptext!=null) ConfigureFragment.idptext.setText(idp_nearby);
            if (ConfigureFragment.scadProgress!=null) ConfigureFragment.scadProgress.setVisibility(View.VISIBLE);
        }
    }


    @Override
    protected String doInBackground(String... params) {
        String str = "";
        HttpResponse response;
        HttpClient myClient = new DefaultHttpClient();
        String lang="";
        lang = Locale.getDefault().getLanguage();
        HttpPost myConnection = new HttpPost("https://cat.eduroam.org/user/API.php?lang=pl&action=locateUser");
        try {
            eduroamCAT.debug("Getting geoip location from cat.eduroam.org ");
            response = myClient.execute(myConnection);
            str = EntityUtils.toString(response.getEntity(), "UTF-8");
            //eduroamCAT.debug("Content="+str);
        } catch (Exception e) {
            eduroamCAT.debug(e.getMessage());
        }
        return str;
    }

    //set only if location from CAT via GEOIP success
    public boolean hasLocation()
    {
        return hasLocation;
    }

    //get latitude value
    public Double getLatitude()
    {
        return latx;
    }

    //get longitude
    public Double getLongitude()
    {
        return longx;
    }

}
