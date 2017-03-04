package com.kerse.messageapp.xmpp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guardanis.imageloader.ImageRequest;
import com.guardanis.imageloader.filters.ImageFilter;
import com.kerse.messageapp.R;
import com.kerse.messageapp.activity.MainActivity;
import com.kerse.messageapp.activity.MainActivity_;
import com.kerse.messageapp.activity.MessagingActivity_;
import com.kerse.messageapp.config.Config;
import com.kerse.messageapp.database.SqliteDatabase;
import com.kerse.messageapp.extra.Preferences_;
import com.kerse.messageapp.model.CustomUser;
import com.kerse.messageapp.model.ServerResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Ankit on 10/3/2015.
 */
public class MyXMPP {


    private static final String DOMAIN = "localhost";
    private static final String HOST = Config.ROOT_URL_XMPP;
    private static final int PORT = 5222;
    private String userName = "";
    private String passWord = "";
    AbstractXMPPConnection connection;
    ChatManager chatmanager;
    Chat newChat;
    XMPPConnectionListener connectionListener = new XMPPConnectionListener();
    private boolean connected;
    private boolean isToasted;
    private boolean chat_created;
    private boolean loggedin;

    public MyXMPP() {
    }

    Context context;

    //Initialize
    public void init(String userId, String pwd, Context context) {
        Log.i("XMPP", "Initializing!");
        this.userName = userId;
        this.passWord = pwd;
        this.context = context;
        XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
        configBuilder.setUsernameAndPassword(userName, passWord);
        configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        //configBuilder.setResource("Android");
        configBuilder.setServiceName(DOMAIN);
        configBuilder.setHost(HOST);
        configBuilder.setPort(PORT);
        //configBuilder.setDebuggerEnabled(true);
        connection = new XMPPTCPConnection(configBuilder.build());
        connection.addConnectionListener(connectionListener);
        connection.disconnect();

    }

    // Disconnect Function
    public void disconnectConnection() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                connection.disconnect();
            }
        }).start();
    }

    public void connectConnection() {
        AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... arg0) {

                // Create a connection
                try {
                    connection.connect();
                    login();
                    connected = true;

                } catch (IOException e) {

                } catch (SmackException e) {
                    Log.e("ERR", e.getMessage());

                } catch (XMPPException e) {
                }

                return null;
            }
        };
        connectionThread.execute();
    }


    public void sendMsg() {
        if (connection.isConnected() == true) {
            // Assume we've created an XMPPConnection name "connection"._
            chatmanager = ChatManager.getInstanceFor(connection);
            newChat = chatmanager.createChat("mehmet2@localhost");

            try {
                newChat.sendMessage("Howdy!");
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
        }
    }

    public void login() {

        try {
            connection.login(userName, passWord);

            //Log.i("LOGIN", "Yey! We're connected to the Xmpp server!");

        } catch (XMPPException | SmackException | IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }

    }


    //Connection Listener to check connection state
    public class XMPPConnectionListener implements ConnectionListener {
        @Override
        public void connected(final XMPPConnection connection) {

            Log.d("xmpp", "Connected!");
            connected = true;
            if (!connection.isAuthenticated()) {
                login();
            }
        }

        @Override
        public void connectionClosed() {
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        init(userName, passWord, context);
                        connectConnection();
                    }
                });
            Log.d("xmpp", "ConnectionCLosed!");
            connected = false;
            chat_created = false;
            loggedin = false;
        }

        @Override
        public void connectionClosedOnError(Exception arg0) {
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        init(userName, passWord, context);
                        connectConnection();
                    }
                });
            Log.d("xmpp", "ConnectionClosedOn Error!");
            connected = false;

            chat_created = false;
            loggedin = false;
        }

        @Override
        public void reconnectingIn(int arg0) {

            Log.d("xmpp", "Reconnectingin " + arg0);

            loggedin = false;
        }

        @Override
        public void reconnectionFailed(Exception arg0) {
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {


                    }
                });
            Log.d("xmpp", "ReconnectionFailed!");
            connected = false;

            chat_created = false;
            loggedin = false;
        }

        @Override
        public void reconnectionSuccessful() {
            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub


                    }
                });
            Log.d("xmpp", "ReconnectionSuccessful");
            connected = true;

            chat_created = false;
            loggedin = false;
        }

        @Override
        public void authenticated(XMPPConnection arg0, boolean arg1) {
            Log.d("xmpp", "Authenticated!");
            loggedin = true;
            ChatManager chatManager = ChatManager.getInstanceFor(arg0);

            chatManager.addChatListener(
                    new ChatManagerListener() {
                        @Override
                        public void chatCreated(Chat chat, boolean createdLocally) {
                            chat.addMessageListener(new ChatMessageListener() {
                                @Override
                                public void processMessage(Chat chat, Message message) {
                                    List<Message.Body> bodyList = new ArrayList<Message.Body>(message.getBodies());

                                    if (bodyList.get(0).getMessage().length() > 0) {
                                        Preferences_ preferences = new Preferences_(context);
                                        SqliteDatabase db = new SqliteDatabase(context);

                                        ObjectMapper objectMapper = new ObjectMapper();

                                        com.kerse.messageapp.model.Message messageJson = new com.kerse.messageapp.model.Message();

                                        try {
                                            messageJson = objectMapper.readValue(bodyList.get(0).getMessage(), com.kerse.messageapp.model.Message.class);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        if (messageJson.getStripe().equals("chat")) {
                                            if (messageJson.getType().equals("3")) {
                                                messageJson.setStatus("")
                                                        .setMessageText(StringEscapeUtils.unescapeJava(messageJson.getMessageText()))
                                                        .setType("2");
                                            }
                                            if (messageJson.getType().equals("1")) {
                                                messageJson.setStatus("")
                                                        .setMessageText(StringEscapeUtils.unescapeJava(messageJson.getMessageText()))
                                                        .setType("0");
                                            }

                                            if (!preferences.currentUserId().get().equals(messageJson.getSenderID()) || preferences.currentUserId().get() == null)
                                                new generatePictureStyleNotification(context, messageJson, db.userInfo(messageJson.getSenderID()).get("photo")).execute();

                                            // getBitmapFromURL(db.userInfo(messageJson.getSenderID()).get("photo"), messageJson);
                                            //sendNotification(remoteMessage.getData());
                                            if (messageJson.getType().equals("0")) {
                                                db.addMessage(messageJson.getMessageText(), messageJson.getSenderID()
                                                        , messageJson.getSendDate(), messageJson.getUniqueID(), "received", "0");

                                                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Config.REQUEST_RECIEVED).putExtra("message", messageJson));

                                            } else if (messageJson.getType().equals("2")) {
                                                Date date = new Date();
                                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

                                                File file = new File(Environment.getExternalStorageDirectory() + "/MessageApp/images");
                                                file.mkdirs();
                                                File mypath = new File(file, "IMG-" + format.format(date) + ".png");
                                                db.addMessage(mypath.getAbsolutePath(), messageJson.getSenderID()
                                                        , messageJson.getSendDate(), messageJson.getUniqueID(), "received", "2");
                                                // byte[] imageByteArray = org.apache.commons.codec.binary.Base64.decodeBase64(messageJson.getMessageText());


                                                byte[] decodedString = Base64.decode(messageJson.getMessageText(), Base64.DEFAULT);
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


                                                com.kerse.messageapp.model.Message receiverMessage = new com.kerse.messageapp.model.Message()
                                                        .setMessageText(mypath.getAbsolutePath())
                                                        .setReceiverID(messageJson.getReceiverID())
                                                        .setUniqueID(messageJson.getUniqueID())
                                                        .setSenderID(messageJson.getSenderID())
                                                        .setType(messageJson.getType())
                                                        .setStatus(messageJson.getStatus())
                                                        .setSendDate(messageJson.getSendDate())
                                                        .setStripe(messageJson.getStripe());
                                                // messageJson.setMessageText(mypath.getAbsolutePath());
                                                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Config.REQUEST_RECIEVED).putExtra("message", receiverMessage));

                                            }


                                            com.kerse.messageapp.model.Message received = new com.kerse.messageapp.model.Message();
                                            received.setUniqueID(messageJson.getUniqueID());
                                            received.setSenderToken(preferences.token().get());
                                            received.setReceiverID(messageJson.getSenderID());
                                            received.setStatus("2");
                                            received.setStripe("status");

                                            new ReceivedMessage(Config.ROOT_URL + "/message/sendmessagexmpp", received).execute();
                                        } else if (messageJson.getStripe().equals("typing")) {

                                            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Config.REQUEST_RECIEVED).putExtra("message", messageJson));

                                        } else if (messageJson.getStripe().equals("status")) {
                                            if (messageJson.getStatus().equals("2")) {
                                                db.updateMessageStatus(messageJson.getUniqueID(), messageJson.getStatus());
                                            } else if (messageJson.getStatus().equals("3")) {
                                                ObjectMapper om = new ObjectMapper();
                                                List<String> list = new ArrayList<String>();
                                                try {
                                                    list = Arrays.asList(om.readValue(messageJson.getMessageText(), String[].class));
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }

                                                for (String id : list) {
                                                    db.updateMessageStatus(id, messageJson.getStatus());
                                                }
                                            }
                                            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Config.REQUEST_RECIEVED).putExtra("message", messageJson));

                                        }

                                    }


                                }
                            });

                            Log.w("app", chat.toString());
                        }
                    });
            chat_created = false;

            if (isToasted)

                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub

                    }
                });
        }
    }

  /*  void sendNotification(com.kerse.messageapp.model.Message data, Bitmap photo) {

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        long[] pattern = {500, 500, 500, 500};//Titreşim ayarı
        SqliteDatabase db = new SqliteDatabase(context);
        String name = db.userInfo(data.getSenderID()).get("fullname");

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.msg_icon)
                .setLargeIcon(photo)
                .setContentTitle(name)
                .setContentText(data.getMessageText())
                .setAutoCancel(true)
                .setVibrate(pattern)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        try {
            Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + context.getPackageName() + "/raw/notification");
            Ringtone r = RingtoneManager.getRingtone(context, alarmSound);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }

        notificationManager.notify(0 , notificationBuilder.build());
    }*/

    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;

    }


    public class generatePictureStyleNotification extends AsyncTask<String, Void, Bitmap> {

        private Context mContext;
        private String imageUrl;
        private com.kerse.messageapp.model.Message data;

        public generatePictureStyleNotification(Context context, com.kerse.messageapp.model.Message data, String imageUrl) {
            super();
            this.mContext = context;
            this.imageUrl = imageUrl;
            this.data = data;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            InputStream in;
            try {
                URL url = new URL(Config.ROOT_URL+this.imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                in = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(in);
                Bitmap lastBitmap = getCroppedBitmap(myBitmap);
                return lastBitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            Preferences_ preferences = new Preferences_(mContext);
            SqliteDatabase db = new SqliteDatabase(mContext);
            HashMap<String, String> userMap = db.userInfo(data.getSenderID());
            CustomUser customUser = new CustomUser()
                    .setProfileName(userMap.get("fullname"))
                    .setUniqueID(userMap.get("uniqueid"))
                    .setProfilePhoto(userMap.get("photo"));

            Intent intent = new Intent(context, MessagingActivity_.class);
            intent.putExtra("mainAndMessageStop", true);
            intent.putExtra("user", customUser);
            intent.putExtra("token", preferences.token().get());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            long[] pattern = {500, 500, 500, 500};//Titreşim ayarı

            String name = db.userInfo(data.getSenderID()).get("fullname");

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext)
                    .setSmallIcon(R.drawable.msg_icon)
                    .setLargeIcon(result)
                    .setContentTitle(name)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setContentText(data.getMessageText())
                    .setAutoCancel(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                    .setVibrate(pattern)
                    .setContentIntent(pendingIntent);
            if(data.getType().equals("2"))
                notificationBuilder.setContentText("\uD83D\uDCF7"+" Fotoğraf");
            NotificationManager notificationManager =
                    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
          //notificationBuilder.setSound(Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.rt ));

            MediaPlayer mMediaPlayer = new MediaPlayer();
            mMediaPlayer = MediaPlayer.create(mContext, R.raw.not);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setLooping(false);
            mMediaPlayer.start();

            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        }
    }

    public class ReceivedMessage extends AsyncTask<Void, Void, Void> {
        private String url;
        private com.kerse.messageapp.model.Message message;

        public ReceivedMessage(String url, com.kerse.messageapp.model.Message message) {
            this.message = message;
            this.url = url;
        }

        @Override
        protected Void doInBackground(Void... params) {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            restTemplate.postForObject(url, message, ServerResponse.class);
            return null;
        }
    }
}
