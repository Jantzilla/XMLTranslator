package com.jantzapps.jantz.xmltranslatorfree.services

import android.app.AlarmManager
import android.app.IntentService
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment

import com.jantzapps.jantz.xmltranslatorfree.helpers.DbHelper
import com.jantzapps.jantz.xmltranslatorfree.receivers.AlarmReceiver

import java.io.File


/**
 * Created by jantz on 7/11/2017.
 */

class RestartAlarmService : IntentService("RestartAlarmsService") {
    internal var alarm_manager: AlarmManager? = null
    internal var mAlarmIntent: PendingIntent? = null
    internal var day = 86400000
    internal val Xml_limit_path = File(Environment.getExternalStorageDirectory().toString() + "/App_data/")
    internal val Xml_limit = File(Xml_limit_path, "Char.txt")

    override fun onHandleIntent(intent: Intent?) {
        val dbHelper = DbHelper(applicationContext)
        if (System.currentTimeMillis() >= dbHelper.time!! + day) {

            alarm_manager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent2 = Intent(this, AlarmReceiver::class.java)
            intent2.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            mAlarmIntent = PendingIntent.getBroadcast(this, 0, intent2, 0)

            alarm_manager!!.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + day, mAlarmIntent)

            dbHelper.newTime()
            dbHelper.newCharCount()

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
        } else {
            val time = dbHelper.time
            alarm_manager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent2 = Intent(this, AlarmReceiver::class.java)
            mAlarmIntent = PendingIntent.getBroadcast(this, 0, intent2, 0)
            alarm_manager!!.set(AlarmManager.RTC_WAKEUP, time!!, mAlarmIntent)
        }


    }

}
