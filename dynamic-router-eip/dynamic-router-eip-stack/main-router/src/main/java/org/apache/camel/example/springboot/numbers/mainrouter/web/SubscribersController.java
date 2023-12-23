package org.apache.camel.example.springboot.numbers.mainrouter.web;

import org.apache.camel.example.springboot.numbers.mainrouter.service.SubscribersService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class SubscribersController {

    private final SubscribersService subscribersService;

    public SubscribersController(SubscribersService subscribersService) {
        this.subscribersService = subscribersService;
    }

    @GetMapping(path = "/list/{channel}")
    public Mono<String> listSubscriptions(@PathVariable("channel") String channel) {
        return Mono.just(subscribersService.listSubscriptions(channel));
    }
}
