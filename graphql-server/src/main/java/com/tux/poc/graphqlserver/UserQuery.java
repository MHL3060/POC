package com.tux.poc.graphqlserver;

import com.tux.poc.model.User;
import graphql.kickstart.tools.GraphQLQueryResolver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class UserQuery implements GraphQLQueryResolver {

    public CompletableFuture<User> findUser(String username) {
        var stopWatch = StopWatch.createStarted();
        var complete = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(10000);
            } catch (Exception e) {
            }

            return User.builder()
                .firstName("first")
                .lastName("last")
                .username("userName")
                .lastUpdated(OffsetDateTime.now())
                .build();
        });

        stopWatch.stop();
        log.info("time used={}", stopWatch.getTime());
        return complete;
    }
}
