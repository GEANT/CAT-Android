package uk.ac.swansea.eduroamcat;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.widget.Toast;
import uk.ac.swansea.eduroamcat.R;

class TabsListener implements ActionBar.TabListener {
            public Fragment fragment;
            public Context context;
 
            public TabsListener(Fragment fragment, Context context) {
                        this.fragment = fragment;
                        this.context = context;
 
            }
 
            public void onTabSelected(Tab tab, FragmentTransaction ft) {
                        ft.replace(R.id.start_activity, fragment);
            }

			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {
				
			}

			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {
                 ft.remove(fragment);
				
			}
			
}