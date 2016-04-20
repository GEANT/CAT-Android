package uk.ac.swansea.eduroamcat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by AyresGJ on 21/03/2016.
 */
public class ProfileAdapter extends ArrayAdapter<IdP> {

        public ProfileAdapter(Context context, ArrayList<IdP> idps) {
            super(context, 0, idps);
        }

        String lang= Locale.getDefault().getLanguage();

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            IdP aIdP = getItem(position);
            eduroamCAT.debug("Adding item to List:"+aIdP.getName());
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.profile_item, parent, false);
            }
            // Lookup view for data population
            TextView firstLine = (TextView) convertView.findViewById(R.id.firstLine);
            TextView secondLine = (TextView) convertView.findViewById(R.id.secondLine);
            ImageView logo = (ImageView)convertView.findViewById(R.id.icon);
            if (aIdP.profileHasLogo) {
                eduroamCAT.debug("has logo set");
                logo.setImageBitmap(aIdP.logo);
            }

            // Populate the data into the template view using the data object
            firstLine.setText(aIdP.getName());
            secondLine.setText(getContext().getString(R.string.distance)+"="+aIdP.getDistance()+"Km");

            // Return the completed view to render on screen
            return convertView;
        }
}