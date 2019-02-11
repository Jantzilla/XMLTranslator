package com.jantzapps.jantz.xmltranslatorfree;

import android.*;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.os.Environment.getExternalStoragePublicDirectory;
import static android.support.v4.app.NotificationCompat.PRIORITY_MAX;
import static java.security.AccessController.getContext;

/**
 * Created by jantz on 7/11/2017.
 */

public class AlarmReceiver extends BroadcastReceiver {
    DbHelper dbHelper;
    AlarmManager alarm_manager;
    PendingIntent mAlarmIntent;
    int day = 86400000;
    final File Xml_limit_path = new File(Environment.getExternalStorageDirectory() + "/App_data/");
    final File Xml_limit = new File(Xml_limit_path, "Char.txt");

    @Override
    public void onReceive(Context context, Intent intent) {

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent i = new Intent(context, RestartAlarmService.class);
            ComponentName service = context.startService(i);
        }

        if (Build.VERSION.SDK_INT >= 23) {
            try {
                Xml_limit.delete();
            } catch (Exception e) {}
        }
        else
        {
            // Code for Below 23 API Oriented Device
            // Do next code
            try {
                Xml_limit.delete();
            } catch (Exception e) {}
        }



        dbHelper = new DbHelper(context);

        dbHelper.newCharCount();
        dbHelper.newTime();

        alarm_manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent2 = new Intent(context, AlarmReceiver.class);
        intent2.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        mAlarmIntent = PendingIntent.getBroadcast(context, 0, intent2, 0);

        alarm_manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + day, mAlarmIntent);



    }}

