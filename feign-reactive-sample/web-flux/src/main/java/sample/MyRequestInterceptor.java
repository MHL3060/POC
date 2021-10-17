package sample;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class MyRequestInterceptor implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        var request = serverWebExchange.getRequest();
        return webFilterChain.filter(serverWebExchange)
            .contextWrite(ctx -> ctx.put("requestHeaders", request.getHeaders()));
    }
}
