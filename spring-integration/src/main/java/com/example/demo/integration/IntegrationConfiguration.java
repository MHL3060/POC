package com.example.demo.integration;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.http.HttpHeaders;
import org.springframework.integration.http.dsl.Http;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.ErrorMessage;

import java.util.Optional;


@Configuration
@EnableIntegration
public class IntegrationConfiguration {

    Logger log = LoggerFactory.getLogger(IntegrationConfiguration.class);
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
                .requestPayloadType(Logon.class)
                .requestChannel(directChannel())
                .errorChannel("errorChannel")
                .get()
        )
            .channel("queueChannel")

            .get();
    }

   /* @Bean
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
    }*/

    @Bean
    public IntegrationFlow handleMessage() {
        return IntegrationFlows.from("queueChannel")
            .wireTap(flow -> flow.handle(m -> log.info("{}", m)))
            .routeToRecipients( r -> {
                r.recipientMessageSelector("errorChannel", m ->
                     Optional.ofNullable(m.getHeaders().get(MessageHeaders.CONTENT_TYPE))
                         .map(s -> s.toString())
                         .map(s -> !s.contains("json"))
                         .orElse(false));
                r.defaultOutputToParentFlow();
                })
            .publishSubscribeChannel(publisher -> {
                publisher.subscribe(flow -> flow
                        .handle(m -> {
                            if (m.getPayload().toString().contains("user")) {
                                throw new IllegalArgumentException("user found");
                            }
                            try {
                                Thread.sleep(1000);
                            } catch(Exception e) {}
                            log.info("subscribed {}", m.getPayload());
                        })
                    );
                }
            )
            .transform(t -> "")
            .wireTap(flow -> flow.handle(m -> {
                log.info("{}",m.getHeaders().get("status"));
            }))
            .enrichHeaders( c -> c.header(HttpHeaders.STATUS_CODE, HttpStatus.OK))
            .get();
    }

    @Bean
    public IntegrationFlow outFlow() {
        return IntegrationFlows.from("queueChannel")
            .handle(
                Http.outboundGateway("http://localhost:8000")
                .httpMethod(HttpMethod.POST)
                .expectedResponseType(String.class)
                .get()
            )
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
                log.info("failed badly");
            }))
            .enrichHeaders(c -> c.header(HttpHeaders.STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR))
            .transform(c -> c)
            .get();
    }

    @Bean
    IntegrationFlow exceptionOrErrorFlow3() {
        return IntegrationFlows.from("errorChannel3")
            .wireTap(f -> f.handle(m -> {
                log.info("failed badly 3 headers: {}, payload: {}", m.getHeaders(), m.getPayload());
            }))
            .enrichHeaders(c -> c.header(HttpHeaders.STATUS_CODE, HttpStatus.BAD_REQUEST))
            .transform(t -> "failed " + t)
            .get();
    }
}
