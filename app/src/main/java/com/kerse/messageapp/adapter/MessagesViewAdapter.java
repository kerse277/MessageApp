package com.kerse.messageapp.adapter;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.kerse.messageapp.R;
import com.kerse.messageapp.config.Config;
import com.kerse.messageapp.model.CustomUser;
import com.kerse.messageapp.model.MessageListModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringEscapeUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import jp.wasabeef.picasso.transformations.MaskTransformation;

/**
 * Created by kerse on 03.12.2016.
 */

public class MessagesViewAdapter extends RecyclerView.Adapter<MessagesViewHolder> {

    private List<MessageListModel> itemList;
    private AppCompatActivity context;
    private String token;
    int position;


    public MessagesViewAdapter(AppCompatActivity context, List<MessageListModel> itemList, String token) {
        this.itemList = itemList;
        this.context = context;
        this.token = token;
    }

    @Override
    public MessagesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_listrow, null);

        return new MessagesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MessagesViewHolder holder, final int position) {
        this.position = position;
        holder.setUser(itemList.get(position).getCustomUser());
        holder.setContext(context);
        holder.setToken(token);
       int badge = itemList.get(position).getBadge();

        if(badge==0) {
            holder.mBadge.setNumber(0);
            holder.mBadge.setVisibility(View.GONE);
        }else {
            holder.mBadge.setNumber(badge);
            holder.mBadge.setVisibility(View.VISIBLE);

        }

        WindowManager wm = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        final int width = metrics.widthPixels;

        holder.txtLayout.getLayoutParams().height = width / 14 * 2;
        Picasso.with(context)
                .load(Config.ROOT_URL+itemList.get(position).getCustomUser().getProfilePhoto())
                .resize(width / 14 * 2, width / 14 * 2)
                .centerCrop()
                .transform(new MaskTransformation(context, R.drawable.octagon_mask))
                .into(holder.personPhoto, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(context)
                                .load(R.drawable.refresh)
                                .resize(width / 14 * 2, width / 14 * 2)
                                .centerCrop()
                                .transform(new MaskTransformation(context, R.drawable.octagon_mask))
                                .into(holder.personPhoto);
                    }
                });
        holder.nameTxt.setText(itemList.get(position).getCustomUser().getProfileName());
        if(itemList.get(position).getMessage().getType().equals("0")||itemList.get(position).getMessage().getType().equals("1"))
            holder.lastMessage.setText(StringEscapeUtils.unescapeJava(itemList.get(position).getMessage().getMessageText()));
        if(itemList.get(position).getMessage().getType().equals("2")||itemList.get(position).getMessage().getType().equals("3"))
            holder.lastMessage.setText(StringEscapeUtils.unescapeJava("\uD83D\uDCF7")+" Fotoğraf");

        Date date = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            date = format.parse(itemList.get(position).getMessage().getSendDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String hour = String.valueOf(date.getHours()), minute = String.valueOf(date.getMinutes());
        if (String.valueOf(date.getHours()).length() < 2) {
            hour = "0" + String.valueOf(date.getHours());
        }
        if (String.valueOf(date.getMinutes()).length() < 2) {
            minute = "0" + String.valueOf(date.getMinutes());
        }

        holder.lastMessageDate.setText(hour + ":" + minute);
    }


    @Override
    public int getItemCount() {
        return (null != itemList ? itemList.size() : 0);
    }
}