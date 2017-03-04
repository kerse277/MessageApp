package com.kerse.messageapp.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.kerse.messageapp.R;
import com.kerse.messageapp.database.SqliteDatabase;
import com.kerse.messageapp.extra.Preferences_;
import com.kerse.messageapp.model.Image;
import com.kerse.messageapp.model.User;
import com.kerse.messageapp.repository.UserRepository;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.androidannotations.rest.spring.annotations.RestService;

import java.io.ByteArrayOutputStream;

import jp.wasabeef.picasso.transformations.MaskTransformation;

@EActivity(R.layout.activity_crop)
public class CropActivity extends AppCompatActivity {

    @RestService
    UserRepository userRepository;

    @ViewById(R.id.cropImageView1)
    CropImageView cropImageView;

    @ViewById(R.id.cropProgress)
    ProgressBar cropProgress;

    @Extra("bitmap")
    String bitmap;

    @Pref
    Preferences_ preferences;

    Bitmap reRotate;

    @AfterViews
    void afterViews() {
        byte[] decodedString = Base64.decode(bitmap, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        reRotate = decodedByte;
        cropImageView.setImageBitmap(decodedByte);
        if (decodedByte.getWidth() < decodedByte.getHeight())
            cropImageView.setMaxCropResultSize(decodedByte.getWidth(), decodedByte.getWidth());
        if (decodedByte.getHeight() < decodedByte.getWidth())
            cropImageView.setMaxCropResultSize(decodedByte.getHeight(), decodedByte.getHeight());

    }
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    @Click(R.id.rotate)
    void rotate() {

        Matrix matrix = new Matrix();

        matrix.postRotate(90);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(reRotate, reRotate.getWidth(), reRotate.getHeight(), true);

        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        reRotate = rotatedBitmap;
        cropImageView.setImageBitmap(rotatedBitmap);
        if (rotatedBitmap.getWidth() < rotatedBitmap.getHeight())
            cropImageView.setMaxCropResultSize(rotatedBitmap.getWidth(), rotatedBitmap.getWidth());
        if (rotatedBitmap.getHeight() < rotatedBitmap.getWidth())
            cropImageView.setMaxCropResultSize(rotatedBitmap.getHeight(), rotatedBitmap.getHeight());
    }

    @Click(R.id.upload)
    void crop() {
        Bitmap cropped = cropImageView.getCroppedImage();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        cropped.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        byte[] byteArray = baos.toByteArray();
        Image image = new Image()
                .setBase64forImage(Base64.encodeToString(byteArray, Base64.DEFAULT))
                .setToken(preferences.token().get());
        if(isOnline())
        updateProfilePhoto(image);
        else
            Toast.makeText(this,"Check your internet connection...",Toast.LENGTH_SHORT).show();

       // Toast.makeText(this, String.valueOf(cropped.getWidth()) + " " + String.valueOf(cropped.getHeight()), Toast.LENGTH_LONG).show();
    }
    @Click(R.id.cancel)
    void cancel(){
        Intent i = new Intent(this,MainActivity_.class);
        i.putExtra("cancelPhoto","cancelPhoto");
        startActivity(i);
        finish();

    }


    @Background
    void updateProfilePhoto(Image image) {
        upProPhotoPre();
        User user = userRepository.updateProfilePhoto(image);
        upProPhotoPost(user);
    }

    @UiThread
    void upProPhotoPre() {
        cropProgress.setVisibility(View.VISIBLE);
    }

    @UiThread
    void upProPhotoPost(User user) {
        SqliteDatabase db = new SqliteDatabase(this);
        db.resetMe();
        db.addMe(user.getProfileName(),user.getPhoneNumber(),user.getProfilePhoto(),user.getUniqueID(),user.getProfileText());


        Intent i = new Intent(this,MainActivity_.class);
        i.putExtra("cancelPhoto","cancelPhoto");
        startActivity(i);
        finish();

        cropProgress.setVisibility(View.GONE);

    }
}
