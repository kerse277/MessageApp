package com.kerse.messageapp.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kerse on 27.11.2016.
 */
@Accessors(chain = true)
public class UserListModel {

    @Getter
    @Setter
    private String token;

    @Getter
    @Setter
    private List<String> contact = new ArrayList<>();
}
