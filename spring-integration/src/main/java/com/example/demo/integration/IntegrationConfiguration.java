package com.example.demo.integration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.http.HttpHeaders;
import org.springframework.integration.http.dsl.Http;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;

import java.util.Optional;


@Configuration
@EnableIntegration
public class IntegrationConfiguration {

    // curl http://localhost:8080/tasks --data '{"username":"xyz","password":"xyz"}' -H 'Content-type: application/json'
    @Bean
    MessageChannel directChannel() {
        return MessageChannels.direct().get();
    }
    @Bean
    MessageChannel errorChannel() {
        return MessageChannels.direct().get();
    }

    @Bean
    public IntegrationFlow httpGateway() {
        return IntegrationFlows.from(
            Http.inboundGateway("/tasks")
                .requestMapping(m -> m.methods(HttpMethod.POST))
                .requestPayloadType(String.class)
                .requestChannel(directChannel())
                .errorChannel("errorChannel")
            .get()
        )
            .transform(t -> {
                return "transofrm " + t;
            })
            .channel("queueChannel")

            .get();
    }

    @Bean
    public IntegrationFlow handleMessage() {
        return IntegrationFlows.from("queueChannel")
            .wireTap(flow -> flow.handle(System.out::println))
            .routeToRecipients( r -> {
                r.recipientMessageSelector("errorChannel", m ->
                     Optional.ofNullable(m.getHeaders().get(MessageHeaders.CONTENT_TYPE))
                         .map(s -> s.toString())
                         .map(s -> !s.contains("json"))
                         .orElse(false));
                r.defaultOutputToParentFlow();
                })
            .publishSubscribeChannel(publisher -> {
                publisher.errorHandler(var1 -> {
                    var1.printStackTrace();
                })
                    .subscribe(flow -> flow
                        .handle(m -> {
                            if (m.getPayload().toString().contains("user")) {
                                throw new IllegalArgumentException("user found");
                            }
                            System.out.println("subscribed " + m.getPayload());
                        })
                    );
                }
            )
            .transform(t -> "")
            .wireTap(flow -> flow.handle(m -> {
                System.out.println(m.getHeaders().get("status"));
            }))
            .enrichHeaders( c -> c.header(HttpHeaders.STATUS_CODE, HttpStatus.OK))
            .get();
    }

    @Bean
    IntegrationFlow exceptionOrErrorFlow() {
        return IntegrationFlows.from("errorChannel")
            .routeByException(r -> {
                r.channelMapping(IllegalArgumentException.class, "errorChannel3");
                r.defaultOutputToParentFlow();
            })
            .wireTap(f -> f.handle(m -> {
                System.out.println("failed badly");
            }))
            .enrichHeaders(c -> c.header(HttpHeaders.STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR))
            .transform(c -> c)
            .get();
    }

    @Bean
    IntegrationFlow exceptionOrErrorFlow3() {
        return IntegrationFlows.from("errorChannel3")
            .wireTap(f -> f.handle(m -> {
                System.out.println("failed badly 3");
            }))
            .enrichHeaders(c -> c.header(HttpHeaders.STATUS_CODE, HttpStatus.BAD_REQUEST))
            .transform( t -> "failed")
            .get();
    }
}
