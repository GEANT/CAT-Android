//*******************
//Author: Gareth Ayres <g.j.ayres@swansea.ac.uk>
//SENSE Project
//https://github.com/GEANT/CAT-Android
//*******************
package uk.ac.swansea.eduroamcat;


import java.util.Locale;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.AsyncTask;


public class ProfileAttributes extends AsyncTask<String, Integer, String> {

    int profileID;
    int profile;
    int status=1;
    String redirect;
    String jsonString = "";
    boolean redirected=false;
    String androidID="";
    IdP aIdP;

    public ProfileAttributes (int profileID,IdP idp)
    {
        this.profileID=profileID;
        this.aIdP=idp;
        //eduroamCAT.debug("profile attributes....");
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion == 26) androidID="android_oreo";
        else if (currentapiVersion == 25) androidID="android_nougat";
        else if (currentapiVersion == 24) androidID="android_marshmallow";
        else if (currentapiVersion == 23) androidID="android_marshmallow";
        else if (currentapiVersion == 22) androidID="android_lollipop";
        else if (currentapiVersion == 21) androidID="android_lollipop";
        else if (currentapiVersion == 20) androidID="android_kitkat";
        else if (currentapiVersion == 19) androidID="android_kitkat";
        else if (currentapiVersion == 18) androidID="android_43";
        else androidID="android_kitkat";
    }

    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        redirect="0";
        try {
            JSONObject allJSON = new JSONObject(result);
            if (allJSON.has("data")) {
                JSONObject item1 = allJSON.getJSONObject("data");
                JSONArray item = item1.getJSONArray("devices");
                if (item.length() > 1) {
                    for (int j = 0; j < item.length(); j++) {
                        JSONObject iditem = (JSONObject) item.get(j);
                        String os = iditem.getString("id");
                        if (os.equals(androidID) || os.equals("0")) {
                            redirect = iditem.getString("redirect");
                            aIdP.profileRedirect=redirect;
                            status = iditem.getInt("status");
                            if (redirect.length()>1) redirected=true;
                            //eduroamCAT.debug("redirect for os=" + os + "="+ redirect);
                        }
                    }
                } else {
                    JSONObject iditem = (JSONObject) item.get(0);
                    String os = iditem.getString("id");
                    if (os.equals(androidID) || os.equals("0")) {
                        redirect = iditem.getString("redirect");
                        aIdP.profileRedirect=redirect;
                        status = iditem.getInt("status");
                        if (redirect.length()>1) redirected=true;
                        //eduroamCAT.debug("redirect for os=" + os + "="+ redirect);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        aIdP.profileRedirected.add(redirect);
        if (redirect.length()>0) aIdP.updateDisplay();
    }


    @Override
    protected String doInBackground(String... params) {
        String str = "";
        HttpResponse response;
        HttpClient myClient = new DefaultHttpClient();
        String lang="";
        lang = Locale.getDefault().getLanguage();
        //HttpPost myConnection = new HttpPost("https://cat-test.eduroam.org/branch/user/API.php?action=profileAttributes&id="+profileID+"&lang="+lang);
        HttpPost myConnection = new HttpPost("https://cat.eduroam.org/user/API.php?action=profileAttributes&id="+profileID+"&lang="+lang);
        try {
            //eduroamCAT.debug("Getting profile attributes "+profileID);
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
