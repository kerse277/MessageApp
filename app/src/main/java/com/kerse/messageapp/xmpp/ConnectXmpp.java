package com.kerse.messageapp.xmpp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.digits.sdk.android.internal.TosFormatHelper;
import com.kerse.messageapp.extra.Preferences_;


public class ConnectXmpp extends Service {

    private String userName;
    private String passWord;
    private MyXMPP xmpp = new MyXMPP();

    public ConnectXmpp() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Preferences_ preferences = new Preferences_(this);

        userName = preferences.uniqueId().get();
        passWord = preferences.password().get();
        xmpp.init(userName, passWord, ConnectXmpp.this);
        xmpp.connectConnection();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // xmpp.disconnectConnection();
        // super.onDestroy();
    }


}
