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

package org.apache.camel.example.springboot.numbers.common.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.CamelContext;
import org.apache.camel.Header;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.dynamicrouter.control.DynamicRouterControlMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

public abstract class RoutingParticipant {

    protected static final Logger LOG = LoggerFactory.getLogger(RoutingParticipant.class);

    private final ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());

    protected final String subscriberId;

    /**
     * The dynamic router control channel URI where subscribe messages will
     * be sent.
     */
    protected final String subscribeUri;

    /**
     * The channel of the dynamic router to send messages.
     */
    protected final String routingChannel;

    /**
     * The priority of the processor when evaluated by the dynamic router.  Lower
     * number means higher priority.
     */
    protected final int priority;

    /**
     * The predicate by which exchanges are evaluated for suitability for
     * a routing participant.
     */
    protected final String predicate;

    /**
     * The language of the predicate string.
     */
    protected final String expressionLanguage;

    /**
     * The URI that a participant implementation will listen on for messages
     * that match its rules.
     */
    protected final String consumeUri;

    /**
     * URI to send a command to (for dynamic routing).
     */
    protected final String commandUri;

    /**
     * The {@link ProducerTemplate} to send subscriber messages to the dynamic
     * router control channel.
     */
    protected final ProducerTemplate producerTemplate;

    protected final CamelContext camelContext;

    protected RoutingParticipant( // NOSONAR
            String subscriberId,
            String subscribeUri,
            String routingChannel,
            int subscriptionPriority,
            String predicate,
            String expressionLanguage,
            String consumeUri,
            String commandUri,
            ProducerTemplate producerTemplate,
            CamelContext camelContext) {
        this.subscriberId = subscriberId;
        this.subscribeUri = subscribeUri;
        this.routingChannel = routingChannel;
        this.priority = subscriptionPriority;
        this.predicate = predicate;
        this.expressionLanguage = expressionLanguage;
        this.consumeUri = consumeUri;
        this.commandUri = commandUri;
        this.producerTemplate = producerTemplate;
        this.camelContext = camelContext;
        producerTemplate.start();
    }

    /**
     * Send the subscribe message after this service instance is created.
     */
    protected void subscribe() {
        try {
            DynamicRouterControlMessage message = createSubscribeMessage();
            String messageJson = objectMapper.writeValueAsString(message);
            LOG.info("Sending subscribe message: {} to {}", messageJson, subscribeUri);
            producerTemplate.sendBody(subscribeUri, messageJson);
        } catch (Exception e) {
            throw new IllegalStateException("Could not serialize a message", e);
        }
    }

    /**
     * After the application is started and ready, subscribe for messages.
     */
    @EventListener(ApplicationReadyEvent.class)
    protected void start() {
        subscribe();
    }

    /**
     * This method consumes messages that have matched the participant's rules
     * and have been routed to the participant.  It adds the results to the
     * results service.
     *
     * @param body the serialized command message
     */
    public abstract void consumeMessage(final String body, @Header(value = "number") String number);

    /**
     * Create a {@link DynamicRouterControlMessage} based on parameters from the
     * implementing class.
     *
     * @return the {@link DynamicRouterControlMessage}
     */
    protected DynamicRouterControlMessage createSubscribeMessage() {
        return DynamicRouterControlMessage.Builder.newBuilder()
                .subscribeChannel(routingChannel)
                .subscriptionId(subscriberId)
                .destinationUri(consumeUri)
                .priority(priority)
                .predicate("#" + predicate)
                .expressionLanguage(expressionLanguage)
                .build();
    }

    /**
     * Gets the consumer URI.
     *
     * @return the consumer URI
     */
    public String getConsumeUri() {
        return this.consumeUri;
    }
}
