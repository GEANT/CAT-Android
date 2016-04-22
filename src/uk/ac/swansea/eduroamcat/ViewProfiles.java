package uk.ac.swansea.eduroamcat;

import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;

public class ViewProfiles extends Activity {

    SimpleCursorAdapter mAdapter;
    static ProfileAdapter adapter;
    static SCAD scad = null;
    static ListView listView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        scad = new SCAD(this);
        scad.execute();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profiles);
        listView = (ListView) findViewById(R.id.list);
        adapter = new ProfileAdapter(this, SCAD.IdPs);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id)
            {
                eduroamCAT.debug("Click in listview");
                Intent download = new Intent(getApplicationContext(), EAPMetadata.class);
                if (!adapter.getItem(position).profileRedirect.equals("0")) {
                    Uri uri = Uri.parse(adapter.getItem(position).profileRedirect);
                    //launch browser for a redirect
                    eduroamCAT.debug("redirect click:"+adapter.getItem(position).title +" and "+adapter.getItem(position).profileRedirect);
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(browserIntent);
                }
                else {
                    Uri uri = Uri.parse(adapter.getItem(position).download);
                    eduroamCAT.debug("Click Download:"+uri.toString());
                    download.setData(uri);
                    startActivity(download);
                    eduroamCAT.debug("started activity");
                }
            }

        });
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        if (adapter.getCount()>0) {
            adapter.clear();
        }
        adapter.notifyDataSetChanged();
    }



    }
