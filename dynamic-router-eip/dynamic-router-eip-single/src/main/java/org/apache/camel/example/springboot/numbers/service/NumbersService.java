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
package org.apache.camel.example.springboot.numbers.service;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.example.springboot.numbers.config.ExampleConfig;
import org.apache.camel.example.springboot.numbers.participants.RoutingParticipant;
import org.apache.camel.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Create numbers and send them to the dynamic router so that they can be
 * processed and binned to demonstrate the usage of the dynamic router
 * component.
 */
@Service
public class NumbersService {

    private static final Logger LOG = LoggerFactory.getLogger(NumbersService.class);

    /**
     * The URI to send the messages to.  This URI feeds the dynamic router in a
     * Camel route.
     */
    private final String startUri;

    /**
     * The routing participants that have subscribed to routing and have
     * provided their rules for suitable exchanges.
     */
    private final List<RoutingParticipant> participants;

    /**
     * The {@link ProducerTemplate} to start the messages on the route.
     */
    @Produce(property = "startUri")
    private final ProducerTemplate start;

    /**
     * The service that contains participants' results of processing the
     * messages that they have received.
     */
    private final ResultsService resultsService;

    /**
     * The {@link CountDownLatch} to wait for the total expected number of
     * messages to be received.
     */
    private CountDownLatch countDownLatch;

    /**
     * The number of messages to generate.
     */
    final int numberOfMessages;

    /**
     * Create this service with everything.
     *
     * @param config             the configuration object representing the config properties
     * @param participants       the dynamic router participants
     * @param start              the producer template to send messages to the start endpoint
     * @param resultsService     the service that compiles routing results
     */
    public NumbersService(
            final ExampleConfig config,
            final List<RoutingParticipant> participants,
            final ProducerTemplate start,
            final ResultsService resultsService) {
        this.startUri = config.getStartUri();
        this.participants = participants;
        this.start = start;
        this.resultsService = resultsService;
        this.numberOfMessages = config.getSendMessageCount();
    }

    /**
     * After the application is started and ready, tell each routing
     * participant to subscribe, and then send the messages.  Afterward,
     * display the results and exit the app.
     */
    public String start(int limit) throws InterruptedException {
        resultsService.resetStatistics();
        LOG.info("Subscribing {} participants", participants.size());
        participants.forEach(RoutingParticipant::subscribe);
        countDownLatch = new CountDownLatch(1);
        final StopWatch watch = new StopWatch();
        LOG.info("Sending {} messages to the dynamic router: {}", limit, getStartUri());
        Mono<Integer> msgFlux = sendMessages(limit);
        msgFlux.subscribe();
        if (!countDownLatch.await(1, MINUTES)) {
            LOG.warn("Statistics may be inaccurate, since the operation timed out");
        }
        return resultsService.getStatistics(watch, limit);
    }

    /**
     * Sends the messages to the starting endpoint of the route.
     *
     * @return an empty Mono
     */
    public Mono<Integer> sendMessages(int limit) {
        return Flux.range(1, limit)
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .doOnNext(n -> start.sendBodyAndHeader(n, "number", n))
                .reduce((a, b) -> b)
                .doFinally(signal -> countDownLatch.countDown());
    }

    /**
     * Gets the starting endpoint of the route.
     *
     * @return the starting endpoint of the route.
     */
    public String getStartUri() {
        return startUri;
    }
}
