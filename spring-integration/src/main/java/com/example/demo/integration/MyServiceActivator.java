package com.example.demo.integration;

import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

/*
@Component
public class MyServiceActivator {

    @ServiceActivator(inputChannel = "queueChannel")
    public MessageHandler messageHandler() {
        MessageHandler handler = new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                System.out.println("message from Activactor " + message.getPayload());
            }
        };
        return handler;
    }
}
*/
