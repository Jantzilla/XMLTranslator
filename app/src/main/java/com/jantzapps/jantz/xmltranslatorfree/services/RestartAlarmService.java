package com.jantzapps.jantz.xmltranslatorfree.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;

import com.jantzapps.jantz.xmltranslatorfree.helpers.DbHelper;
import com.jantzapps.jantz.xmltranslatorfree.receivers.AlarmReceiver;

import java.io.File;

/**
 * Created by jantz on 7/11/2017.
 */

public class RestartAlarmService extends IntentService {
    DbHelper dbHelper;
    AlarmManager alarm_manager;
    PendingIntent mAlarmIntent;
    int day = 86400000;
    Context context;
    final File Xml_limit_path = new File(Environment.getExternalStorageDirectory() + "/App_data/");
    final File Xml_limit = new File(Xml_limit_path, "Char.txt");
    //private static final String TAG = AlarmReceiver.class.getName();


    public RestartAlarmService() {
        super("RestartAlarmsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        dbHelper = new DbHelper(getApplicationContext());
        if(System.currentTimeMillis()>= dbHelper.getTime() + day) {

            alarm_manager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            Intent intent2 = new Intent(this, AlarmReceiver.class);
            intent2.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            mAlarmIntent = PendingIntent.getBroadcast(this, 0, intent2, 0);

            alarm_manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + day, mAlarmIntent);

            dbHelper.newTime();
            dbHelper.newCharCount();

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
        }
        else {
            Long time = dbHelper.getTime();
            alarm_manager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            Intent intent2 = new Intent(this, AlarmReceiver.class);
            mAlarmIntent = PendingIntent.getBroadcast(this, 0, intent2, 0);
            alarm_manager.set(AlarmManager.RTC_WAKEUP, time, mAlarmIntent);
        }


    }

}
