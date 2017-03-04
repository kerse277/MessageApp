package com.kerse.messageapp.fragment;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.baoyz.widget.PullRefreshLayout;
import com.kerse.messageapp.R;
import com.kerse.messageapp.activity.MainActivity;
import com.kerse.messageapp.adapter.MessagesViewAdapter;
import com.kerse.messageapp.adapter.UsersViewAdapter;
import com.kerse.messageapp.config.Config;
import com.kerse.messageapp.database.SqliteDatabase;
import com.kerse.messageapp.extra.Preferences_;
import com.kerse.messageapp.model.CustomUser;
import com.kerse.messageapp.model.Message;
import com.kerse.messageapp.model.MessageListModel;
import com.kerse.messageapp.repository.UserRepository;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.androidannotations.rest.spring.annotations.RestService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


@EFragment(R.layout.fragment_message)
public class MessageFragment extends Fragment {

    @RestService
    UserRepository userRepository;

    @ViewById(R.id.messagesRecycler)
    RecyclerView recyclerView;

    @Pref
    Preferences_ preferences;

    @AfterViews
    void afterViews() {


        SqliteDatabase db = new SqliteDatabase(getContext());
        final List<MessageListModel> msgModels = new ArrayList<>();

        for (HashMap<String, String> userMap : db.users()) {
            CustomUser customUser = new CustomUser()
                    .setProfileName(userMap.get("fullname"))
                    .setUniqueID(userMap.get("uniqueid"))
                    .setProfilePhoto(userMap.get("photo"))
                    .setProfileText(userMap.get("ptxt"));
            HashMap<String, String> messageMap = db.lastMessage(userMap.get("uniqueid"));
            Message message = new Message()
                    .setSendDate(messageMap.get("mdate"))
                    .setMessageText(messageMap.get("message"))
                    .setType(messageMap.get("type"));

            int badge = 0;
            for (HashMap<String, String> map : db.messages(customUser.getUniqueID())) {
                Message messageBadge = new Message()
                        .setMessageText(map.get("message"))
                        .setSendDate(map.get("mdate"))
                        .setUniqueID(map.get("uniqueid"))
                        .setStatus(map.get("status"))
                        .setType(map.get("type"));
                if (messageBadge.getType().equals("0") || messageBadge.getType().equals("2")) {
                    if (!messageBadge.getStatus().equals("3")) {
                        badge++;
                    }
                }
            }
            if (messageMap.get("mdate") != null) {
                MessageListModel messageListModel = new MessageListModel()
                        .setMessage(message)
                        .setCustomUser(customUser)
                        .setBadge(badge);
                msgModels.add(messageListModel);
            }
        }
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        Collections.sort(msgModels, new Comparator<MessageListModel>() {
            public int compare(MessageListModel o1, MessageListModel o2) {
                if (o1.getMessage().getSendDate() == null || o2.getMessage().getSendDate() == null)
                    return 0;
                Date date1 = null, date2 = null;
                try {
                    date1 = format.parse(o1.getMessage().getSendDate());
                    date2 = format.parse(o2.getMessage().getSendDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return date2.compareTo(date1);
            }
        });

        LinearLayoutManager lLayout = new LinearLayoutManager(getContext());

        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(lLayout);

        final MessagesViewAdapter mAdapter = new MessagesViewAdapter((MainActivity) getActivity(), msgModels, preferences.token().get());

        recyclerView.setAdapter(mAdapter);


    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocalBroadcastManager.getInstance((MainActivity) getActivity()).registerReceiver(mReciever, new IntentFilter(Config.REQUEST_RECIEVED));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance((MainActivity) getActivity()).unregisterReceiver(mReciever);
    }

    BroadcastReceiver mReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Config.REQUEST_RECIEVED:
                    afterViews();
                    break;
                default:
                    // do nothing
            }
        }
    };

}
