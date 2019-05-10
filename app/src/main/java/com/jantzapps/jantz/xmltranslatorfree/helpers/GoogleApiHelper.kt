package com.jantzapps.jantz.xmltranslatorfree.helpers

import android.content.Context
import android.os.Bundle

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.drive.Drive

class GoogleApiHelper(context: Context) : GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private var context: Context? = null

    val googleApiClient: GoogleApiClient? = null

    init {
        if (mGoogleApiClient == null) {
            this.context = context
            buildGoogleApiClient()
        }
        connect()
    }

    fun clearDefaultAccountAndReconnect() {
        mGoogleApiClient!!.clearDefaultAccountAndReconnect()
    }

    fun connect() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient!!.connect()
        }
    }

    private fun buildGoogleApiClient() {

        mGoogleApiClient = GoogleApiClient.Builder(context!!).addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addScope(Drive.SCOPE_APPFOLDER)                                                     // required for App Folder sample
                .addConnectionCallbacks((context as GoogleApiClient.ConnectionCallbacks?)!!)
                .addOnConnectionFailedListener((context as GoogleApiClient.OnConnectionFailedListener?)!!)
                .build()

    }

    override fun onConnected(bundle: Bundle?) {}

    override fun onConnectionSuspended(i: Int) {}

    override fun onConnectionFailed(connectionResult: ConnectionResult) {}

    companion object {
        private var mGoogleApiClient: GoogleApiClient? = null
    }

}