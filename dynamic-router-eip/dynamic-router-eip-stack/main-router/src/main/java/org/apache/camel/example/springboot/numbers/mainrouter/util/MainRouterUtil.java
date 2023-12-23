package org.apache.camel.example.springboot.numbers.mainrouter.util;

import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;

public class MainRouterUtil {

    public static final BiFunction<StateMachine<States, Events>, Message<Events>, Mono<String>> sendEventAndMapResponse = (sm, m) ->
            sm.sendEvent(Mono.just(m))
                    .next()
                    .map(er ->
                            switch (er.getResultType()) {
                                case ACCEPTED -> "Event accepted.";
                                case DENIED -> "Event denied.";
                                case DEFERRED -> "Event deferred.";
                            });

    public enum States {
        STARTING("Starting"),
        INITIALIZING("Initializing"),
        READY("Ready"),
        GENERATING_NUMBERS("Generating Numbers");

        private final String description;

        States(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
    }

    public enum Events {
        PARTICIPANT_SUBSCRIBED("Routing Participant Subscribed"),
        INITIALIZATION_COMPLETE("Initialization Complete"),
        GENERATE_NUMBERS_STARTED("Generate Numbers Started"),
        GENERATE_NUMBERS_COMPLETE("Generate Numbers Complete");

        private final String description;

        Events(String description) {
            this.description = description;
        }
    }
}
