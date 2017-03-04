package com.kerse.messageapp.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ToxicBakery.viewpager.transforms.ForegroundToBackgroundTransformer;
import com.digits.sdk.android.Digits;
import com.kerse.messageapp.R;
import com.kerse.messageapp.adapter.MainPagerAdapter;
import com.kerse.messageapp.config.Config;
import com.kerse.messageapp.extra.Preferences_;
import com.kerse.messageapp.repository.UserRepository;
import com.kerse.messageapp.xmpp.ConnectXmpp;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.KeyDown;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.androidannotations.rest.spring.annotations.RestService;


import lombok.Getter;
import lombok.Setter;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {


    @RestService
    UserRepository userRepository;


    @Extra("newLogin")
    @Getter
    @Setter
    boolean newLogin;

    @Pref
    Preferences_ preferences;

    @ViewById(R.id.pager)
    ViewPager viewPager;

    @ViewById(R.id.tab_layout)
    TabLayout tabLayout;

    @ViewById(R.id.toolbar)
    Toolbar toolbar;

    @Extra("mainStop")
    boolean mainStop = true;

    @Extra("mainAndMessageStop")
    boolean mainAndMessageStop = true;

    @Extra("cancelPhoto")
    String cancelPhoto;

    @AfterViews
    void afterView() {
        setSupportActionBar(toolbar);

        tabLayout.addTab(tabLayout.newTab().setText("Messages"));
        tabLayout.addTab(tabLayout.newTab().setText("Users"));
        tabLayout.addTab(tabLayout.newTab().setText("My Profile"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final MainPagerAdapter adapter = new MainPagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());

        viewPager.setAdapter(adapter);
        viewPager.setPageTransformer(true, new ForegroundToBackgroundTransformer());
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        viewPager.setOffscreenPageLimit(3);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {


            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        preferences.currentUserId().put("");

        if (cancelPhoto != null) {
            viewPager.setCurrentItem(2);
        }

        Intent i = new Intent(this, ConnectXmpp.class);
        stopService(i);
        startService(i);

    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    @KeyDown(KeyEvent.KEYCODE_TV_NETWORK)
    void vifi(){
        Toast.makeText(this,"asdasdasdasdasd",Toast.LENGTH_SHORT).show();
    }

    @Background
    void active() {
        if (isOnline())
            userRepository.active(preferences.token().get());
    }

    @Background
    void passive() {
        if (!isLogOut && isOnline())
            userRepository.passive(preferences.token().get());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mainStop && isOnline()) {
            passive();
            mainAndMessageStop = true;
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (mainStop && mainAndMessageStop && isOnline())
            active();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    boolean isLogOut = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                Toast.makeText(getApplicationContext(), "MessageApp", Toast.LENGTH_LONG).show();
                return true;
            case R.id.out:

                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(MainActivity.this);
                }
                builder.setTitle("Çıkış");
                builder.setIcon(R.drawable.exit);
                builder.setMessage("Çıkış yapmak istediğinize emin misiniz?");
                builder.setNegativeButton("İPTAL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {


                    }
                });


                builder.setPositiveButton("TAMAM", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(MainActivity.this, RegisterActivity_.class);
                        startActivity(intent);
                        passive();
                        isLogOut = true;
                        Digits.clearActiveSession();
                        preferences.clear();

                    }
                });


                builder.show();


                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocalBroadcastManager.getInstance(this).registerReceiver(mReciever, new IntentFilter(Config.MAIN_STOP));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        passive();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReciever);
    }

    BroadcastReceiver mReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Config.MAIN_STOP:
                    mainStop = false;
                    break;
                default:

            }
        }
    };


}
