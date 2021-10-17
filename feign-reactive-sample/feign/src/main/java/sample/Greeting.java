package sample;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

//if you name feign client with eureka app name than you may omit ribbon configuration
@FeignClient(name = "web-flux-app")
public interface Greeting {

    @GetMapping("/greeting")
    String greeting();
}
