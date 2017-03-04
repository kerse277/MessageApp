package com.kerse.messageapp.extra;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.DefaultString;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref(value = SharedPref.Scope.UNIQUE)
public interface Preferences {

    @DefaultString("")
    String token();

    @DefaultString("")
    String uniqueId();

    @DefaultString("")
    String password();

    @DefaultBoolean(false)
    boolean isLogin();

    @DefaultString("")
    String currentUserId();
}