//*******************
//Author: Gareth Ayres <g.j.ayres@swansea.ac.uk>
//SENSE Project
//https://github.com/GEANT/CAT-Android
//*******************
package uk.ac.swansea.eduroamcat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;

public class ProfileAdapter extends ArrayAdapter<IdP> {

        public ProfileAdapter(Context context, ArrayList<IdP> idps) {
            super(context, 0, idps);
        }

        String lang= Locale.getDefault().getLanguage();

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final IdP aIdP = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.profile_item, parent, false);
            }
                // Lookup view for data population
                TextView firstLine = (TextView) convertView.findViewById(R.id.firstLine);
                TextView secondLine = (TextView) convertView.findViewById(R.id.secondLine);
                ImageView logo = (ImageView) convertView.findViewById(R.id.icon);
                if (aIdP.profileHasLogo) {
                    //eduroamCAT.debug("has logo set");
                    logo.setImageBitmap(aIdP.logo);
                }

                // Populate the data into the template view using the data object
                firstLine.setText(aIdP.getName());
                if (aIdP.getDistance() < 1000)
                    secondLine.setText(getContext().getString(R.string.distance) + "=" + aIdP.getDistance() + "Km");
                else secondLine.setText("");

                if (aIdP.profileID.size() > 1) {
                    //add buttons for multiple profiles
                    LinearLayout layout = (LinearLayout) convertView.findViewById(R.id.profiles);
                    RelativeLayout item = (RelativeLayout) convertView.findViewById(R.id.itemlayout);
                    ViewGroup.LayoutParams params = item.getLayoutParams();
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    TextView idpName = new TextView(getContext());
                    idpName.setText(aIdP.getName() + ":");
                    idpName.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
                    layout.removeAllViews();
                    layout.addView(idpName);
                    for (final String profile : aIdP.profileDisplay) {
                        Button btnTag = new Button(getContext());
                        btnTag.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        btnTag.setText(profile);
                        btnTag.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                Uri uri = Uri.parse(aIdP.getDownload(profile));
                                eduroamCAT.debug("Click Download:"+uri.toString());
                                Intent download = new Intent(getContext(), EAPMetadata.class);
                                download.setData(uri);
                                getContext().startActivity(download);
                                eduroamCAT.debug("started activity");
                            }
                        });
                        layout.addView(btnTag);
                    }
                }
            return convertView;
        }
}