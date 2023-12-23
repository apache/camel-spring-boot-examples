/*
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.apache.camel.example.springboot.numbers.mainrouter.config;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.dynamicrouter.control.DynamicRouterControlMessage;
import org.apache.camel.example.springboot.numbers.mainrouter.model.StateMachineEvent;
import org.apache.camel.example.springboot.numbers.mainrouter.util.MainRouterUtil.Events;
import org.apache.camel.impl.engine.DefaultExecutorServiceManager;
import org.apache.camel.support.DefaultThreadPoolFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.COMPONENT_SCHEME_CONTROL;
import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.CONTROL_ACTION_LIST;
import static org.apache.camel.component.dynamicrouter.control.DynamicRouterControlConstants.CONTROL_ACTION_SUBSCRIBE;
import static org.apache.camel.component.dynamicrouter.routing.DynamicRouterConstants.COMPONENT_SCHEME_ROUTING;
import static org.apache.camel.example.springboot.numbers.common.util.NumbersCommonUtil.ENDPOINT_DIRECT_COMMAND;
import static org.apache.camel.example.springboot.numbers.common.util.NumbersCommonUtil.ENDPOINT_DIRECT_LIST;
import static org.apache.camel.example.springboot.numbers.mainrouter.util.MainRouterUtil.Events.PARTICIPANT_SUBSCRIBED;

/**
 * This configuration ingests the config properties in the application.yml file.
 * Sets up the Camel route that feeds the Dynamic Router.
 */
@Configuration
@EnableConfigurationProperties(DynamicRouterComponentConfig.class)
public class MainRouterConfig {

    /**
     * Holds the config exchange.properties.
     */
    private final DynamicRouterComponentConfig dynamicRouterComponentConfig;

    /**
     * URI for sending command messages.
     */
    private final String commandUri;

    /**
     * URI for the dynamic router to send messages to.
     */
    private final String subscribeUri;

    /**
     * The Camel context.
     */
    private final CamelContext camelContext;

    /**
     * For publishing  events.
     */
    final ApplicationEventPublisher eventPublisher;

    /**
     * Create this config with the config properties object.
     *
     * @param dynamicRouterComponentConfig the config properties object
     * @param camelContext the Camel context
     */
    public MainRouterConfig(final DynamicRouterComponentConfig dynamicRouterComponentConfig,
                            @Value("${number-generator.command-uri}") String commandUri,
                            @Value("${number-generator.subscribe-uri}") String subscribeUri,
                            final CamelContext camelContext,
                            final ApplicationEventPublisher eventPublisher) {
        this.dynamicRouterComponentConfig = dynamicRouterComponentConfig;
        this.commandUri = commandUri;
        this.subscribeUri = subscribeUri;
        this.camelContext = camelContext;
        this.eventPublisher = eventPublisher;
    }

    private static final Function<DynamicRouterComponentConfig, String> createCommandUri = cfg ->
            "%s:%s?recipientMode=%s&executorService=%s".formatted(
                    COMPONENT_SCHEME_ROUTING,
                    cfg.routingChannel(),
                    cfg.recipientMode(),
                    "customPool");

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper(new JsonFactory());
    }

    @Bean
    public ExecutorService customPool() {
        DefaultThreadPoolFactory threadPoolFactory = new DefaultThreadPoolFactory();
        threadPoolFactory.setCamelContext(camelContext);
        DefaultExecutorServiceManager executorServiceManager = new DefaultExecutorServiceManager(camelContext);
        executorServiceManager.setThreadPoolFactory(threadPoolFactory);
        return executorServiceManager.newCachedThreadPool(this, "DynamicRouterSpringBootThreadFactory");
    }

    /**
     * Creates a simple route to allow a producer to send messages through
     * the dynamic router on the routing channel.
     */
    @Bean
    RouteBuilder numbersRouter() {
        return new RouteBuilder(camelContext) {
            @Override
            public void configure() {
                from(commandUri).to(createCommandUri.apply(dynamicRouterComponentConfig));
            }
        };
    }

    /**
     * Creates a simple route to allow a producer to send messages through
     * the dynamic router on the routing channel.
     */
    @Bean
    RouteBuilder directCommandRouter() {
        return new RouteBuilder(camelContext) {
            @Override
            public void configure() {
                from(ENDPOINT_DIRECT_COMMAND).to(createCommandUri.apply(dynamicRouterComponentConfig));
            }
        };
    }

    /**
     * Creates a simple route to allow a producer to send messages through
     * the dynamic router on the routing channel.
     */
    @Bean
    RouteBuilder listSubscribersRouter() {
        return new RouteBuilder(camelContext) {
            @Override
            public void configure() {
                from(ENDPOINT_DIRECT_LIST)
                        .toD(COMPONENT_SCHEME_CONTROL + ":" + CONTROL_ACTION_LIST +
                                "?subscribeChannel=${header.subscribeChannel}");
            }
        };
    }

    /**
     * Creates a simple route to allow dynamic routing participants to
     * subscribe or unsubscribe.
     */
    @Bean
    RouteBuilder subscriptionRouter() {
        return new RouteBuilder(camelContext) {
            @Override
            public void configure() {
                from(subscribeUri)
                        .log(LoggingLevel.INFO, MainRouterConfig.class.getCanonicalName(), "Processing subscription: ${body}")
                        .unmarshal().json(DynamicRouterControlMessage.class)
                        .process(exchange -> {
                            Message<Events> message = MessageBuilder.withPayload(PARTICIPANT_SUBSCRIBED).build();
                            eventPublisher.publishEvent(new StateMachineEvent(this, message));
                        })
                        .to(COMPONENT_SCHEME_CONTROL + ":" + CONTROL_ACTION_SUBSCRIBE);
            }
        };
    }
}
