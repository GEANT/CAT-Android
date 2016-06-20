package uk.ac.swansea.eduroamcat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
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
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setUseCaches(false);
            connection.connect();
            if ( connection instanceof HttpURLConnection) {
                HttpURLConnection httpConnection = (HttpURLConnection) connection;

                code = httpConnection.getResponseCode();
                eduroamCAT.debug("response code="+code);
            }
//            int fileLength = connection.getContentLength();
//            int tickSize = 2 * fileLength / 100;
//            int nextProgress = tickSize;
            InputStream input = new BufferedInputStream(url.openStream());
            String path = Environment.getExternalStorageDirectory().getPath() + "/EAPConfig/";
            File file = new File(path);
            file.mkdirs();
            File outputFile = new File(file, "eduroam.eap-config");
            OutputStream output = new FileOutputStream(outputFile);
            byte data[] = new byte[1024 * 1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();
            eduroamCAT.debug("ending download");

        } catch (Exception e) {
        	eduroamCAT.debug("Error writing file to external storage");
        }
        eduroamCAT.downloaded=true;
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