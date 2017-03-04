package com.kerse.messageapp.adapter;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kerse.messageapp.R;
import com.kerse.messageapp.activity.MessagingActivity_;
import com.kerse.messageapp.config.Config;
import com.kerse.messageapp.model.CustomUser;
import com.nex3z.notificationbadge.NotificationBadge;

import lombok.Setter;

/**
 * Created by kerse on 03.12.2016.
 */

public class MessagesViewHolder extends RecyclerView.ViewHolder {

    @Setter
    private CustomUser user;

    @Setter
    AppCompatActivity context;

    @Setter
    int badge;

    @Setter
    String token;

    public ImageView personPhoto;

    public TextView nameTxt, lastMessage, lastMessageDate;

    RelativeLayout txtLayout;

    NotificationBadge mBadge;


    public MessagesViewHolder(final View itemView) {
        super(itemView);
        mBadge = (NotificationBadge) itemView.findViewById(R.id.badge);
        lastMessageDate = (TextView) itemView.findViewById(R.id.lastMessageDate);
        lastMessage = (TextView) itemView.findViewById(R.id.lastMessage);
        nameTxt = (TextView) itemView.findViewById(R.id.messagesNameTxt);
        personPhoto = (ImageView) itemView.findViewById(R.id.messagesPersonPhoto);
        txtLayout = (RelativeLayout) itemView.findViewById(R.id.messagesLayout);

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
