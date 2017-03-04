package com.kerse.messageapp.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Accessors(chain = true)
public class RegisterUserModel {

    @Getter
    @Setter
    private User user;

    @Getter
    @Setter
    private List<String> contact = new ArrayList<>();

}
