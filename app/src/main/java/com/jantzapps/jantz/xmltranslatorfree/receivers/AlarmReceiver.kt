package com.jantzapps.jantz.xmltranslatorfree.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment

import com.jantzapps.jantz.xmltranslatorfree.helpers.DbHelper
import com.jantzapps.jantz.xmltranslatorfree.services.RestartAlarmService

import java.io.File

/**
 * Created by jantz on 7/11/2017.
 */

class AlarmReceiver : BroadcastReceiver() {
    internal var day = 86400000
    internal val Xml_limit_path = File(Environment.getExternalStorageDirectory().toString() + "/App_data/")
    internal val Xml_limit = File(Xml_limit_path, "Char.txt")

    override fun onReceive(context: Context, intent: Intent) {

        if ("android.intent.action.BOOT_COMPLETED" == intent.action) {
            val i = Intent(context, RestartAlarmService::class.java)
            val service = context.startService(i)
        }

        if (Build.VERSION.SDK_INT >= 23) {
            try {
                Xml_limit.delete()
            } catch (e: Exception) {
            }

        } else {
            // Code for Below 23 API Oriented Device
            // Do next code
            try {
                Xml_limit.delete()
            } catch (e: Exception) {
            }

        }



        val dbHelper = DbHelper(context)

        dbHelper.newCharCount()
        dbHelper.newTime()

        val alarm_manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent2 = Intent(context, AlarmReceiver::class.java)
        intent2.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        val mAlarmIntent = PendingIntent.getBroadcast(context, 0, intent2, 0)

        alarm_manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + day, mAlarmIntent)


    }
}

