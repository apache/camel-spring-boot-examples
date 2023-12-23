package org.apache.camel.example.springboot.numbers.mainrouter.config;

import org.apache.camel.example.springboot.numbers.mainrouter.service.NumberGeneratorService;
import org.apache.camel.example.springboot.numbers.mainrouter.service.NumberStatisticsService;
import org.apache.camel.example.springboot.numbers.mainrouter.service.WarmUpService;
import org.apache.camel.example.springboot.numbers.mainrouter.util.MainRouterUtil.Events;
import org.apache.camel.example.springboot.numbers.mainrouter.util.MainRouterUtil.States;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.apache.camel.example.springboot.numbers.common.util.NumbersCommonUtil.HEADER_EVENT_LIMIT;

@Configuration
public class StateMachineActionConfig {

    private static final Logger LOG = LoggerFactory.getLogger(StateMachineActionConfig.class);

    private final WarmUpService warmUpService;

    private final NumberGeneratorService numberGeneratorService;

    private final NumberStatisticsService numberStatisticsService;

    public StateMachineActionConfig(WarmUpService warmUpService, NumberGeneratorService numberGeneratorService, NumberStatisticsService numberStatisticsService) {
        this.warmUpService = warmUpService;
        this.numberGeneratorService = numberGeneratorService;
        this.numberStatisticsService = numberStatisticsService;
    }

    @Bean
    public StateMachineListener<States, Events> listener() {
        return new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<States, Events> from, State<States, Events> to) {
                LOG.info("State changed from '{}' to '{}'",
                        from == null ? "none" : from.getId(),
                        to.getId());
            }
        };
    }

    @Bean
    public Action<States, Events> warmUpAction() {
        return context -> {
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.schedule(warmUpService::doWarmUp, 2, TimeUnit.SECONDS);
            executor.shutdown();
        };
    }

    @Bean
    public Action<States, Events> generateNumbersAction() {
        return context -> {
            int limit = (int) context.getMessageHeader(HEADER_EVENT_LIMIT);
            numberGeneratorService.generateNumbers(limit);
        };
    }

    @Bean
    public Action<States, Events> resetStatisticsAction() {
        return context -> numberStatisticsService.resetStats();
    }
}
