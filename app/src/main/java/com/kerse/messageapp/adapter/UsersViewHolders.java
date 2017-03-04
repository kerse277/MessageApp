package com.kerse.messageapp.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kerse.messageapp.R;
import com.kerse.messageapp.activity.MessagingActivity_;
import com.kerse.messageapp.config.Config;
import com.kerse.messageapp.model.CustomUser;
import com.squareup.picasso.Picasso;

import java.io.Serializable;

import lombok.Setter;

public class UsersViewHolders extends RecyclerView.ViewHolder {

    @Setter
    private CustomUser user;

    @Setter
    AppCompatActivity context;


    @Setter
    String token;

    public ImageView personPhoto, location;

    public TextView nameTxt, profileTxt;

    RelativeLayout txtLayout;


    public UsersViewHolders(final View itemView) {
        super(itemView);

        nameTxt = (TextView) itemView.findViewById(R.id.nameTxt);
        personPhoto = (ImageView) itemView.findViewById(R.id.personPhoto);
        profileTxt = (TextView) itemView.findViewById(R.id.userProfileText);
        txtLayout = (RelativeLayout) itemView.findViewById(R.id.friendLayout);
        location = (ImageView) itemView.findViewById(R.id.userLocation);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Config.MAIN_STOP));

                Intent i = new Intent(context, MessagingActivity_.class);
                i.putExtra("user", user);
                i.putExtra("token", token);
                i.putExtra("mainAndMessageStop", false);
                context.startActivity(i);
            }
        });

    }

}
