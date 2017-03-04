package com.kerse.messageapp.fcm;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.kerse.messageapp.activity.MainActivity;
import com.kerse.messageapp.R;
import com.kerse.messageapp.config.Config;
import com.kerse.messageapp.database.SqliteDatabase;
import com.kerse.messageapp.extra.Preferences;
import com.kerse.messageapp.extra.Preferences_;
import com.kerse.messageapp.model.Message;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.UiThread;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData().size() > 0) {
            Preferences_ preferences = new Preferences_(this);
            SqliteDatabase db = new SqliteDatabase(getApplicationContext());

            if (!preferences.currentUserId().get().equals(remoteMessage.getData().get("senderID")))
                getBitmapFromURL(db.userInfo(remoteMessage.getData().get("senderID")).get("photo"),remoteMessage.getData());
                //sendNotification(remoteMessage.getData());


            db.addMessage(remoteMessage.getData().get("messageText"), remoteMessage.getData().get("senderID")
                    , remoteMessage.getData().get("sendDate"), remoteMessage.getData().get("uniqueID"), "", "0");
            Message message = new Message()
                    .setMessageText(StringEscapeUtils.unescapeJava(remoteMessage.getData().get("messageText")))
                    .setSenderID(remoteMessage.getData().get("senderID"))
                    .setSendDate(remoteMessage.getData().get("sendDate"))
                    .setUniqueID(remoteMessage.getData().get("uniqueID"))
                    .setStatus("")
                    .setType("0");

            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(Config.REQUEST_RECIEVED).putExtra("message", message));
        }


    }


    void sendNotification(Map<String, String> data,Bitmap photo) {

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        long[] pattern = {500, 500, 500, 500};//Titreşim ayarı
        SqliteDatabase db = new SqliteDatabase(this);
        String name = db.userInfo(data.get("senderID")).get("fullname");

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.msg_icon)
                .setLargeIcon(photo)
                .setContentTitle(name)
                .setContentText(StringEscapeUtils.unescapeJava(data.get("messageText")))
                .setAutoCancel(true)
                .setVibrate(pattern)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        try {
            Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + this.getPackageName() + "/raw/notification");
            Ringtone r = RingtoneManager.getRingtone(this, alarmSound);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
    public  Bitmap getBitmapFromURL(String src,Map<String,String> data) {
        try {

            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            Bitmap lastBitmap = getCroppedBitmap(myBitmap);
            sendNotification(data,lastBitmap);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }
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

    }