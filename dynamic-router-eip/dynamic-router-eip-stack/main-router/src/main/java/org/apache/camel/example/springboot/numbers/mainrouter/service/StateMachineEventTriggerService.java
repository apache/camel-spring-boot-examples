package org.apache.camel.example.springboot.numbers.mainrouter.service;

import org.apache.camel.example.springboot.numbers.mainrouter.model.StateMachineEvent;
import org.apache.camel.example.springboot.numbers.mainrouter.util.MainRouterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;

@Service
public class StateMachineEventTriggerService {

    private static final Logger LOG = LoggerFactory.getLogger(StateMachineEventTriggerService.class);

    private final StateMachine<MainRouterUtil.States, MainRouterUtil.Events> stateMachine;

    public StateMachineEventTriggerService(StateMachine<MainRouterUtil.States, MainRouterUtil.Events> stateMachine) {
        this.stateMachine = stateMachine;
    }

    @EventListener
    public void onEvent(StateMachineEvent event) {
        LOG.info("Received state machine event: {}", event.getMessage().getPayload().name());
        MainRouterUtil.sendEventAndMapResponse.apply(stateMachine, event.getMessage()).subscribe();
    }
}
