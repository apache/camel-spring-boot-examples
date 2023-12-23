package org.apache.camel.example.springboot.numbers.mainrouter.config;

import org.apache.camel.example.springboot.numbers.mainrouter.util.MainRouterUtil.Events;
import org.apache.camel.example.springboot.numbers.mainrouter.util.MainRouterUtil.States;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;

import java.util.EnumSet;

@Configuration
@EnableStateMachine
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<States, Events> {

    private final StateMachineListener<States, Events> listener;

    private final Action<States, Events> warmUpAction;

    private final Action<States, Events> generateNumbersAction;

    private final Action<States, Events> resetStatisticsAction;

    private boolean initialized = false;

    public StateMachineConfig(StateMachineListener<States, Events> listener,
                              @Qualifier("warmUpAction") Action<States, Events> warmUpAction,
                              @Qualifier("generateNumbersAction") Action<States, Events> generateNumbersAction,
                              @Qualifier("resetStatisticsAction") Action<States, Events> resetStatisticsAction) {
        this.listener = listener;
        this.warmUpAction = warmUpAction;
        this.generateNumbersAction = generateNumbersAction;
        this.resetStatisticsAction = resetStatisticsAction;
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<States, Events> config) throws Exception {
        config
                .withConfiguration()
                .autoStartup(true)
                .listener(listener);
    }

    @Override
    public void configure(StateMachineStateConfigurer<States, Events> states) throws Exception {
        states
                .withStates()
                .initial(States.STARTING)
                .states(EnumSet.allOf(States.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<States, Events> transitions) throws Exception {
        // @formatter:off
        transitions
                .withExternal()
                    .source(States.STARTING)
                    .target(States.INITIALIZING)
                    .event(Events.PARTICIPANT_SUBSCRIBED)
                    .guard(context -> !initialized)
                    .action(context -> initialized = true)
                    .action(resetStatisticsAction)
                    .action(warmUpAction)
                    .and()
                .withExternal()
                    .source(States.INITIALIZING)
                    .target(States.READY)
                    .event(Events.INITIALIZATION_COMPLETE)
                    .action(resetStatisticsAction)
                    .and()
                .withExternal()
                    .source(States.READY)
                    .target(States.GENERATING_NUMBERS)
                    .event(Events.GENERATE_NUMBERS_STARTED)
                    .action(resetStatisticsAction)
                    .action(generateNumbersAction)
                    .and()
                .withExternal()
                    .source(States.GENERATING_NUMBERS)
                    .target(States.READY)
                    .event(Events.GENERATE_NUMBERS_COMPLETE);
        // @formatter:on
    }
}
