package org.apache.camel.example.springboot.numbers.mainrouter.service;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.example.springboot.numbers.mainrouter.model.StateMachineEvent;
import org.apache.camel.example.springboot.numbers.mainrouter.util.MainRouterUtil.Events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

import static org.apache.camel.example.springboot.numbers.common.util.NumbersCommonUtil.COMMAND_PROCESS_NUMBER;
import static org.apache.camel.example.springboot.numbers.common.util.NumbersCommonUtil.ENDPOINT_DIRECT_COMMAND;
import static org.apache.camel.example.springboot.numbers.common.util.NumbersCommonUtil.HEADER_COMMAND;
import static org.apache.camel.example.springboot.numbers.common.util.NumbersCommonUtil.HEADER_NUMBER;
import static org.apache.camel.example.springboot.numbers.mainrouter.util.MainRouterUtil.Events.INITIALIZATION_COMPLETE;

/**
 * This service sends a small number of messages through the system shortly after startup to
 * warm up the JVM so that we can see optimal results the first time we run a batch of
 * messages through the system.
 */
@Service
public class WarmUpService {

    private static final Logger LOG = LoggerFactory.getLogger(WarmUpService.class);

    private final ProducerTemplate producerTemplate;

    private final ApplicationEventPublisher eventPublisher;

    public WarmUpService(ProducerTemplate producerTemplate,
                         ApplicationEventPublisher eventPublisher) {
        this.producerTemplate = producerTemplate;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Sends messages for the warm-up.
     */
    public void doWarmUp() {
        LOG.info("Running warm-up...");
        Flux.range(1, 1000000)
                .flatMap(n -> Mono.just(n)
                        .subscribeOn(Schedulers.boundedElastic())
                        .map(String::valueOf)
                        .doOnNext(ns -> producerTemplate.sendBodyAndHeaders(ENDPOINT_DIRECT_COMMAND, 1,
                                Map.of(HEADER_COMMAND, COMMAND_PROCESS_NUMBER,
                                        HEADER_NUMBER, ns))))
                .doFinally(x -> {
                    LOG.info("Warm-up finished");
                    Message<Events> message = MessageBuilder.withPayload(INITIALIZATION_COMPLETE).build();
                    eventPublisher.publishEvent(new StateMachineEvent(this, message));
                })
                .subscribe();
    }
}
