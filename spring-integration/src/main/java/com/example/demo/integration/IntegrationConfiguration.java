package com.example.demo.integration;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.MessageHandlerSpec;
import org.springframework.integration.http.HttpHeaders;
import org.springframework.integration.http.dsl.Http;
import org.springframework.messaging.MessageChannel;


@Configuration
public class IntegrationConfiguration {

    // curl http://localhost:8080/tasks --data '{"username":"xyz","password":"xyz"}' -H 'Content-type: application/json'
    @Bean
    MessageChannel directChannel() {
        return MessageChannels.direct().get();
    }

    @Bean
    public IntegrationFlow httpGateway() {
        return IntegrationFlows.from(
            Http.inboundGateway("/tasks")
                .requestMapping(m -> m.methods(HttpMethod.POST))
                .requestPayloadType(String.class)
                .requestChannel(directChannel())
               // .replyChannel("httpResponse")
            .get()
        )
            .transform(t -> {
                return "transofrm " + t;
            })
            .channel("queueChannel")
            .get();
    }

    @MessagingGateway(defaultRequestChannel = "httpResponse")
    public interface MyGateway {

        String sendReceive(String in);

    }

    @Bean
    public IntegrationFlow handleMessage(MyGateway gateway) {
        return IntegrationFlows.from("queueChannel")
            .wireTap(flow -> flow.handle(System.out::println))
            .publishSubscribeChannel(publisher ->
                publisher.subscribe(flow -> flow.handle(m -> {
                    System.out.println("subscribed " + m.getPayload());
                }))
            )
            .enrichHeaders( c -> c.header(HttpHeaders.STATUS_CODE, HttpStatus.OK))
            .get();
    }

}
