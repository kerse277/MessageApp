package com.kerse.messageapp.repository;

import com.kerse.messageapp.config.Config;
import com.kerse.messageapp.model.Message;
import com.kerse.messageapp.model.ServerResponse;

import org.androidannotations.rest.spring.annotations.Body;
import org.androidannotations.rest.spring.annotations.Post;
import org.androidannotations.rest.spring.annotations.Rest;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * Created by kerse on 06.11.2016.
 */
@Rest(rootUrl = Config.ROOT_URL+"/message",converters = { MappingJackson2HttpMessageConverter.class })
public interface MessageRepository {

    @Post("/sendmessagexmpp")
    ServerResponse sendMessage(@Body Message message);

}
