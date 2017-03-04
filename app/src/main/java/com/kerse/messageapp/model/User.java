package com.kerse.messageapp.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Accessors(chain = true)
public class User {

    @Setter
    @Getter
    private Long id;

    @Getter
    @Setter
    private String uniqueID;

    @Setter
    @Getter
    private String profileName;

    @Getter
    @Setter
    private String phoneNumber;

    @Getter
    @Setter
    private String password;

    @Getter
    @Setter
    private String deviceID;

    @Getter
    @Setter
    private String profilePhoto;

    @Getter
    @Setter
    private String profileText;

    @Getter
    @Setter
    private String status;

    @Getter
    @Setter
    private String token;

}
