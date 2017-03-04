package com.kerse.messageapp.fragment;


import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.baoyz.widget.PullRefreshLayout;
import com.kerse.messageapp.R;
import com.kerse.messageapp.activity.MainActivity;
import com.kerse.messageapp.activity.ProgressActivity;
import com.kerse.messageapp.adapter.UsersViewAdapter;
import com.kerse.messageapp.database.SqliteDatabase;
import com.kerse.messageapp.extra.Preferences_;
import com.kerse.messageapp.model.CustomUser;
import com.kerse.messageapp.model.MessageListModel;
import com.kerse.messageapp.model.UserListModel;
import com.kerse.messageapp.repository.UserRepository;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.androidannotations.rest.spring.annotations.RestService;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@EFragment(R.layout.fragment_users)
public class UsersFragment extends Fragment {

    @RestService
    UserRepository userRepository;

    @ViewById(R.id.swipeRefreshLayout)
    PullRefreshLayout layout;

    @ViewById(R.id.usersRecycler)
    RecyclerView recyclerView;

    @Pref
    Preferences_ preferences;

    private static String[] PERMISSIONS_CONTACT = {Manifest.permission.READ_CONTACTS};
    private static final int REQUEST_CONTACTS = 1;
    private List<String> contactList = new ArrayList<>();
    String phone;

    @AfterViews
    void afterViews() {
        SqliteDatabase db = new SqliteDatabase(getContext());
        phone = db.meInfo().get("phone");
        layout.setRefreshStyle(PullRefreshLayout.STYLE_RING);


        if (((MainActivity) getActivity()).isNewLogin()) {
            getUsersBack(preferences.token().get());
        } else {
            List<CustomUser> customUsers = new ArrayList<>();
            List<String> usersLocations = new ArrayList<>();

            for (HashMap<String, String> hashMap : db.users()) {
                CustomUser customUser = new CustomUser()
                        .setProfileName(hashMap.get("fullname"))
                        .setUniqueID(hashMap.get("uniqueid"))
                        .setProfilePhoto(hashMap.get("photo"))
                        .setProfileText(hashMap.get("ptxt"));
                usersLocations.add(hashMap.get("location"));
                customUsers.add(customUser);
            }

            LinearLayoutManager lLayout = new LinearLayoutManager(getContext());

            Collections.sort(customUsers, new Comparator<CustomUser>() {
                public int compare(CustomUser o1, CustomUser o2) {
                    if (o1.getProfileName() == null || o2.getProfileName() == null)
                        return 0;

                    return o1.getProfileName().compareTo(o2.getProfileName());
                }
            });


            recyclerView.setHasFixedSize(true);

            recyclerView.setLayoutManager(lLayout);

            final UsersViewAdapter mAdapter = new UsersViewAdapter((MainActivity) getActivity(), customUsers, usersLocations, preferences.token().get());

            recyclerView.setAdapter(mAdapter);
        }

        layout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (ActivityCompat.checkSelfPermission(((MainActivity) getActivity()), Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(((MainActivity) getActivity()), PERMISSIONS_CONTACT, REQUEST_CONTACTS);
                    Toast.makeText(((MainActivity) getActivity()), "Uygulamanın Düzgün Çalışması için gerekli izinleri vermelisiniz.", Toast.LENGTH_LONG).show();
                    layout.setRefreshing(false);
                } else {

                    conctactBackground();
                }
            }
        });

    }

    @UiThread
    void getUsersPre() {

    }

    @Background
    void getUsersBack(String token) {

        getUsersPre();

        List<CustomUser> userList = Arrays.asList(userRepository.userList(token));

        getUsersPost(userList);
    }

    @UiThread
    void getUsersPost(List<CustomUser> customUsers) {
        List<String> userLocations = new ArrayList<>();
        LinearLayoutManager lLayout = new LinearLayoutManager(getContext());

        SqliteDatabase db = new SqliteDatabase(getContext());
        db.resetUsers();

        for (CustomUser customUser : customUsers) {
            if (!contactList.contains(customUser.getUniqueID())) {
                db.addUser(customUser.getProfileName(), customUser.getProfilePhoto(), customUser.getUniqueID(), customUser.getProfileText(), "sim");
                userLocations.add("sim");
            } else {
                db.addUser(customUser.getProfileName(), customUser.getProfilePhoto(), customUser.getUniqueID(), customUser.getProfileText(), "friend");
                userLocations.add("friend");
            }
        }
        Collections.sort(customUsers, new Comparator<CustomUser>() {
            public int compare(CustomUser o1, CustomUser o2) {
                if (o1.getProfileName() == null || o2.getProfileName() == null)
                    return 0;

                return o1.getProfileName().compareTo(o2.getProfileName());
            }
        });
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(lLayout);
        UsersViewAdapter mAdapter = new UsersViewAdapter((MainActivity) getActivity(), customUsers,userLocations, preferences.token().get());
        recyclerView.setAdapter(mAdapter);

        layout.setRefreshing(false);
    }

    @Background
    void conctactBackground() {
        ContentResolver contentResolver = ((MainActivity) getActivity()).getContentResolver();
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

        UserListModel userListModel = new UserListModel()
                .setContact(contactList)
                .setToken(preferences.token().get());
        reUserList(userListModel);
    }

    @Background
    void reUserList(UserListModel userListModel) {
        List<CustomUser> userList = Arrays.asList(userRepository.reUserList(userListModel));
        getUsersPost(userList);
    }


}