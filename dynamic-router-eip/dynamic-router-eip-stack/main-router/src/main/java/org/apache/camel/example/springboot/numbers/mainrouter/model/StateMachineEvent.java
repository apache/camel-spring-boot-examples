package org.apache.camel.example.springboot.numbers.mainrouter.model;

import org.apache.camel.example.springboot.numbers.mainrouter.util.MainRouterUtil.Events;
import org.springframework.context.ApplicationEvent;
import org.springframework.messaging.Message;

public class StateMachineEvent extends ApplicationEvent {

    private final transient Message<Events> message;

    public StateMachineEvent(Object source, Message<Events> message) {
        super(source);
        this.message = message;
    }

    public Message<Events> getMessage() {
        return message;
    }
}
