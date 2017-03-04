package com.kerse.messageapp.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.kerse.messageapp.R;
import com.kerse.messageapp.database.SqliteDatabase;
import com.kerse.messageapp.extra.Preferences_;
import com.kerse.messageapp.model.LoginRequest;
import com.kerse.messageapp.model.RegisterUserModel;
import com.kerse.messageapp.model.Token;
import com.kerse.messageapp.model.User;
import com.kerse.messageapp.repository.UserRepository;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.androidannotations.rest.spring.annotations.RestService;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_progress)
public class ProgressActivity extends AppCompatActivity {

    @RestService
    UserRepository userRepository;

    @Pref
    Preferences_ preferences;

    private List<String> contactList = new ArrayList<>();


    @Extra("name")
    String name;

    @Extra("phone")
    String phone;

    @Extra("password")
    String password;


    @AfterViews
    void afterViews() {
        SqliteDatabase db = new SqliteDatabase(this);
        if(db.meInfo().get("phone")!=null) {
            if (!db.meInfo().get("phone").equals(phone)) {
                db.resetMe();
                db.resetMessage();
                db.resetUsers();
            }
        }
        conctactBackground();
    }


    void registerUser() {

        User user = new User();


        user.setProfileName(name)
                .setPhoneNumber(phone)
                .setPassword(password);

        addUserBack(user);
    }

    @Background
    void addUserBack(User user) {
        RegisterUserModel registerUserModel = new RegisterUserModel()
                .setUser(user);

        registerUserModel.setContact(contactList);

        User user1 = userRepository.addUser(registerUserModel);

        addUserBackPost(user1);
    }


    @UiThread
    void addUserBackPost(User user) {
        if (user != null) {
            SqliteDatabase db = new SqliteDatabase(this);
            db.addMe(user.getProfileName(), user.getPhoneNumber(), user.getProfilePhoto(), user.getUniqueID(), user.getProfileText());

            login(user.getUniqueID());
        } else {
            Toast.makeText(getApplicationContext(), "invalid input...", Toast.LENGTH_SHORT).show();
        }

    }


    @Background
    void conctactBackground() {
        ContentResolver contentResolver = ProgressActivity.this.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null,
                null, null, null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

                    Cursor person_cursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (person_cursor.moveToNext()) {

                        String person_phoneNumber = person_cursor.getString(person_cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        String number = (person_phoneNumber.replace("+90", "0")).replace(" ", "");
                        if (!contactList.contains("+90(" + number.substring(1, 4) + ") " + number.substring(4, 7) + "-" + number.substring(7))
                                && !phone.equals("+90(" + number.substring(1, 4) + ") " + number.substring(4, 7) + "-" + number.substring(7))) {


                            String newNumber = "+90(" + number.substring(1, 4) + ") " + number.substring(4, 7) + "-" + number.substring(7);

                            contactList.add(newNumber);

                        }
                    }
                    person_cursor.close();
                }
            }
        }
        contactPostExecute();
    }

    @UiThread
    void contactPostExecute() {
        registerUser();

    }

    @Background
    void login(String uniqueId) {

        // String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        LoginRequest loginRequest = new LoginRequest()
                .setPhone(phone)
                .setPassword(password);

        Token token = userRepository.login(loginRequest);

        loginPostExecute(token,uniqueId);

    }

    @UiThread
    void loginPostExecute(Token token,String uniqueId) {
        if (!token.getToken().equals("empty")) {
            Intent i = new Intent(this, MainActivity_.class);
            i.putExtra("newLogin", true);
            preferences.token().put(token.getToken());
            preferences.password().put(password);
            preferences.uniqueId().put(uniqueId);
            preferences.isLogin().put(true);
            startActivity(i);
            finish();
        }else {
            Toast.makeText(getApplicationContext(),"Invalid input...",Toast.LENGTH_SHORT).show();
        }

    }

}
