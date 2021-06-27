package com.example.demo.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.http.HttpHeaders;
import org.springframework.integration.http.dsl.Http;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;

import java.util.Optional;
import java.util.concurrent.Executors;


@Configuration
public class IntegrationConfiguration {

    Logger log = LoggerFactory.getLogger(IntegrationConfiguration.class);

    @Bean
    MessageChannel directChannel() {
        return MessageChannels.direct().get();
    }

    @Bean
    public IntegrationFlow httpAsyncGateway() {
        return IntegrationFlows.from(
            Http.inboundGateway("/asyncTasks")
                .requestMapping(m -> m.methods(HttpMethod.POST))
                .requestPayloadType(Logon.class)
                .requestChannel(directChannel())
                .errorChannel("errorChannel")
                .get()
        )
            .channel("queueChannel")
            .get();
    }

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
                publisher.subscribe(flow ->
                        flow.channel(c -> c.executor(Executors.newCachedThreadPool()))
                        .channel("own"))
                        .get();
                publisher.subscribe(flow -> flow
                        .handle(m -> {
                            try {
                                sleepNoException(5000);
                                System.out.println("done");

                            } catch(Exception e) { e.printStackTrace();}
                            log.info("subscribed {}", m.getPayload());
                        })
                    ).get();
                }
            )
            .transform(t -> "")
            .wireTap(flow -> flow.handle(m -> {
                log.info("{}",m.getHeaders().get("status"));
            }))
            .enrichHeaders( c -> c.header(HttpHeaders.STATUS_CODE, HttpStatus.OK))
            .enrichHeaders( c -> c.header("custom", "all_good"))
            .get();
    }

    @Bean
    public IntegrationFlow outFlow() {
        return IntegrationFlows.from("own")
            .handle(
                Http.outboundGateway("http://localhost:8000")
                    .httpMethod(HttpMethod.POST)
                    //.errorHandler(new DefaultResponseErrorHandler())
                    .expectedResponseType(String.class)
                    .get()
            )
            .get();
    }

    @Bean
    IntegrationFlow exceptionOrErrorFlow() {
        return IntegrationFlows.from("errorChannel")
            .routeByException(r -> {
                r.channelMapping(IllegalArgumentException.class, "badRequest");
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
        return IntegrationFlows.from("badRequest")
            .wireTap(f -> f.handle(m -> {
                log.info("failed badly 3 headers: {}, payload: {}", m.getHeaders(), m.getPayload());
            }))
            .enrichHeaders(c -> c.header(HttpHeaders.STATUS_CODE, HttpStatus.BAD_REQUEST))
            .transform(t -> "failed " + t)
            .get();
    }

    private void sleepNoException(long millisecond) {
        try {
            Thread.sleep(millisecond);
        } catch(Exception e) {}
    }
}
