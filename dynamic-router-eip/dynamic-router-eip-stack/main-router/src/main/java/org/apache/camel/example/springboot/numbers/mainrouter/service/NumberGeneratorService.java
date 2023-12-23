package org.apache.camel.example.springboot.numbers.mainrouter.service;

import jakarta.validation.constraints.NotNull;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.example.springboot.numbers.mainrouter.model.StateMachineEvent;
import org.apache.camel.example.springboot.numbers.mainrouter.util.MainRouterUtil;
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
import static org.apache.camel.example.springboot.numbers.mainrouter.util.MainRouterUtil.Events.GENERATE_NUMBERS_COMPLETE;

@Service
public class NumberGeneratorService {

    private static final Logger LOG = LoggerFactory.getLogger(NumberGeneratorService.class);

    private final ProducerTemplate producerTemplate;

    private final ApplicationEventPublisher eventPublisher;

    public NumberGeneratorService(@NotNull final ProducerTemplate producerTemplate,
                                  ApplicationEventPublisher eventPublisher) {
        this.producerTemplate = producerTemplate;
        this.eventPublisher = eventPublisher;
        producerTemplate.start();
    }

    /**
     * When a command has been received to generate numbers, this will continuously generate
     * numbers and send them in a command to have recipients process the numbers.  It will
     * only stop when a limit (if any) is reached, or if a subsequent command instructs
     * number message generation to stop
     *
     * @param limit the count of numbers to produce (zero means Integer.MAX_VALUE)
     */
    public void generateNumbers(int limit) {
        String msg;
        try {
            LOG.info("Generating numbers from 1 to {}", limit);
            Flux.range(1, limit)
                    .flatMap(n -> Mono.just(n)
                            .map(Object::toString)
                            .subscribeOn(Schedulers.boundedElastic())
                            .doOnNext(strN -> producerTemplate.sendBodyAndHeaders(ENDPOINT_DIRECT_COMMAND, strN,
                                    Map.of(HEADER_COMMAND, COMMAND_PROCESS_NUMBER, HEADER_NUMBER, strN))))
                    .doFinally(x -> {
                        Message<MainRouterUtil.Events> message =
                                MessageBuilder.withPayload(GENERATE_NUMBERS_COMPLETE).build();
                        eventPublisher.publishEvent(new StateMachineEvent(this, message));
                    })
                    .subscribe();
        } catch (Exception e) {
            msg = String.format("Exception when trying to send number messages: %s", e.getMessage());
            LOG.warn(msg, e);
        }
    }
}
