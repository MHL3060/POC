package com.example.demo.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.http.dsl.Http;
import org.springframework.messaging.MessageChannel;

@Configuration
public class SimpleIntegration {

    @Autowired
    MessageChannel directChannel;

    @Bean // curl http://localhost:8080/tasks --data '{"username":"xyz","password":"xyz"}' -H 'Content-type: application/json'
    public IntegrationFlow httpGateway() {
        return IntegrationFlows.from(
            Http.inboundGateway("/tasks")
                .requestMapping(m -> m.methods(HttpMethod.POST))
                .requestPayloadType(String.class)
                .get()
        )
            .transform(t -> {
                return "hello " + t;
            })
            .get();
    }
}
