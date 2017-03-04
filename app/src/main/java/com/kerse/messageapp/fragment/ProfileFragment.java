package com.kerse.messageapp.fragment;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kerse.messageapp.R;
import com.kerse.messageapp.activity.CropActivity_;
import com.kerse.messageapp.activity.MainActivity;
import com.kerse.messageapp.config.Config;
import com.kerse.messageapp.database.SqliteDatabase;
import com.kerse.messageapp.extra.Preferences_;
import com.kerse.messageapp.model.Image;
import com.kerse.messageapp.model.ServerResponse;
import com.kerse.messageapp.model.User;
import com.kerse.messageapp.repository.UserRepository;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.androidannotations.rest.spring.annotations.RestService;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import jp.wasabeef.picasso.transformations.MaskTransformation;

@EFragment(R.layout.fragment_profile)
public class ProfileFragment extends Fragment {

    @RestService
    UserRepository userRepository;

    @ViewById(R.id.profilePhoto)
    ImageView profilePhoto;

    @ViewById(R.id.updateProPhoto)
    ImageButton updateProPhoto;

    @ViewById(R.id.profileEdit)
    Button profileEdit;

    @ViewById(R.id.profileFullName)
    TextView profileFullName;

    @ViewById(R.id.profileStatus)
    TextView profileStatus;

    @ViewById(R.id.profilePhoneNumber)
    TextView profilePhoneNumber;

    @ViewById(R.id.profilePhotoProgress)
    ProgressBar profilePhotoProgress;

    @ViewById(R.id.editProgress)
    ProgressBar editProgress;

    @Pref
    Preferences_ preferences;

    @AfterViews
    void afterViews() {
        SqliteDatabase db = new SqliteDatabase((MainActivity) getActivity());
        HashMap<String, String> map = db.meInfo();

        WindowManager wm = (WindowManager) (getContext().getSystemService(Context.WINDOW_SERVICE));
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int height = metrics.heightPixels;

        Picasso.with(getContext())
                .load(Config.ROOT_URL+map.get("photo"))
                .transform(new MaskTransformation(getContext(), R.drawable.octagon_mask))
                .resize(height / 3, height / 3)
                .into(profilePhoto);

        profileFullName.setText(map.get("fullname"));
        profileStatus.setText(map.get("ptxt"));
        profilePhoneNumber.setText(map.get("phone"));


    }


    @Click(R.id.updateProPhoto)
    void click() {
        updatePhoto();
    }


    public void updatePhoto() {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("image/*");
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(chooseFile, "Choose a photo"), 1);
    }


    @OnActivityResult(value = 1)
    public void activityResult(int resultCode, Intent data) {
        if (data != null) {

            Bitmap bitmap = null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (bitmap != null) {
                bitmap.recycle();
            }
            InputStream stream;
            try {

                stream = getContext().getContentResolver().openInputStream(data.getData());
                bitmap = BitmapFactory.decodeStream(stream);


                bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);

                byte[] byteArray = baos.toByteArray();
                Image image = new Image()
                        .setBase64forImage(Base64.encodeToString(byteArray, Base64.DEFAULT))
                        .setToken(preferences.token().get());

                Intent intent = new Intent(getActivity(), CropActivity_.class);
                intent.putExtra("bitmap", Base64.encodeToString(byteArray, Base64.DEFAULT));
                startActivity(intent);
                //updateProfilePhoto(image);
                data = null;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }


    @Click(R.id.profileEdit)
    void updateProfile() {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.update_profile_dialog);

        Button update = (Button) dialog.findViewById(R.id.profileEditButton);
        final EditText name = (EditText) dialog.findViewById(R.id.profileEditName);
        final EditText status = (EditText) dialog.findViewById(R.id.profileEditStatus);
        name.setText(profileFullName.getText().toString());
        status.setText(profileStatus.getText().toString());
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name.getText().toString().trim().equalsIgnoreCase("")) {
                    name.setError("This field can not be blank");
                } else if (status.getText().toString().trim().equalsIgnoreCase("")) {
                    status.setError("This field can not be blank");
                } else {
                    if (isOnline()) {
                        User user = new User()
                                .setProfileName(name.getText().toString())
                                .setProfileText(status.getText().toString())
                                .setToken(preferences.token().get());
                        updateProfileBack(user);
                        dialog.dismiss();
                    } else
                        Toast.makeText(getActivity(), "Check your internet connection...", Toast.LENGTH_SHORT).show();
                }


            }
        });
        dialog.show();


    }

    @Background
    void updateProfileBack(User user) {
        updateProfileBackPre();
        User responseUser = userRepository.updateProfile(user);
        updateProfileBackPost(responseUser);
    }

    @UiThread
    void updateProfileBackPost(User user) {
        profileFullName.setText(user.getProfileName());
        profileStatus.setText(user.getProfileText());
        SqliteDatabase db = new SqliteDatabase((MainActivity) getActivity());
        db.resetMe();
        db.addMe(user.getProfileName(), user.getPhoneNumber(), user.getProfilePhoto(), user.getUniqueID(), user.getProfileText());
        profileEdit.setVisibility(View.VISIBLE);
        editProgress.setVisibility(View.GONE);
    }

    @UiThread
    void updateProfileBackPre() {
        profileEdit.setVisibility(View.GONE);
        editProgress.setVisibility(View.VISIBLE);
    }


    @Background
    void updateProfilePhoto(Image image) {
        upProPhotoPre();
        User user = userRepository.updateProfilePhoto(image);
        upProPhotoPost(user);
    }

    @UiThread
    void upProPhotoPre() {
        profilePhotoProgress.setVisibility(View.VISIBLE);
        updateProPhoto.setVisibility(View.GONE);
    }

    @UiThread
    void upProPhotoPost(User user) {
        SqliteDatabase db = new SqliteDatabase((MainActivity) getActivity());
        db.resetMe();
        db.addMe(user.getProfileName(), user.getPhoneNumber(), user.getProfilePhoto(), user.getUniqueID(), user.getProfileText());

        WindowManager wm = (WindowManager) (getContext().getSystemService(Context.WINDOW_SERVICE));
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int height = metrics.heightPixels;

        Picasso.with(getContext())
                .load(Config.ROOT_URL+user.getProfilePhoto())
                .transform(new MaskTransformation(getContext(), R.drawable.octagon_mask))
                .resize(height / 3, height / 3)
                .into(profilePhoto);

        profilePhotoProgress.setVisibility(View.GONE);
        updateProPhoto.setVisibility(View.VISIBLE);
    }

}
