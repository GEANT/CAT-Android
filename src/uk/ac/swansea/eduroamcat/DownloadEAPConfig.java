//*******************
//Author: Gareth Ayres <g.j.ayres@swansea.ac.uk>
//SENSE Project
//https://github.com/GEANT/CAT-Android
//*******************
package uk.ac.swansea.eduroamcat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import android.os.AsyncTask;
import android.os.Environment;

class DownloadEAPConfig extends AsyncTask<String, Integer, Integer> {

    @Override
    protected Integer doInBackground(String... aurl) {
        int code=0;
        try {
            URL url = new URL(aurl[0]);
            URLConnection connection = url.openConnection();
            eduroamCAT.debug("starting download...");
            eduroamCAT.downloaded=false;
            //test storage
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                eduroamCAT.debug("Storage state ok ="+state.toString());
            }
            else eduroamCAT.debug("Storage state BAD ="+state.toString());
            InputStream input = new BufferedInputStream(url.openStream());
            String path = Environment.getExternalStorageDirectory().getPath() + "/EAPConfig/";
            File file = new File(path);
            file.mkdirs();
            File outputFile = new File(file, "eduroam.eap-config");
            OutputStream output = new FileOutputStream(outputFile);
            byte data[] = new byte[1024 * 1024];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();
            eduroamCAT.debug("ending download");
            code=1;

        } catch (Exception e) {
        	eduroamCAT.debug("Error writing file to external storage:"+e.getMessage() );
            code=0;
        }
        if (code>0) eduroamCAT.downloaded=true;
        return code;
    }

    protected void onPostExecute(Integer unused) {
        eduroamCAT.debug("finished download...");
        eduroamCAT.downloaded=true;
    }

    protected void onProgressUpdate(Integer... progress) {
        eduroamCAT.debug("downloading...");
    }

}