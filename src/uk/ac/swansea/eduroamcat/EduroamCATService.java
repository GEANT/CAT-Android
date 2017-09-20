//*******************
//Author: Gareth Ayres <g.j.ayres@swansea.ac.uk>
//GEANT eduroam dev
//https://github.com/GEANT/CAT-Android
//*******************

package uk.ac.swansea.eduroamcat;

import android.app.Activity;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

/**
 * Check status of eduroam profile
 */

public class EduroamCATService extends IntentService {
    private static final String CHECK_CA_CERT = "uk.ac.swansea.eduroamcat.action.CACERT";
    private static final String CHECK_CERT = "uk.ac.swansea.eduroamcat.action.CERT";

    public EduroamCATService() {
        super("EduroamCATService");
    }

    public static void startActionCACert(Context context) {
        Intent intent = new Intent(context, EduroamCATService.class);
        intent.setAction(CHECK_CA_CERT);
        context.startService(intent);
    }

    public static void startActionCert(Context context) {
        Intent intent = new Intent(context, EduroamCATService.class);
        intent.setAction(CHECK_CERT);
        context.startService(intent);
    }

    //notifcy user using a status bar notificaiton
    public static void notifyUser(Context context,int mId)
    {
        if (mId<1) { mId=1; }
        int nTitle=R.string.notification_title_connected;
        int nText=R.string.notification_message_connected;

        switch (mId) {
            case 1:
                nTitle = R.string.notification_title_connected;
                nText = R.string.notification_message_connected;
                break;
            default:
                nTitle = R.string.notification_title_connected;
                nText = R.string.notification_message_connected;
        }

        Intent intent = new Intent(context, eduroamCAT.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(context.getString(nTitle))
                        .setContentText(context.getString(nText));
        notificationManager.notify(mId, mBuilder.build());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            notifyUser(getApplicationContext(),1);

        }
        eduroamCAT.debug("cat service started");
        stopSelf();
    }

}
