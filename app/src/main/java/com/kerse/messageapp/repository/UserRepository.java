package com.kerse.messageapp.repository;

import android.widget.Toast;

import com.kerse.messageapp.config.Config;
import com.kerse.messageapp.model.CustomUser;
import com.kerse.messageapp.model.Image;
import com.kerse.messageapp.model.LoginRequest;
import com.kerse.messageapp.model.RegisterUserModel;
import com.kerse.messageapp.model.ServerResponse;
import com.kerse.messageapp.model.Token;
import com.kerse.messageapp.model.User;
import com.kerse.messageapp.model.UserListModel;

import org.androidannotations.rest.spring.annotations.Body;

import org.androidannotations.rest.spring.annotations.Get;
import org.androidannotations.rest.spring.annotations.Path;
import org.androidannotations.rest.spring.annotations.Post;
import org.androidannotations.rest.spring.annotations.Rest;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.HashMap;


@Rest(rootUrl = Config.ROOT_URL+"/user",converters = { MappingJackson2HttpMessageConverter.class })
public interface UserRepository {

    @Post("/adduser")
    User addUser(@Body RegisterUserModel registerUserModel);

    @Post("/login")
    Token login(@Body LoginRequest loginRequest);

    @Get("/userlist?token={token}")
    CustomUser[] userList(@Path String token);

    @Post("/userlist")
    CustomUser[] reUserList(@Body UserListModel userListModel);

    @Get("/active?token={token}")
    void active(@Path String token);

    @Get("/passive?token={token}")
    void passive(@Path String token);

    @Get("/getuserstatus?uniqueid={uniqueID}")
    ServerResponse getuserstatus(@Path String uniqueID);

    @Post("/updateprofilephoto")
    User updateProfilePhoto(@Body Image image);

    @Post("/updateprofile")
    User updateProfile(@Body User user);
}
