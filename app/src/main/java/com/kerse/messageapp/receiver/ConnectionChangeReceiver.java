package com.kerse.messageapp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.kerse.messageapp.xmpp.ConnectXmpp;

public class ConnectionChangeReceiver extends BroadcastReceiver {
    static boolean isConnected = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null) {
            if (activeNetInfo.getDetailedState().toString().equals("DISCONNECTED")) {
                Toast.makeText(context, "Active Network Type : " + activeNetInfo.getTypeName(), Toast.LENGTH_SHORT).show();
                Intent i = new Intent(context, ConnectXmpp.class);
                context.stopService(i);
                context.startService(i);
                isConnected = false;
            }
            if (activeNetInfo.getDetailedState().toString().equals("CONNECTED")) {
                Toast.makeText(context, "Mobile Network Type : " + activeNetInfo.getTypeName(), Toast.LENGTH_SHORT).show();
                Intent i = new Intent(context, ConnectXmpp.class);
                context.stopService(i);
                context.startService(i);
                if (!isConnected) {
                    Intent intent1 = new Intent(context, OfflineService_.class);
                    context.stopService(intent1);
                    context.startService(intent1);
                    isConnected = true;
                }
            }
        } else
            isConnected = false;
    }
}