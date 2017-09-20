//*******************
//Author: Gareth Ayres <g.j.ayres@swansea.ac.uk>
//GEANT eduroam dev
//https://github.com/GEANT/CAT-Android
//*******************
package uk.ac.swansea.eduroamcat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager alarmMgr;
        Intent intentnew = new Intent(context, EduroamCATService.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intentnew, 0);
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                    AlarmManager.INTERVAL_FIFTEEN_MINUTES, alarmIntent);
            eduroamCAT.debug("alarm set");
        }
    }
}
