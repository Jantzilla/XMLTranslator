package com.jantzapps.jantz.xmltranslatorfree.helpers;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

public class GoogleApiHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = GoogleApiHelper.class.getSimpleName();
    private Context context;
    private static GoogleApiClient mGoogleApiClient;
    private ConnectionListener connectionListener;
    private Bundle connectionBundle;

    public GoogleApiHelper(Context context) {
        if(mGoogleApiClient == null) {
            this.context = context;
            buildGoogleApiClient();
        }
        connect();
    }

    public GoogleApiClient getGoogleApiClient() {
        return this.mGoogleApiClient;
    }

    public void clearDefaultAccountAndReconnect() {
        mGoogleApiClient.clearDefaultAccountAndReconnect();
    }

    public void setConnectionListener(ConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
        if (this.connectionListener != null && isConnected()) {
            connectionListener.onConnected(connectionBundle);
        }
    }

    public void connect() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    public void disconnect() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public boolean isConnected() {
        return mGoogleApiClient != null && mGoogleApiClient.isConnected();
    }

    private void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(context).addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addScope(Drive.SCOPE_APPFOLDER)                                                     // required for App Folder sample
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) context)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) context)
                .build();

    }

    @Override
    public void onConnected(Bundle bundle) {
        connectionBundle = bundle;
        if (connectionListener != null) {
            connectionListener.onConnected(bundle);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: googleApiClient.connect()");
        mGoogleApiClient.connect();
        if (connectionListener != null) {
            connectionListener.onConnectionSuspended(i);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: connectionResult = " + connectionResult);
        if (connectionListener != null) {
            connectionListener.onConnectionFailed(connectionResult);
        }
    }

    public interface ConnectionListener {
        void onConnectionFailed(@NonNull ConnectionResult connectionResult);

        void onConnectionSuspended(int i);

        void onConnected(Bundle bundle);
    }
}