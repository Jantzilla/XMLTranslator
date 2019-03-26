package com.jantzapps.jantz.xmltranslatorfree.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.jantzapps.jantz.xmltranslatorfree.R;
import com.jantzapps.jantz.xmltranslatorfree.activities.MainActivity;
import com.jantzapps.jantz.xmltranslatorfree.helpers.GoogleApiHelper;
import com.jantzapps.jantz.xmltranslatorfree.utils.TranslateXML;

import java.util.ArrayList;

public class TranslationService extends Service {
    private static final String NOTIFICATION_CHANNEL_ID = "Translation Notification";
    private static final int REQUEST_CODE = 132;
    private String fromLang = "";
    private String[] toLangs;
    private ArrayList<String> xmlStringsList;
    private ArrayList<String> xmlNamesList;
    private GoogleApiClient mGoogleApiClient;
    private GoogleApiHelper googleApiHelper;
    private LocalBroadcastManager broadcaster;
    private Intent intent;
    private PendingIntent pendingIntent;

    public TranslationService() {}

    @Override
    public void onCreate() {
        super.onCreate();
        broadcaster = LocalBroadcastManager.getInstance(this);
        intent = new Intent(this, MainActivity.class);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        if(mGoogleApiClient == null) {
            googleApiHelper = new GoogleApiHelper(this);
            mGoogleApiClient = googleApiHelper.getGoogleApiClient();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent.getBooleanExtra("stopped", false)) {
            TranslateXML.stopTranslation(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_DETACH);
            } else
                stopForeground(false);
            stopSelf();

        } else {

            this.fromLang = intent.getStringExtra("fromLang");
            this.toLangs = intent.getStringArrayExtra("toLangs");
            this.xmlStringsList = intent.getStringArrayListExtra("xmlStringsList");
            this.xmlNamesList = intent.getStringArrayListExtra("xmlNamesList");

            TranslateXML.translateXML(fromLang, toLangs, xmlStringsList, mGoogleApiClient, xmlNamesList, this, broadcaster);

            startForeground(REQUEST_CODE, createNotification(100, 0, "Translating..."));
        }

        return START_NOT_STICKY;
    }

    @Override
    public boolean stopService(Intent name) {

        TranslateXML.stopTranslation(false);
        stopForeground(false);
        stopSelf();
        Log.d("Translation Has Been", " Stopped");

        return true;

    }

    public Notification createNotification(int totalUnits, int completedUnits, String caption) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Progress", NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        int percentComplete = 0;
        if (totalUnits > 0) {
            percentComplete = (int) (100 * completedUnits / totalUnits);
        }

        if(percentComplete == 100) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_DETACH);
            }

            //Return the latest progress of task
            return new NotificationCompat.Builder(getBaseContext(), NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(caption)
                    .setContentInfo(String.valueOf(percentComplete +"%"))
                    .setContentIntent(pendingIntent)
                    .setOngoing(false)
                    .setAutoCancel(false)
                    .build();

        } else {

            //Return the latest progress of task
            return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(caption)
                    .setProgress(100, percentComplete, false)
                    .setContentInfo(String.valueOf(percentComplete +"%"))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .build();

        }
    }

    @Override
    public void onDestroy() {
        TranslateXML.stopTranslation(false);
        stopSelf();

        super.onDestroy();
    }


}
