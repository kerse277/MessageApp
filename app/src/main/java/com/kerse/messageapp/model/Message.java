package com.kerse.messageapp.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Accessors(chain = true)
public class Message implements Serializable{

    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String uniqueID;

    @Getter
    @Setter
    private String messageText;

    @Getter
    @Setter
    private String senderToken;

    @Getter
    @Setter
    private String senderID;

    @Getter
    @Setter
    private String receiverID;

    @Getter
    @Setter
    private String sendDate;

    @Getter
    @Setter
    private String type;

    @Getter
    @Setter
    private String status;

    @Getter
    @Setter
    private String stripe;

}
