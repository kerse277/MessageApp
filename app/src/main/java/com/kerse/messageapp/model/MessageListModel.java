package com.kerse.messageapp.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Created by kerse on 03.12.2016.
 */
@Accessors(chain = true)
public class MessageListModel {

    @Getter
    @Setter
    private Message message;

    @Getter
    @Setter
    private CustomUser customUser;

    @Getter
    @Setter
    private  int badge;

}
