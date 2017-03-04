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
import com.kerse.messageapp.extra.CircleTransform;
import com.kerse.messageapp.model.CustomUser;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import jp.wasabeef.picasso.transformations.MaskTransformation;

public class UsersViewAdapter extends RecyclerView.Adapter<UsersViewHolders> {

    private List<CustomUser> itemList;
    private List<String> locationList;
    private AppCompatActivity context;
    private String token;
    int position;


    public UsersViewAdapter(AppCompatActivity context, List<CustomUser> itemList, List<String> userLocations, String token) {
        this.itemList = itemList;
        this.locationList = userLocations;
        this.context = context;
        this.token = token;
    }

    @Override
    public UsersViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_listrow, null);

        return new UsersViewHolders(view);
    }

    @Override
    public void onBindViewHolder(final UsersViewHolders holder, int position) {
        this.position = position;
        holder.setUser(itemList.get(position));
        holder.setContext(context);
        holder.setToken(token);
        WindowManager wm = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        final int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        holder.txtLayout.getLayoutParams().height = width / 14 * 2;
        Picasso.with(context)
                .load(Config.ROOT_URL+itemList.get(position).getProfilePhoto())
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
        holder.nameTxt.setText(itemList.get(position).getProfileName());
        holder.profileTxt.setText(itemList.get(position).getProfileText());
        if (locationList.get(position).equals("sim"))
            holder.location.setImageResource(R.drawable.sim);
        else
            holder.location.setImageResource(R.drawable.friend);

    }


    @Override
    public int getItemCount() {
        return (null != itemList ? itemList.size() : 0);
    }

}