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

package org.apache.camel.example.springboot.numbers.mainrouter.routing;

import org.apache.camel.CamelContext;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.dynamicrouter.control.DynamicRouterControlMessage;
import org.apache.camel.example.springboot.numbers.common.service.RoutingParticipant;
import org.apache.camel.example.springboot.numbers.mainrouter.service.NumberStatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class NumberStatisticsRoutingParticipant extends RoutingParticipant {

    protected static final Logger LOG = LoggerFactory.getLogger(NumberStatisticsRoutingParticipant.class);

    private final NumberStatisticsService numberStatisticsService;

    public NumberStatisticsRoutingParticipant(final NumberStatisticsService numberStatisticsService,
            @Value("${number-generator.subscribe-uri}") String subscribeUri,
            @Value("${number-generator.routing-channel}") String routingChannel,
            @Value("${number-generator.predicate}") String predicate,
            @Value("${number-generator.expression-language}") String expressionLanguage,
            @Value("${number-generator.subscription-priority}") int subscriptionPriority,
            @Value("${number-generator.consume-uri}") String consumeUri,
            @Value("${number-generator.command-uri}") String commandUri,
            ProducerTemplate producerTemplate,
            CamelContext camelContext) {
        super("processNumberStats", subscribeUri, routingChannel, subscriptionPriority,
                predicate, expressionLanguage, consumeUri, commandUri, producerTemplate, camelContext);
        this.numberStatisticsService = numberStatisticsService;
    }

    /**
     * Send the subscribe message after this service instance is created.
     */
    @Override
    protected void subscribe() {
        DynamicRouterControlMessage message = createSubscribeMessage();
        producerTemplate.sendBody("dynamic-router-control:subscribe", message);
    }

    /**
     * After the application is started and ready, subscribe for messages.
     */
    @Override
    @EventListener(ApplicationReadyEvent.class)
    protected void start() {
        LOG.info("Application ready -- scheduling number statistics routing subscription");
        Executors.newScheduledThreadPool(1)
                .schedule(this::subscribe, 10, TimeUnit.SECONDS);
    }

    /**
     * This method consumes messages that have matched the participant's rules
     * and have been routed to the participant.  It adds the results to the
     * results service.
     *
     * @param body the serialized command message
     */
    @Override
    @Consume(property = "consumeUri")
    public void consumeMessage(final String body, @Header(value = "number") String number) throws Exception {
        this.numberStatisticsService.updateStats(body);
    }
}
