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

public class ViewProfiles extends Activity {

    SimpleCursorAdapter mAdapter;
    static ProfileAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profiles);
        final ListView listView = (ListView) findViewById(R.id.list);
        adapter = new ProfileAdapter(this, SCAD.IdPs);
        if (findViewById(R.id.list)==null) eduroamCAT.debug("NULL ACTIVITY");
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id)
            {
                Intent download = new Intent(getApplicationContext(), EAPMetadata.class);
                if (adapter.getItem(position).profileRedirect.length()>0) {
                    Uri uri = Uri.parse(adapter.getItem(position).profileRedirect);
                    //launch browser
                }
                else {
                    Uri uri = Uri.parse(adapter.getItem(position).download);
                    download.setData(uri);
                    startActivity(download);
                }
            }

        });
    }



}
