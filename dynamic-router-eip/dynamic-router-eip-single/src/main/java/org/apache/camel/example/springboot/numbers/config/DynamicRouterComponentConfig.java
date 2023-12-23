/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.example.springboot.numbers.config;

import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Predicate;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.example.springboot.numbers.participants.PredicateConstants;
import org.apache.camel.example.springboot.numbers.participants.RoutingParticipant;
import org.apache.camel.example.springboot.numbers.service.ResultsService;
import org.apache.camel.impl.engine.DefaultExecutorServiceManager;
import org.apache.camel.support.DefaultThreadPoolFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import static org.apache.camel.component.dynamicrouter.routing.DynamicRouterConstants.COMPONENT_SCHEME_ROUTING;
import static org.apache.camel.example.springboot.numbers.participants.PredicateConstants.PREDICATE_EIGHTS;
import static org.apache.camel.example.springboot.numbers.participants.PredicateConstants.PREDICATE_EVEN;
import static org.apache.camel.example.springboot.numbers.participants.PredicateConstants.PREDICATE_FIVES;
import static org.apache.camel.example.springboot.numbers.participants.PredicateConstants.PREDICATE_FOURS;
import static org.apache.camel.example.springboot.numbers.participants.PredicateConstants.PREDICATE_NINES;
import static org.apache.camel.example.springboot.numbers.participants.PredicateConstants.PREDICATE_ODD;
import static org.apache.camel.example.springboot.numbers.participants.PredicateConstants.PREDICATE_PRIMES;
import static org.apache.camel.example.springboot.numbers.participants.PredicateConstants.PREDICATE_SEVENS;
import static org.apache.camel.example.springboot.numbers.participants.PredicateConstants.PREDICATE_SIXES;
import static org.apache.camel.example.springboot.numbers.participants.PredicateConstants.PREDICATE_TENS;
import static org.apache.camel.example.springboot.numbers.participants.PredicateConstants.PREDICATE_THREES;

/**
 * This configuration ingests the config properties in the application.yml file.
 * Sets up the Camel route that feeds the Dynamic Router.  Also creates all
 * routing participants by using the various predicates defined in the
 * {@link PredicateConstants} class.
 */
@Configuration
@EnableConfigurationProperties(ExampleConfig.class)
public class DynamicRouterComponentConfig {

    /**
     * Holds the config properties.
     */
    private final ExampleConfig exampleConfig;

    /**
     * The Camel context.
     */
    private final CamelContext camelContext;

    /**
     * Create this config with the config properties object.
     *
     * @param exampleConfig the config properties object
     */
    public DynamicRouterComponentConfig(final ExampleConfig exampleConfig, CamelContext camelContext) {
        this.exampleConfig = exampleConfig;
        this.camelContext = camelContext;
    }

    private static final Function<ExampleConfig, String> createCommandUri = cfg ->
            "%s:%s?recipientMode=%s&executorService=%s".formatted(
                    COMPONENT_SCHEME_ROUTING,
                    cfg.getRoutingChannel(),
                    cfg.getRecipientMode(),
                    "customPool");

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
     * the dynamic router.
     */
    @Bean
    RouteBuilder myRouter() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from(exampleConfig.getStartUri()).to(createCommandUri.apply(exampleConfig));
            }
        };
    }

    @Bean
    Predicate predicateTens() {
        return PREDICATE_TENS;
    }

    @Bean
    Predicate predicateNines() {
        return PREDICATE_NINES;
    }

    @Bean
    Predicate predicateEights() {
        return PREDICATE_EIGHTS;
    }

    @Bean
    Predicate predicateSevens() {
        return PREDICATE_SEVENS;
    }

    @Bean
    Predicate predicateSixes() {
        return PREDICATE_SIXES;
    }

    @Bean
    Predicate predicateFives() {
        return PREDICATE_FIVES;
    }

    @Bean
    Predicate predicateFours() {
        return PREDICATE_FOURS;
    }

    @Bean
    Predicate predicateThrees() {
        return PREDICATE_THREES;
    }

    @Bean
    Predicate predicateEven() {
        return PREDICATE_EVEN;
    }

    @Bean
    Predicate predicateOdd() {
        return PREDICATE_ODD;
    }

    @Bean
    Predicate predicatePrimes() {
        return PREDICATE_PRIMES;
    }

    /**
     * Create a {@link RoutingParticipant} that handles messages where the body comprises a number that is
     * a multiple of 10.
     *
     * @param subscriberTemplate the {@link ConsumerTemplate} for subscribing to messages that match
     * @param resultsService     the service that handles the results of message processing
     * @return the configured {@link RoutingParticipant}
     */
    @Bean
    RoutingParticipant tensRoutingParticipant(final ProducerTemplate subscriberTemplate, final ResultsService resultsService) {
        return new RoutingParticipant(exampleConfig, "tens", 1, "predicateTens", subscriberTemplate, resultsService);
    }

    /**
     * Create a {@link RoutingParticipant} that handles messages where the body comprises a number that is
     * a multiple of 9.
     *
     * @param subscriberTemplate the {@link ConsumerTemplate} for subscribing to messages that match
     * @param resultsService     the service that handles the results of message processing
     * @return the configured {@link RoutingParticipant}
     */
    @Bean
    RoutingParticipant ninesRoutingParticipant(final ProducerTemplate subscriberTemplate, final ResultsService resultsService) {
        return new RoutingParticipant(exampleConfig, "nines", 2, "predicateNines", subscriberTemplate, resultsService);
    }

    /**
     * Create a {@link RoutingParticipant} that handles messages where the body comprises a number that is
     * a multiple of 8.
     *
     * @param subscriberTemplate the {@link ConsumerTemplate} for subscribing to messages that match
     * @param resultsService     the service that handles the results of message processing
     * @return the configured {@link RoutingParticipant}
     */
    @Bean
    RoutingParticipant eightsRoutingParticipant(
            final ProducerTemplate subscriberTemplate, final ResultsService resultsService) {
        return new RoutingParticipant(exampleConfig, "eights", 3, "predicateEights", subscriberTemplate, resultsService);
    }

    /**
     * Create a {@link RoutingParticipant} that handles messages where the body comprises a number that is
     * a multiple of 7.
     *
     * @param subscriberTemplate the {@link ConsumerTemplate} for subscribing to messages that match
     * @param resultsService     the service that handles the results of message processing
     * @return the configured {@link RoutingParticipant}
     */
    @Bean
    RoutingParticipant sevensRoutingParticipant(
            final ProducerTemplate subscriberTemplate, final ResultsService resultsService) {
        return new RoutingParticipant(exampleConfig, "sevens", 4, "predicateSevens", subscriberTemplate, resultsService);
    }

    /**
     * Create a {@link RoutingParticipant} that handles messages where the body comprises a number that is
     * a multiple of 6.
     *
     * @param subscriberTemplate the {@link ConsumerTemplate} for subscribing to messages that match
     * @param resultsService     the service that handles the results of message processing
     * @return the configured {@link RoutingParticipant}
     */
    @Bean
    RoutingParticipant sixesRoutingParticipant(final ProducerTemplate subscriberTemplate, final ResultsService resultsService) {
        return new RoutingParticipant(exampleConfig, "sixes", 5, "predicateSixes", subscriberTemplate, resultsService);
    }

    /**
     * Create a {@link RoutingParticipant} that handles messages where the body comprises a number that is
     * a multiple of 5.
     *
     * @param subscriberTemplate the {@link ConsumerTemplate} for subscribing to messages that match
     * @param resultsService     the service that handles the results of message processing
     * @return the configured {@link RoutingParticipant}
     */
    @Bean
    RoutingParticipant fivesRoutingParticipant(final ProducerTemplate subscriberTemplate, final ResultsService resultsService) {
        return new RoutingParticipant(exampleConfig, "fives", 6, "predicateFives", subscriberTemplate, resultsService);
    }

    /**
     * Create a {@link RoutingParticipant} that handles messages where the body comprises a number that is
     * a multiple of 4.
     *
     * @param subscriberTemplate the {@link ConsumerTemplate} for subscribing to messages that match
     * @param resultsService     the service that handles the results of message processing
     * @return the configured {@link RoutingParticipant}
     */
    @Bean
    RoutingParticipant foursRoutingParticipant(final ProducerTemplate subscriberTemplate, final ResultsService resultsService) {
        return new RoutingParticipant(exampleConfig, "fours", 7, "predicateFours", subscriberTemplate, resultsService);
    }

    /**
     * Create a {@link RoutingParticipant} that handles messages where the body comprises a number that is
     * a multiple of 3.
     *
     * @param subscriberTemplate the {@link ConsumerTemplate} for subscribing to messages that match
     * @param resultsService     the service that handles the results of message processing
     * @return the configured {@link RoutingParticipant}
     */
    @Bean
    RoutingParticipant threesRoutingParticipant(
            final ProducerTemplate subscriberTemplate, final ResultsService resultsService) {
        return new RoutingParticipant(exampleConfig, "threes", 8, "predicateThrees", subscriberTemplate, resultsService);
    }

    /**
     * Create a {@link RoutingParticipant} that handles messages where the body comprises a number that is
     * an even number.
     *
     * @param subscriberTemplate the {@link ConsumerTemplate} for subscribing to messages that match
     * @param resultsService     the service that handles the results of message processing
     * @return the configured {@link RoutingParticipant}
     */
    @Bean
    RoutingParticipant evensRoutingParticipant(final ProducerTemplate subscriberTemplate, final ResultsService resultsService) {
        return new RoutingParticipant(exampleConfig, "even", 9, "predicateEven", subscriberTemplate, resultsService);
    }

    /**
     * Create a {@link RoutingParticipant} that handles messages where the body comprises a number that is
     * an odd number.
     *
     * @param subscriberTemplate the {@link ConsumerTemplate} for subscribing to messages that match
     * @param resultsService     the service that handles the results of message processing
     * @return the configured {@link RoutingParticipant}
     */
    @Bean
    RoutingParticipant oddsRoutingParticipant(final ProducerTemplate subscriberTemplate, final ResultsService resultsService) {
        return new RoutingParticipant(exampleConfig, "odd", 100, "predicateOdd", subscriberTemplate, resultsService);
    }

    /**
     * Create a {@link RoutingParticipant} that handles messages where the body comprises a number that is
     * a prime.
     *
     * @param subscriberTemplate the {@link ConsumerTemplate} for subscribing to messages that match
     * @param resultsService     the service that handles the results of message processing
     * @return the configured {@link RoutingParticipant}
     */
    @Bean
    RoutingParticipant primesRoutingParticipant(
            final ProducerTemplate subscriberTemplate, final ResultsService resultsService) {
        return new RoutingParticipant(exampleConfig, "primes", 10, "predicatePrimes", subscriberTemplate, resultsService);
    }
}
