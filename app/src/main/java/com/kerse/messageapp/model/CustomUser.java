package com.kerse.messageapp.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class CustomUser implements Serializable{

    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private String uniqueID;

    @Getter
    @Setter
    private String phoneNumber;

    @Setter
    @Getter
    private String profileName;

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
    private String secretProfile;


}
