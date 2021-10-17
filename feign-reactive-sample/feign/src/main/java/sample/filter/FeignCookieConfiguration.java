package sample.filter;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import reactivefeign.client.ReactiveHttpRequestInterceptor;
import reactor.core.publisher.Mono;

import java.util.List;

public class FeignCookieConfiguration {


    @Bean
    public ReactiveHttpRequestInterceptor intercept() {
        return reactiveHttpRequest -> {
            return Mono.deferContextual(Mono::just)
                .map(ctx ->
                    ctx.getOrEmpty("requestHeaders")
                        .map(HttpHeaders.class::cast)
                        .map(HttpHeaders::entrySet)
                        .map(es -> {
                            es.stream()
                                .filter(e -> e.getKey().equals("cookie"))
                                .forEach(e -> {
                                    reactiveHttpRequest.headers().put(e.getKey(), e.getValue());
                                });
                            reactiveHttpRequest.headers().put("api-key", List.of("my-api-key"));
                            return reactiveHttpRequest;
                        })
                        .orElse(reactiveHttpRequest)
                );
        };
    }
}
