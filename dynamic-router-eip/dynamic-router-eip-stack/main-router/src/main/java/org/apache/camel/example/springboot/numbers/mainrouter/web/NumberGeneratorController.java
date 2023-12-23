package org.apache.camel.example.springboot.numbers.mainrouter.web;

import org.apache.camel.example.springboot.numbers.mainrouter.util.MainRouterUtil.Events;
import org.apache.camel.example.springboot.numbers.mainrouter.util.MainRouterUtil.States;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static org.apache.camel.example.springboot.numbers.common.util.NumbersCommonUtil.HEADER_EVENT_LIMIT;
import static org.apache.camel.example.springboot.numbers.mainrouter.util.MainRouterUtil.Events.GENERATE_NUMBERS_STARTED;
import static org.apache.camel.example.springboot.numbers.mainrouter.util.MainRouterUtil.sendEventAndMapResponse;

@RestController
public class NumberGeneratorController {

    final StateMachine<States, Events> stateMachine;

    public NumberGeneratorController(StateMachine<States, Events> stateMachine) {
        this.stateMachine = stateMachine;
    }

    @PutMapping(path = "/generate")
    public Mono<String> generate(@RequestParam("limit") int limit) {
        Message<Events> message = MessageBuilder.withPayload(GENERATE_NUMBERS_STARTED)
                .setHeader(HEADER_EVENT_LIMIT, limit)
                .build();
        return sendEventAndMapResponse.apply(stateMachine, message);
    }
}
