package com.jantzapps.jantz.xmltranslatorfree.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class TranslationService extends Service {
    public TranslationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;
    }
}
