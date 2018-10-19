//*******************
//Author: Gareth Ayres <g.j.ayres@swansea.ac.uk>
//SENSE Project
//https://github.com/GEANT/CAT-Android
//*******************
package uk.ac.swansea.eduroamcat;

import java.io.InputStream;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;


public class ProfileLogo extends AsyncTask<String, Integer, Bitmap> {

    int idpID;
    int profile;
    String androidID="";
    IdP aIdP;

    public ProfileLogo (int idpID,IdP idp)
    {
        this.idpID=idpID;
        this.aIdP=idp;
        //eduroamCAT.debug("profile logo....");
    }

    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        if (result!=null) {
            aIdP.logo = result;
            //eduroamCAT.debug("got logo for " + idpID + " and got " + result.toString());
        }
        ViewProfiles.adapter.notifyDataSetChanged();

}


    @Override
    protected Bitmap doInBackground(String... params) {
        Bitmap logo = null;
        String url = "https://cat.eduroam.org/user/API.php?action=sendLogo&id="+idpID+"";
        try {
            //eduroamCAT.debug("Getting profile logo "+idpID);
            InputStream in = new java.net.URL(url).openStream();
            logo = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            eduroamCAT.debug(e.getMessage());
        }
        return logo;
    }


}
