package uk.ac.swansea.eduroamcat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.View;

public class IdP extends AsyncTask<String, Integer, String> {
	
	public String title;
	public float distance;
	public int id;
	public List <Integer> profileID = new ArrayList<Integer>();
	public List <String> profileDisplay = new ArrayList<String>();
    public List <String> profileRedirected = new ArrayList<String>();
    public String profileRedirect="";
	public String download;
	String jsonString = "";
	
	
	public IdP(String title, int id, float distance)
	{
		this.title=title;
		this.id=id;
		this.distance=distance; 
	}
	
	public int getDistance()
	{
		return Math.round(this.distance/1000);
	}
	
	public void getProfileID()
	{
		//https://cat.eduroam.org/user/API.php?action=listProfiles&id=1
	}
	
    protected void onPreExecute() {
        super.onPreExecute();
        //String loadingProfiles = getString(R.string.loading_profiles);
		//http://stackoverflow.com/questions/11814060/onpostexecute-getresources-getstring-nullpointerexception
		String loadingProfiles = "Loading Profiles...";
        Spanned idp_nearby = Html.fromHtml("<h1>SCAD Discovery...</h1>" + loadingProfiles);
        if (ConfigureFragment.idptext!=null) ConfigureFragment.idptext.setText(idp_nearby);
        if (ConfigureFragment.scadProgress!=null) ConfigureFragment.scadProgress.setVisibility(View.VISIBLE);
    }	
	
	protected void onPostExecute(String result) {
        super.onPostExecute(result);
        //eduroamCAT.debug("RESULT="+result);
        try {
        	JSONObject allJSON = new JSONObject(result);
				if (allJSON.has("data")) {
					eduroamCAT.debug("adding download link...");
					JSONArray item = allJSON.getJSONArray("data");
					//eduroamCAT.debug("data="+item.toString());
					if (item.length()>1) {
						for (int j=0; j<item.length(); j++)
						{
							JSONObject iditem = (JSONObject) item.get(j);
							profileID.add(iditem.getInt("id"));
                            //get attributes for profile
                            ProfileAttributes attributes = new ProfileAttributes(profileID.get(profileID.size()-1),this);
                            attributes.execute();
							profileDisplay.add(iditem.getString("display"));
						}
					}
					else {
						JSONObject iditem = (JSONObject) item.get(0);
						profileID.add(iditem.getInt("id"));
                        //get attributes for profile
                        ProfileAttributes attributes = new ProfileAttributes(profileID.get(profileID.size()-1),this);
                        attributes.execute();
						profileDisplay.add(iditem.getString("display"));
					}
				}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//update configure fragment text
		//Resources.getSystem().getString(R.string.nearby)
        updateDisplay();
	}

    public void updateDisplay()
    {
        String idps="<h1>Nearby Configs</h1>"+ConfigureFragment.scad.getAllProviders();
        Spanned idp_nearby = Html.fromHtml(idps);
        ConfigureFragment.idptext.setText(idp_nearby);
        ConfigureFragment.idptext.setMovementMethod(new ScrollingMovementMethod());
        ConfigureFragment.idptext.setMovementMethod(LinkMovementMethod.getInstance());
        if (ConfigureFragment.scadProgress!=null) ConfigureFragment.scadProgress.setVisibility(View.GONE);
    }
	
	@Override
	protected String doInBackground(String... params) {
		String str = "";
		HttpResponse response;
		HttpClient myClient = new DefaultHttpClient();
		String lang="";
		lang = Locale.getDefault().getLanguage();
		HttpPost myConnection = new HttpPost("https://cat.eduroam.org/user/API.php?action=listProfiles&id="+id+"&lang="+lang);
		try {
			eduroamCAT.debug("Getting list of profiles from API for "+id);
			response = myClient.execute(myConnection);
			str = EntityUtils.toString(response.getEntity(), "UTF-8");
			//eduroamCAT.debug("Content="+str);
		} catch (Exception e) {
			eduroamCAT.debug(e.getMessage());
		}
		this.jsonString=str;
		return str;
	}

}
