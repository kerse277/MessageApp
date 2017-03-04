package com.kerse.messageapp.receiver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.kerse.messageapp.config.Config;
import com.kerse.messageapp.database.SqliteDatabase;
import com.kerse.messageapp.extra.Preferences_;
import com.kerse.messageapp.model.Message;
import com.kerse.messageapp.model.ServerResponse;
import com.kerse.messageapp.repository.MessageRepository;
import com.kerse.messageapp.xmpp.ConnectXmpp;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.rest.spring.annotations.RestService;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kerse on 03.03.2017.
 */
@EService
public class OfflineService extends Service {

    @RestService
    MessageRepository messageRepository;

    List<Message> messages = new ArrayList<>();

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
        SqliteDatabase db = new SqliteDatabase(this);

        if (db.oflineMessages().size() > 0) {
            for (HashMap<String, String> map : db.oflineMessages()) {
                Message message = new Message()
                        .setSenderToken(preferences.token().get())
                        .setMessageText(map.get("message"))
                        .setSendDate(map.get("mdate"))
                        .setUniqueID(map.get("uniqueid"))
                        .setReceiverID(map.get("userid"))
                        .setStripe("chat")
                        .setType(map.get("type"))
                        .setStatus(map.get("status"));

                messages.add(message);
            }
            waitNetwork();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {

    }

    @UiThread(delay = 4000)
    void waitNetwork() {
        if (messages.size() > 0 && isOnline()) {
            for (Message message : messages) {
                sendMessage(message);
            }
        }

    }


    @Background
    void sendMessage(Message message) {
        ServerResponse serverResponse = messageRepository.sendMessage(message);
        sendMessagePost(serverResponse, message);
    }

    @UiThread
    void sendMessagePost(ServerResponse status, Message message) {
        SqliteDatabase db = new SqliteDatabase(this);

        if (!status.getStatus().equals("NMR")) {
            db.updateMessageStatus(message.getUniqueID(), "1");
            db.deleteOflineMessage(message.getUniqueID());
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Config.REQUEST_RECIEVED_OFFLINE).putExtra("message", message.getUniqueID()));

        }

    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

}
