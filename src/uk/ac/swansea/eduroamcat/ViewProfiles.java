//*******************
//Author: Gareth Ayres <g.j.ayres@swansea.ac.uk>
//SENSE Project
//https://github.com/GEANT/CAT-Android
//*******************
package uk.ac.swansea.eduroamcat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;

public class ViewProfiles extends Activity {

    SimpleCursorAdapter mAdapter;
    static ProfileAdapter adapter;
    static SCAD scad = null;
    static ListView listView = null;
    Switch switch_search;
    private String searchText="";
    String search="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profiles);
        listView = (ListView) findViewById(R.id.list);
        switch_search = (Switch) findViewById(R.id.switch1);
        switch_search.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
               boolean isChecked) {
                if(isChecked){
                    AlertDialog.Builder builder = new AlertDialog.Builder(ViewProfiles.this);
                    builder.setTitle(getString(R.string.manual_search));
                    final EditText input = new EditText(ViewProfiles.this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);
                    builder.setPositiveButton(getString(R.string.search), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            searchText = input.getText().toString();
                            if (searchText.length()>2) {
                                search = searchText;
                                if (search != null) search = search.trim();
                                adapter.clear();
                                scad = new SCAD(ViewProfiles.this, search);
                                scad.execute();
                            }
                        }
                    });
                    builder.setNegativeButton(getString(R.string.button_no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }else{
                    search="";
                    adapter.clear();
                }

            }
        });
        scad = new SCAD(this,search);
        scad.execute();
        adapter = new ProfileAdapter(this, SCAD.IdPs);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id)
            {
                eduroamCAT.debug("Click in listview");
                Intent download = new Intent(getApplicationContext(), EAPMetadata.class);
                if (!adapter.getItem(position).profileRedirect.equals("0") && adapter.getItem(position).profileRedirect.length()>0) {
                    String url=adapter.getItem(position).profileRedirect;
                    if (url.contains("http")==false)
                        url="http://"+url;
                    Uri uri = Uri.parse(url);
                    //launch browser for a redirect
                    eduroamCAT.debug("redirect click:"+adapter.getItem(position).title +" and "+uri.toString());
                    if (url.toString().length()>0) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(browserIntent);
                    }
                }
                else {
                    if (adapter.getItem(position).profileID.size() == 1) {
                        if (adapter.getItem(position).download.length() > 0) {
                            Uri uri = Uri.parse(adapter.getItem(position).download);
                            eduroamCAT.debug("Click Download:" + uri.toString());
                            download.setData(uri);
                            startActivity(download);
                            eduroamCAT.debug("started activity");
                        }
                    }
                }
            }

        });
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        eduroamCAT.debug("resumed viewProfiles");
        if (adapter.getCount()>0) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SCAD.MAX_DISTANCE=30000;
    }



    }
