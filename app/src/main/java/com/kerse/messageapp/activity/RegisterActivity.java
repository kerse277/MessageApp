package com.kerse.messageapp.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.AuthConfig;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.kerse.messageapp.R;
import com.kerse.messageapp.extra.Preferences_;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.vicmikhailau.maskededittext.MaskedEditText;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import io.fabric.sdk.android.Fabric;


@EActivity(R.layout.activity_register)
public class RegisterActivity extends AppCompatActivity {

    private static final String TWITTER_KEY = "Q5Ijhc6S3zdk5NI4TGDOmEnTx";
    private static final String TWITTER_SECRET = "Jz9ezO1deO0tBG9EwnBIU1XhpVEyfN09YHEh7XMcjBFJHdW57l";
    private static String[] PERMISSIONS_CONTACT = {Manifest.permission.READ_CONTACTS};
    private static final int REQUEST_CONTACTS = 1;

    boolean checkEmpty = false;



    @ViewById(R.id.phoneRegister)
    MaskedEditText phone;

    @ViewById(R.id.name)
    EditText name;



    @Pref
    Preferences_ preferences;

    @AfterViews
    void afterViews() {
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new TwitterCore(authConfig), new Digits.Builder().build());

        Digits.Builder digitsBuilder = new Digits.Builder().withTheme(R.style.CustomDigitsTheme);
        Fabric.with(this, new TwitterCore(authConfig), digitsBuilder.build());

        if (preferences.isLogin().get()) {
            Intent i = new Intent(this, MainActivity_.class);
            i.putExtra("newLogin", false);
            startActivity(i);
            finish();
        }

        checkForContactsPermissions();

    }
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }


    private void checkForContactsPermissions() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_CONTACT, REQUEST_CONTACTS);
        } else {

        }

    }


    @Click(R.id.btnRegister)
    void register() {
        if(isOnline())
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_CONTACT, REQUEST_CONTACTS);
            Toast.makeText(this,"Uygulamanın Düzgün Çalışması için gerekli izinleri vermelisiniz.",Toast.LENGTH_LONG).show();
        } else {
            if (name.getText().toString().length() != 0 && phone.getText().toString().length() != 0) {
                checkEmpty = true;
            } else {
                Toast.makeText(getApplicationContext(), "Please Enter To Empty Area..", Toast.LENGTH_LONG).show();
                checkEmpty = false;
            }

            if (checkEmpty) {
             AuthConfig.Builder authConfigBuilder = new AuthConfig.Builder()
                        .withAuthCallBack(new AuthCallback() {
                            @Override
                            public void success(DigitsSession session, String phoneNumber) {
                                // TODO: associate the session userID with your user model
                             //   Toast.makeText(getApplicationContext(), "Authentication successful for "
                             //           + session.getAuthToken().secret, Toast.LENGTH_LONG).show();
                                toProgress(session.getAuthToken().secret);
                            }

                            @Override
                            public void failure(DigitsException exception) {
                                Log.d("Digits", "Sign in with Digits failure", exception);
                            }
                        })
                        .withPhoneNumber(phone.getText().toString());

                Digits.authenticate(authConfigBuilder.build());
              // toProgress("373639");


            }
        }
        else
            Toast.makeText(this,"Check your interner connection...",Toast.LENGTH_SHORT).show();

    }

    void toProgress(String password){
        Intent i = new Intent(this, ProgressActivity_.class);
        i.putExtra("name", name.getText().toString());
        i.putExtra("phone", phone.getText().toString());
        i.putExtra("password",password);
        startActivity(i);
        finish();
    }


}
