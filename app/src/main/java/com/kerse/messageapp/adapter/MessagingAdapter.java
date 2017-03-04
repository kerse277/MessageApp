package com.kerse.messageapp.adapter;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;

import com.daasuu.bl.BubbleLayout;
import com.kerse.messageapp.R;
import com.kerse.messageapp.model.Image;
import com.kerse.messageapp.model.Message;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by kerse on 05.11.2016.
 */

public class MessagingAdapter extends SelectableAdapter<RecyclerView.ViewHolder> {

    HashMap<String, Message> mList;
    List<String> idList;
    Context context;
    private RightViewHolder.ClickListener clickListenerRight;
    private LeftViewHolder.ClickListener clickListenerLeft;
    private RightViewImgHolder.ClickListener clickListenerRightImg;
    private LeftViewImgHolder.ClickListener clickListenerLeftImg;

    public MessagingAdapter(HashMap<String, Message> list, List<String> idList, Context context, RightViewHolder.ClickListener clickListenerRight, LeftViewHolder.ClickListener clickListenerLeft,RightViewImgHolder.ClickListener clickListenerRightImg, LeftViewImgHolder.ClickListener clickListenerLeftImg) {
        this.mList = list;
        this.clickListenerRight = clickListenerRight;
        this.clickListenerLeft = clickListenerLeft;
        this.clickListenerRightImg = clickListenerRightImg;
        this.clickListenerLeftImg = clickListenerLeftImg;
        this.idList = idList;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case 0:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.left_messaging_row, parent, false);
                return new LeftViewHolder(view, clickListenerLeft);
            case 1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.right_messaging_row, parent, false);
                return new RightViewHolder(view, clickListenerRight);
            case 2:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.left_image_layout, parent, false);
                return new LeftViewImgHolder(view, clickListenerLeftImg);
            case 3:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.right_image_layout, parent, false);
                return new RightViewImgHolder(view, clickListenerRightImg);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message object = mList.get(idList.get(position));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        WindowManager wm = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;


        if (object != null) {
            Date date = null;
            switch (object.getType()) {
                case "0":

                    ((LeftViewHolder) holder).mTitlee.setText(StringEscapeUtils.unescapeJava(object.getMessageText()));
                    ((LeftViewHolder) holder).mTitlee.setEmojiconSize(height / 25);


                    try {
                        date = format.parse(object.getSendDate());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String hour2 = String.valueOf(date.getHours()), minute2 = String.valueOf(date.getMinutes());
                    if (hour2.length() < 2) {
                        hour2 = "0" + String.valueOf(date.getHours());
                    }
                    if (minute2.length() < 2) {
                        minute2 = "0" + String.valueOf(date.getMinutes());
                    }
                    ((LeftViewHolder) holder).time.setText(hour2 + ":" + minute2);
                    ((LeftViewHolder) holder).space.getLayoutParams().width = width / 3;
                    ((LeftViewHolder) holder).selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
                    break;
                case "1":

                    ((RightViewHolder) holder).mTitle.setText(StringEscapeUtils.unescapeJava(object.getMessageText()));
                    ((RightViewHolder) holder).mTitle.setEmojiconSize(height / 25);


                    try {
                        date = format.parse(object.getSendDate());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String hour = String.valueOf(date.getHours()), minute = String.valueOf(date.getMinutes());
                    if (hour.length() < 2) {
                        hour = "0" + hour;
                    }
                    if (minute.length() < 2) {
                        minute = "0" + minute;
                    }
                    ((RightViewHolder) holder).time.setText(hour + ":" + minute);
                    if (object.getStatus().equals("0")) {


                        ((RightViewHolder) holder).checkImage.setImageResource(R.drawable.time1);

                    } else if (object.getStatus().equals("1")) {
                        ((RightViewHolder) holder).checkImage.setImageResource(R.drawable.check1);

                    } else if (object.getStatus().equals("2")) {
                        ((RightViewHolder) holder).checkImage.setImageResource(R.drawable.check2);

                    } else if (object.getStatus().equals("3")) {
                        ((RightViewHolder) holder).checkImage.setImageResource(R.drawable.check3);

                    }
                    ((RightViewHolder) holder).space.getLayoutParams().width = width / 3;
                    ((RightViewHolder) holder).selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);


                    break;
                case "2":
                    File file = new File(object.getMessageText());
                    Picasso.with(context)
                            .load(file)
                            .resize(width/5*2,width/5*2)
                            .into(((LeftViewImgHolder) holder).img);

                    try {
                        date = format.parse(object.getSendDate());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String hour3 = String.valueOf(date.getHours()), minute3 = String.valueOf(date.getMinutes());
                    if (hour3.length() < 2) {
                        hour3 = "0" + hour3;
                    }
                    if (minute3.length() < 2) {
                        minute3 = "0" + minute3;
                    }
                    ((LeftViewImgHolder) holder).time.setText(hour3 + ":" + minute3);
                    ((LeftViewImgHolder) holder).space.getLayoutParams().width = width / 3;
                    ((LeftViewImgHolder) holder).selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
                    break;
                case "3":
                    File file2 = new File(object.getMessageText());
                    Picasso.with(context)
                            .load(file2)
                            .resize(width/5*2,width/5*2)
                            .into(((RightViewImgHolder) holder).img);

                    try {
                        date = format.parse(object.getSendDate());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String hour4 = String.valueOf(date.getHours()), minute4 = String.valueOf(date.getMinutes());
                    if (hour4.length() < 2) {
                        hour4 = "0" + hour4;
                    }
                    if (minute4.length() < 2) {
                        minute4 = "0" + minute4;
                    }
                    ((RightViewImgHolder) holder).time.setText(hour4 + ":" + minute4);
                    if (object.getStatus().equals("0")) {


                        ((RightViewImgHolder) holder).checkImage.setImageResource(R.drawable.time1);

                    } else if (object.getStatus().equals("1")) {
                        ((RightViewImgHolder) holder).checkImage.setImageResource(R.drawable.check1);

                    } else if (object.getStatus().equals("2")) {
                        ((RightViewImgHolder) holder).checkImage.setImageResource(R.drawable.check2);

                    } else if (object.getStatus().equals("3")) {
                        ((RightViewImgHolder) holder).checkImage.setImageResource(R.drawable.check3);

                    }
                    ((RightViewImgHolder) holder).space.getLayoutParams().width = width / 3;
                    ((RightViewImgHolder) holder).selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);


                    break;
            }
        }
    }


    @Override
    public int getItemCount() {
        if (mList == null)
            return 0;
        return mList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mList != null) {
            Message object = mList.get(idList.get(position));
            if (object != null) {
                return Integer.parseInt(object.getType());
            }
        }
        return 0;
    }

    public static class LeftViewHolder extends RecyclerView.ViewHolder {
        private EmojiconTextView mTitlee;
        private TextView time;
        private Space space;
        ImageView leftImg;
        private ClickListener listener;
        private TextView selectedOverlay;
        @Setter
        private int pos;


        public LeftViewHolder(View itemView, final ClickListener listener) {
            super(itemView);
            this.listener = listener;
            leftImg = (ImageView) itemView.findViewById(R.id.leftImg);
            mTitlee = (EmojiconTextView) itemView.findViewById(R.id.leftTv);
            time = (TextView) itemView.findViewById(R.id.leftTime);
            space = (Space) itemView.findViewById(R.id.space2);
            selectedOverlay = (TextView) itemView.findViewById(R.id.selectOver);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClicked(getAdapterPosition());
                    }

                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (listener != null) {
                        listener.onItemLongClicked(getAdapterPosition());
                    }
                    return false;
                }
            });

        }

        public interface ClickListener {
            public void onItemClicked(int position);

            public boolean onItemLongClicked(int position);
        }

    }

    public static class RightViewHolder extends RecyclerView.ViewHolder {
        private EmojiconTextView mTitle;
        private TextView time;
        private Space space;
        private ImageView checkImage, rightImg;
        private ClickListener listener;
        private TextView selectedOverlay;
        @Setter
        private int pos;

        public RightViewHolder(View itemView, final ClickListener listener) {
            super(itemView);
            this.listener = listener;
            selectedOverlay = (TextView) itemView.findViewById(R.id.selectOver);
            mTitle = (EmojiconTextView) itemView.findViewById(R.id.rightTv);
            time = (TextView) itemView.findViewById(R.id.rightTime);
            rightImg = (ImageView) itemView.findViewById(R.id.rightImg);
            space = (Space) itemView.findViewById(R.id.space);
            checkImage = (ImageView) itemView.findViewById(R.id.checkImage);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClicked(getAdapterPosition());
                    }

                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (listener != null) {
                        listener.onItemLongClicked(getAdapterPosition());
                    }
                    return false;
                }
            });

        }


        public interface ClickListener {
            public void onItemClicked(int position);

            public boolean onItemLongClicked(int position);
        }

    }


    public static class LeftViewImgHolder extends RecyclerView.ViewHolder {
        private ImageView img;
        private TextView time;
        private Space space;
        private ClickListener listener;
        private TextView selectedOverlay;
        @Setter
        private int pos;


        public LeftViewImgHolder(View itemView, final ClickListener listener) {
            super(itemView);
            this.listener = listener;
            img = (ImageView) itemView.findViewById(R.id.leftImg);
            time = (TextView) itemView.findViewById(R.id.leftTime);
            space = (Space) itemView.findViewById(R.id.space2);
            selectedOverlay = (TextView) itemView.findViewById(R.id.selectOver);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClicked(getAdapterPosition());
                    }

                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (listener != null) {
                        listener.onItemLongClicked(getAdapterPosition());
                    }
                    return false;
                }
            });

        }

        public interface ClickListener {
            public void onItemClicked(int position);

            public boolean onItemLongClicked(int position);
        }

    }

    public static class RightViewImgHolder extends RecyclerView.ViewHolder {
        private ImageView img;
        private TextView time;
        private Space space;
        private ImageView checkImage;
        private ClickListener listener;
        private TextView selectedOverlay;
        @Setter
        private int pos;

        public RightViewImgHolder(View itemView, final ClickListener listener) {
            super(itemView);
            this.listener = listener;
            selectedOverlay = (TextView) itemView.findViewById(R.id.selectOver);
            img = (ImageView) itemView.findViewById(R.id.rightImg);
            time = (TextView) itemView.findViewById(R.id.rightTime);
            space = (Space) itemView.findViewById(R.id.space);
            checkImage = (ImageView) itemView.findViewById(R.id.checkImage);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClicked(getAdapterPosition());
                    }

                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (listener != null) {
                        listener.onItemLongClicked(getAdapterPosition());
                    }
                    return false;
                }
            });

        }


        public interface ClickListener {
            public void onItemClicked(int position);

            public boolean onItemLongClicked(int position);
        }

    }


}