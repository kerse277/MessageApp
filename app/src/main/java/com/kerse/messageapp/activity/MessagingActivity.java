package com.kerse.messageapp.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerse.messageapp.R;
import com.kerse.messageapp.adapter.MessagingAdapter;
import com.kerse.messageapp.config.Config;
import com.kerse.messageapp.database.SqliteDatabase;
import com.kerse.messageapp.extra.Preferences_;
import com.kerse.messageapp.model.CustomUser;
import com.kerse.messageapp.model.Message;
import com.kerse.messageapp.model.ServerResponse;
import com.kerse.messageapp.repository.MessageRepository;
import com.kerse.messageapp.repository.UserRepository;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FocusChange;
import org.androidannotations.annotations.KeyDown;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.androidannotations.api.UiThreadExecutor;
import org.androidannotations.rest.spring.annotations.RestService;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import jp.wasabeef.picasso.transformations.MaskTransformation;

@EActivity(R.layout.activity_messaging)
public class MessagingActivity extends AppCompatActivity implements MessagingAdapter.RightViewHolder.ClickListener, MessagingAdapter.LeftViewHolder.ClickListener, MessagingAdapter.RightViewImgHolder.ClickListener, MessagingAdapter.LeftViewImgHolder.ClickListener {

    @RestService
    MessageRepository messageRepository;

    @RestService
    UserRepository userRepository;

    @ViewById(R.id.messagingRecycler)
    RecyclerView recyclerView;

    @ViewById(R.id.toolbarTitle)
    TextView toolbarTv;

    @ViewById(R.id.toolbar_logo)
    ImageView toolbarLogo;

    @ViewById(R.id.messagingToolbar)
    RelativeLayout toolbar;

    @ViewById(R.id.userStatus)
    TextView userStatus;

    @ViewById(R.id.messageBack)
    ImageButton messageBack;

    @ViewById(R.id.messageDelete)
    ImageButton messageDelete;

    @ViewById(R.id.messageAtth)
    ImageButton messageAtth;

    @ViewById(R.id.messageMenu)
    ImageButton messageMenu;

    @Pref
    Preferences_ preferences;

    @Extra("token")
    String token;

    CustomUser user;
    HashMap<String, Message> list = new HashMap<>();
    List<String> idList = new ArrayList<>();
    MessagingAdapter mAdapter;
    boolean messageStop = true;

    EmojiconEditText emojiconEditText;
    ImageView emojiImageView;
    View rootView;
    EmojIconActions emojIcon;

    @Extra("mainAndMessageStop")
    boolean mainAndMessageStop = true;

    @AfterViews
    void afterViews() {
        user = (CustomUser) getIntent().getSerializableExtra("user");
        WindowManager wm = (WindowManager) (this.getSystemService(Context.WINDOW_SERVICE));
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        final int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        toolbar.getLayoutParams().height = width / 18 * 3;
        messageBack.getLayoutParams().width = width / 14;
        Picasso.with(this)
                .load(Config.ROOT_URL+user.getProfilePhoto())
                .transform(new MaskTransformation(this, R.drawable.octagon_mask))
                .resize(width / 14 * 2, width / 14 * 2)
                .into(toolbarLogo, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(MessagingActivity.this)
                                .load(R.drawable.refresh_white)
                                .transform(new MaskTransformation(MessagingActivity.this, R.drawable.octagon_mask))
                                .resize(width / 14 * 2, width / 14 * 2)
                                .into(toolbarLogo);
                    }
                });

        toolbarTv.setText(user.getProfileName());
        loadMessages();
        if (isOnline())
            getUserStatus(user.getUniqueID());
        else
            userStatus.setText("");
        preferences.currentUserId().put(user.getUniqueID());

        rootView = findViewById(R.id.root_view);
        emojiImageView = (ImageView) findViewById(R.id.emoji_btn);
        emojiconEditText = (EmojiconEditText) findViewById(R.id.emojicon_edit_text);
        emojiconEditText.setEmojiconSize(height / 25);
        // textView = (EmojiconTextView) findViewById(R.id.textView);
        emojIcon = new EmojIconActions(this, rootView, emojiconEditText, emojiImageView);
        emojIcon.ShowEmojIcon();
        emojIcon.setIconsIds(R.drawable.ic_action_keyboard, R.drawable.smiley);
        emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                Log.e("", "Keyboard opened!");
            }

            @Override
            public void onKeyboardClose() {
                Log.e("", "Keyboard closed");
            }
        });


    }

    @Click(R.id.userProfile)
    void userProfile() {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.user_profile_dialog);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme; //style id


        ImageView photo = (ImageView) dialog.findViewById(R.id.userProfilePhoto);
        Button ok = (Button) dialog.findViewById(R.id.userOk);
        TextView name = (TextView) dialog.findViewById(R.id.userProfileFullName);
        TextView status = (TextView) dialog.findViewById(R.id.userProfileStatus);

        WindowManager wm = (WindowManager) (getSystemService(Context.WINDOW_SERVICE));
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int height = metrics.heightPixels;

        name.setText(user.getProfileName());
        status.setText(user.getProfileText());
        Picasso.with(this)
                .load(Config.ROOT_URL+user.getProfilePhoto())
                .transform(new MaskTransformation(this, R.drawable.octagon_mask))
                .resize(height / 3, height / 3)
                .into(photo);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.dismiss();
            }
        });
        dialog.show();
    }


    @UiThread(delay = 7000, id = "getStatus")
    void recurrentStatus() {
        if (isOnline()) {
            if (!userStatus.getText().toString().equals("Typing...")) {

                getUserStatus(user.getUniqueID());

            } else
                recurrentStatus();
        } else {
            recurrentStatus();
        }
    }

    @Background
    void getUserStatus(String uniqueID) {

        ServerResponse serverResponse = userRepository.getuserstatus(uniqueID);
        getStatusPost(serverResponse.getStatus());
    }

    @UiThread
    void getStatusPost(String status) {
        if (status != null) {
            if (status.equals("active"))
                userStatus.setText("Online");
            else {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                Date date = null;
                try {
                    date = format.parse(status);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String hour = String.valueOf(date.getHours());
                if (String.valueOf(date.getHours()).length() < 2)
                    hour = "0" + String.valueOf(date.getHours());
                String minute = String.valueOf(date.getMinutes());
                if (String.valueOf(date.getMinutes()).length() < 2)
                    minute = "0" + String.valueOf(date.getMinutes());

                userStatus.setText("Last Seen " + hour + ":" + minute);

            }
            recurrentStatus();
        } else {
            userStatus.setText("");
            recurrentStatus();
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    void loadMessages() {
        SqliteDatabase db = new SqliteDatabase(this);
        List<String> notSeenIds = new ArrayList<>();
        //final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        for (HashMap<String, String> map : db.messages(user.getUniqueID())) {
            Message message = new Message()
                    .setMessageText(map.get("message"))
                    .setSendDate(map.get("mdate"))
                    .setUniqueID(map.get("uniqueid"))
                    .setStatus(map.get("status"))
                    .setType(map.get("type"));
            if (message.getType().equals("0") || message.getType().equals("2")) {
                if (!message.getStatus().equals("3")) {
                    notSeenIds.add(message.getUniqueID());
                    db.updateMessageStatus(message.getUniqueID(), "3");
                }
            }
            System.out.println(message.getSendDate());
            list.put(message.getUniqueID(), message);
            idList.add(message.getUniqueID());
        }

        LinearLayoutManager lLayout = new LinearLayoutManager(this);
        // lLayout.setReverseLayout(true);
        lLayout.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(lLayout);

        mAdapter = new MessagingAdapter(list, idList, this, this, this, this, this);

        recyclerView.setAdapter(mAdapter);

        // recyclerView.smoothScrollToPosition(list.size());


       /* Collections.sort(list, new Comparator<Message>() {
            public int compare(Message o1, Message o2) {
                if (o1.getSendDate() == null || o2.getSendDate() == null)
                    return 0;
                Date date1 = null, date2 = null;
                try {
                    date1 = format.parse(o1.getSendDate());
                    date2 = format.parse(o2.getSendDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return date1.compareTo(date2);
            }
        });*/
        if (list.size() > 0) {
            ObjectMapper objectMapper = new ObjectMapper();
            String list = null;
            try {
                list = objectMapper.writeValueAsString(notSeenIds);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            sendNotSeen(list);
        }
    }

    @Background
    void sendNotSeen(String list) {
        Message message = new Message()
                .setMessageText(list)
                .setSenderToken(token)
                .setReceiverID(user.getUniqueID())
                .setStripe("status")
                .setStatus("3");
        if (isOnline()) {
            messageRepository.sendMessage(message);
        } else {
            recurrentSeen(message);
        }
    }

    @Background
    void reSeenBack(Message message) {
        messageRepository.sendMessage(message);

    }

    @UiThread(delay = 7000)
    void recurrentSeen(Message message) {
        if (isOnline()) {
            reSeenBack(message);
        } else {
            recurrentSeen(message);
        }
    }

    boolean typing = false;

    @AfterTextChange(R.id.emojicon_edit_text)
    void sendTyping() {
        if (emojiconEditText.getText().toString().length() > 0) {
            if (!typing) {
                Message message = new Message()
                        .setSenderToken(token)
                        .setMessageText("typing")
                        .setReceiverID(user.getUniqueID())
                        .setStripe("typing");
                typingSend(message);
            }
            typing = true;
        } else {
            Message message = new Message()
                    .setSenderToken(token)
                    .setMessageText("done")
                    .setReceiverID(user.getUniqueID())
                    .setStripe("typing");
            typingSend(message);
            typing = false;
        }
    }

    @Background
    void typingSend(Message message) {
        if(isOnline())
        messageRepository.sendMessage(message);
    }

    @Click(R.id.messageSend)
    void sendMessage() {
        //LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Config.REQUEST_RECIEVED));

        String uniqueID = UUID.randomUUID().toString();

        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        SqliteDatabase db = new SqliteDatabase(this);
        Message message = new Message()
                .setSenderToken(token)
                .setMessageText(StringEscapeUtils.escapeJava(emojiconEditText.getText().toString()))
                .setUniqueID(uniqueID)
                .setSendDate(format.format(date))
                .setReceiverID(user.getUniqueID())
                .setStripe("chat")
                .setType("1")
                .setStatus("0");
        emojiconEditText.setText("");
        db.addMessage(message.getMessageText(), message.getReceiverID(), message.getSendDate(), uniqueID, message.getStatus(), message.getType());
        if (!isOnline())
            db.addOflineMessage(message.getMessageText(), message.getReceiverID(), message.getSendDate(), uniqueID, message.getStatus(), message.getType());

        list.put(uniqueID, message);
        idList.add(uniqueID);
        mAdapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(list.size());
        uniqueID = null;

        if (isOnline())
            sendMessageBack(message);

    }

    @Click(R.id.messageAtth)
    void sendImageMessage() {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("image/*");
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(chooseFile, "Choose a photo"), 1);
    }

    @OnActivityResult(value = 1)
    public void activityResult(int resultCode, Intent data) {
        if (data != null) {

            Bitmap bitmap = null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (bitmap != null) {
                bitmap.recycle();
            }
            InputStream stream;
            try {

                stream = getContentResolver().openInputStream(data.getData());
                bitmap = BitmapFactory.decodeStream(stream);


                bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);

                byte[] byteArray = baos.toByteArray();
                String uniqueID = UUID.randomUUID().toString();
                Date date = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                SqliteDatabase db = new SqliteDatabase(this);

                Message message = new Message()
                        .setSenderToken(token)
                        .setMessageText(Base64.encodeToString(byteArray, Base64.DEFAULT))
                        .setUniqueID(uniqueID)
                        .setSendDate(format.format(date))
                        .setReceiverID(user.getUniqueID())
                        .setStripe("chat")
                        .setType("3")
                        .setStatus("0");
                File file = new File(Environment.getExternalStorageDirectory() + "/MessageApp/images");
                file.mkdirs();
                File mypath = new File(file, "IMG-" + format.format(date) + ".png");

                db.addMessage(mypath.getAbsolutePath(), message.getReceiverID(), message.getSendDate(), message.getUniqueID(), message.getStatus(), message.getType());
                if (!isOnline())
                    db.addOflineMessage(message.getMessageText(), message.getReceiverID(), message.getSendDate(), message.getUniqueID(), message.getStatus(), message.getType());

                byte[] decodedString = Base64.decode(message.getMessageText(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(mypath);
                    decodedByte.compress(Bitmap.CompressFormat.PNG, 100, fos);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if(isOnline())
                sendMessageBack(message);

                Message message1 = new Message()
                        .setMessageText(mypath.getAbsolutePath())
                        .setUniqueID(message.getUniqueID())
                        .setReceiverID(message.getReceiverID())
                        .setStatus(message.getStatus())
                        .setStripe(message.getStripe())
                        .setSendDate(message.getSendDate())
                        .setSenderToken(message.getSenderToken())
                        .setType(message.getType());

                list.put(uniqueID, message1);
                // list.get(uniqueID).setMessageText(mypath.getAbsolutePath());
                idList.add(uniqueID);
                mAdapter.notifyDataSetChanged();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    @FocusChange(R.id.emojicon_edit_text)
    void focus() {
        focusBack();
    }

    @UiThread(delay = 120)
    void focusBack() {

        recyclerView.smoothScrollToPosition(list.size());
    }

    @Background
    void sendMessageBack(Message message) {

        ServerResponse status = messageRepository.sendMessage(message);

        sendMessagPost(status, message);


    }


    @UiThread
    void sendMessagPost(ServerResponse status, Message message) {
        SqliteDatabase db = new SqliteDatabase(this);

        if (!status.getStatus().equals("NMR")) {

            Message message1 = list.get(message.getUniqueID()).setStatus("1");
            db.updateMessageStatus(message1.getUniqueID(), "1");
            mAdapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(list.size());
        }

    }

    @Click(R.id.messageBack)
    void MessageBack() {
        messageStop = false;
        Intent i = new Intent(this, MainActivity_.class);
        i.putExtra("mainStop", true);
        i.putExtra("mainAndMessageStop", false);
        startActivity(i);
        finish();
        UiThreadExecutor.cancelAll("getStatus");
        preferences.currentUserId().put("");
    }

    @KeyDown(KeyEvent.KEYCODE_BACK)
    void keyback() {
        messageStop = false;
        Intent i = new Intent(this, MainActivity_.class);
        i.putExtra("mainStop", true);
        i.putExtra("mainAndMessageStop", false);
        startActivity(i);
        finish();
        UiThreadExecutor.cancelAll("getStatus");
        preferences.currentUserId().put("");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (messageStop&&isOnline())
            passive();
        mainAndMessageStop = true;
        preferences.currentUserId().put("");
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (messageStop && mainAndMessageStop&&isOnline())
            active();
        preferences.currentUserId().put(user.getUniqueID());
    }

    @Background
    void passive() {
        if(isOnline())
        userRepository.passive(token);
    }

    @Background
    void active() {
        if(isOnline())
        userRepository.active(token);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocalBroadcastManager.getInstance(this).registerReceiver(mReciever, new IntentFilter(Config.REQUEST_RECIEVED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mReciever, new IntentFilter(Config.REQUEST_RECIEVED_OFFLINE));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReciever);
    }

    @UiThread(delay = 300)
    void sendSeen(String id) {

        List<String> ids = new ArrayList<>();
        ids.add(id);
        ObjectMapper om = new ObjectMapper();
        String idLists = null;
        try {
            idLists = om.writeValueAsString(ids);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        sendNotSeen(idLists);
    }

    BroadcastReceiver mReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Config.REQUEST_RECIEVED:
                    SqliteDatabase db = new SqliteDatabase(MessagingActivity.this);
                    Message message = (Message) intent.getSerializableExtra("message");
                    if (message.getSenderID().equals(user.getUniqueID())) {
                        if (message.getStripe().equals("chat")) {


                            list.put(message.getUniqueID(), message);
                            idList.add(message.getUniqueID());
                            mAdapter.notifyDataSetChanged();
                            recyclerView.smoothScrollToPosition(list.size());
                            sendSeen(message.getUniqueID());
                            db.updateMessageStatus(message.getUniqueID(), "3");
                        } else if (message.getStripe().equals("typing")) {
                            if (message.getMessageText().equals("typing")) {
                                userStatus.setText("Typing...");
                            } else {
                                userStatus.setText("Online");
                            }
                        } else if (message.getStripe().equals("status")) {
                            if (message.getStatus().equals("2")) {
                                list.get(message.getUniqueID()).setStatus("2");
                                mAdapter.notifyDataSetChanged();
                                recyclerView.smoothScrollToPosition(list.size());
                            } else if (message.getStatus().equals("3")) {
                                ObjectMapper om = new ObjectMapper();
                                List<String> iDlist = new ArrayList<>();
                                try {
                                    iDlist = Arrays.asList(om.readValue(message.getMessageText(), String[].class));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                for (String id : iDlist) {
                                    if (list.get(id) != null)
                                        list.get(id).setStatus("3");
                                }
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                    break;
                case Config.REQUEST_RECIEVED_OFFLINE:
                    String id = intent.getStringExtra("message");
                    list.get(id).setStatus("1");
                    mAdapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(list.size());
                    break;
                default:
                    // do nothing
            }
        }
    };
    boolean isMediaDelete, isImage;

    @Click(R.id.messageDelete)
    void messageDelete() {
        isMediaDelete = false;
        isImage = false;
        if (mAdapter.getSelectedItemCount() > 0) {
            final SqliteDatabase db = new SqliteDatabase(this);
            for (int i : mAdapter.getSelectedItems()) {
                if (list.get(idList.get(i)).getType().equals("2") || list.get(idList.get(i)).getType().equals("3")) {
                    isImage = true;
                }
            }
            if (isImage) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(MessagingActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(MessagingActivity.this);
                }
                builder.setTitle("Sil");
                builder.setIcon(R.drawable.delete_alert);
                builder.setMessage("Medyaları telefondan silmek istiyor musunuz ?");
                builder.setNegativeButton("HAYIR", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        for (int i : mAdapter.getSelectedItems()) {
                            db.deleteMessage(idList.get(i));
                        }
                        idList.clear();
                        list.clear();
                        afterViews();

                    }
                });


                builder.setPositiveButton("EVET", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        for (int i : mAdapter.getSelectedItems()) {
                            if (list.get(idList.get(i)).getType().equals("2") || list.get(idList.get(i)).getType().equals("3")) {
                                File file = new File(list.get(idList.get(i)).getMessageText());
                                file.delete();
                            }
                            db.deleteMessage(idList.get(i));
                        }
                        idList.clear();
                        list.clear();
                        afterViews();

                    }
                });


                builder.show();
            } else {
                for (int i : mAdapter.getSelectedItems()) {
                    db.deleteMessage(idList.get(i));
                }
                idList.clear();
                list.clear();
                afterViews();
            }


            // mAdapter.notifyDataSetChanged();

        }
        messageAtth.setVisibility(View.VISIBLE);
        messageDelete.setVisibility(View.GONE);
    }

    @Click(R.id.messageMenu)
    void messageMenu() {
        final SqliteDatabase db = new SqliteDatabase(this);
        PopupMenu popup = new PopupMenu(this, messageMenu);
        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.message_popup, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.deleteAll:
                        AlertDialog.Builder builder;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            builder = new AlertDialog.Builder(MessagingActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
                        } else {
                            builder = new AlertDialog.Builder(MessagingActivity.this);
                        }
                        builder.setTitle("Sil");
                        builder.setIcon(R.drawable.delete_alert);
                        builder.setMessage("Sohbeti silmek istediğinize emin misiniz ?");
                        builder.setNegativeButton("İPTAL", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {


                            }
                        });


                        builder.setPositiveButton("TAMAM", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                for (String ids : idList) {
                                    db.deleteMessage(ids);
                                }

                                messageStop = false;
                                Intent i = new Intent(MessagingActivity.this, MainActivity_.class);
                                i.putExtra("mainStop", true);
                                i.putExtra("mainAndMessageStop", false);
                                startActivity(i);
                                finish();
                                UiThreadExecutor.cancelAll("getStatus");
                                preferences.currentUserId().put("");

                            }
                        });


                        builder.show();

                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        popup.show();

    }


    @Override
    public void onItemClicked(int position) {
        if (mAdapter.getSelectedItemCount() > 0) {
            toggleSelection(position);
        } else {
            Message message = list.get(idList.get(position));
            if (message.getType().equals("2") || message.getType().equals("3")) {
                final Dialog dialog = new Dialog(this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.setContentView(R.layout.photo_view_dialog);
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
                ImageView photo = (ImageView) dialog.findViewById(R.id.photoView);


                WindowManager wm = (WindowManager) (getSystemService(Context.WINDOW_SERVICE));
                Display display = wm.getDefaultDisplay();
                DisplayMetrics metrics = new DisplayMetrics();
                display.getMetrics(metrics);
                int height = metrics.heightPixels;
                File file = new File(message.getMessageText());
                Picasso.with(this)
                        .load(file)
                        .into(photo);


                dialog.show();
            }
        }
        deleteVisibile();
    }

    @Override
    public boolean onItemLongClicked(int position) {
        toggleSelection(position);
        deleteVisibile();
        return true;
    }

    @UiThread(delay = 100)
    void deleteVisibile() {
        if (mAdapter.getSelectedItemCount() > 0) {
            messageAtth.setVisibility(View.GONE);
            messageDelete.setVisibility(View.VISIBLE);
        } else {
            messageAtth.setVisibility(View.VISIBLE);
            messageDelete.setVisibility(View.GONE);
        }
    }

    private void toggleSelection(int position) {
        mAdapter.toggleSelection(position);

    }
}
