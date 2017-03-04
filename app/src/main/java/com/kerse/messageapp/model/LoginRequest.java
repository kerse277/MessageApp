package com.kerse.messageapp.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Created by kerse on 28.10.2016.
 */
@Accessors(chain = true)
public class LoginRequest {

    @Getter
    @Setter
    private String phone;

    @Getter
    @Setter
    private String password;


}
