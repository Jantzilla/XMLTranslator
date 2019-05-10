package com.jantzapps.jantz.xmltranslatorfree.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager

import com.google.android.gms.common.api.GoogleApiClient
import com.jantzapps.jantz.xmltranslatorfree.R
import com.jantzapps.jantz.xmltranslatorfree.activities.MainActivity
import com.jantzapps.jantz.xmltranslatorfree.helpers.GoogleApiHelper
import com.jantzapps.jantz.xmltranslatorfree.utils.TranslateXML

import java.util.ArrayList

class TranslationService : Service() {
    private var fromLang = ""
    private var toLangs: Array<String>? = null
    private var xmlStringsList: ArrayList<String>? = null
    private var xmlNamesList: ArrayList<String>? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var googleApiHelper: GoogleApiHelper? = null
    private var broadcaster: LocalBroadcastManager? = null
    private var intent: Intent? = null
    private var pendingIntent: PendingIntent? = null

    override fun onCreate() {
        super.onCreate()
        broadcaster = LocalBroadcastManager.getInstance(this)
        intent = Intent(this, MainActivity::class.java)
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        if (mGoogleApiClient == null) {
            googleApiHelper = GoogleApiHelper(this)
            mGoogleApiClient = googleApiHelper!!.googleApiClient
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        if (intent.getBooleanExtra("stopped", false)) {
            TranslateXML.stopTranslation()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(Service.STOP_FOREGROUND_DETACH)
            } else
                stopForeground(false)
            stopSelf()

        } else {

            this.fromLang = intent.getStringExtra("fromLang")
            this.toLangs = intent.getStringArrayExtra("toLangs")
            this.xmlStringsList = intent.getStringArrayListExtra("xmlStringsList")
            this.xmlNamesList = intent.getStringArrayListExtra("xmlNamesList")

            TranslateXML.translateXML(fromLang, toLangs, xmlStringsList, mGoogleApiClient, xmlNamesList, this, broadcaster)

            startForeground(REQUEST_CODE, createNotification(100, 0, getString(R.string.translating)))
        }

        return Service.START_NOT_STICKY
    }

    override fun stopService(name: Intent): Boolean {

        TranslateXML.stopTranslation()
        stopForeground(false)
        stopSelf()
        return true

    }

    fun createNotification(totalUnits: Int, completedUnits: Int, caption: String): Notification {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "Progress", NotificationManager.IMPORTANCE_HIGH)

            // Configure the notification channel.
            notificationChannel.description = "Channel description"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            notificationChannel.enableVibration(true)
            notificationChannel.importance = NotificationManager.IMPORTANCE_HIGH
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        var percentComplete = 0
        if (totalUnits > 0) {
            percentComplete = 100 * completedUnits / totalUnits
        }

        if (percentComplete == 100) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(Service.STOP_FOREGROUND_DETACH)
            }

            //Return the latest progress of task
            return NotificationCompat.Builder(baseContext, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(caption)
                    .setContentInfo("$percentComplete%")
                    .setContentIntent(pendingIntent)
                    .setOngoing(false)
                    .setAutoCancel(false)
                    .build()

        } else {

            //Return the latest progress of task
            return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(caption)
                    .setProgress(100, percentComplete, false)
                    .setOnlyAlertOnce(true)
                    .setContentInfo("$percentComplete%")
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .build()

        }
    }

    override fun onDestroy() {
        TranslateXML.stopTranslation()
        stopSelf()

        super.onDestroy()
    }

    companion object {
        private val NOTIFICATION_CHANNEL_ID = "Translation Notification"
        private val REQUEST_CODE = 132
    }


}
