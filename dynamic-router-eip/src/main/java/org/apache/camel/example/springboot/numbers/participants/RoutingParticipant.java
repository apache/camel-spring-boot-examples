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
package org.apache.camel.example.springboot.numbers.participants;

import org.apache.camel.Consume;
import org.apache.camel.Predicate;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.dynamicrouter.message.DynamicRouterControlMessage;
import org.apache.camel.component.dynamicrouter.message.DynamicRouterControlMessage.SubscribeMessageBuilder;
import org.apache.camel.example.springboot.numbers.config.ExampleConfig;
import org.apache.camel.example.springboot.numbers.service.ResultsService;

/**
 * A dynamic routing participant that knows how to build a control channel message
 * for subscribing to dynamic router content.  A provided subscribe URI will allow
 * an instance to consume messages sent to that URI.  Results are sent to the
 * results service instance.
 */
public class RoutingParticipant {

    /**
     * Name of the dynamic router channel for an implementation to create a
     * subscription for.
     */
    protected final String routingChannel;

    /**
     * The dynamic router control channel URI where subscribe messages will
     * be sent.
     */
    protected final String subscribeUri;

    /**
     * For consuming messages that fit an implementation's rules, this is the
     * scheme/component that the participant will listen on and consume from.
     */
    protected final String receiverScheme;

    /**
     * The "bin" or "category" of messages for a participant implementation.
     * This value is also used along with the {@link #receiverScheme} to
     * create the {@link #consumeUri}.
     */
    protected final String bin;

    /**
     * The priority of the processor when evaluated by the dynamic router.  Lower
     * number means higher priority.
     */
    protected final int priority;

    /**
     * The {@link Predicate} by which exchanges are evaluated for suitability for
     * a routing participant.
     */
    protected final Predicate predicate;

    /**
     * The URI that a participant implementation will listen on for messages
     * that match its rules.
     */
    protected final String consumeUri;

    /**
     * The {@link ProducerTemplate} to send subscriber messages to the dynamic
     * router control channel.
     */
    protected final ProducerTemplate subscriberTemplate;

    /**
     * The service that accumulates results of dynamic routing from each
     * participant.
     */
    protected final ResultsService resultsService;

    public RoutingParticipant(
            final ExampleConfig config,
            final String bin,
            final int priority,
            final Predicate predicate,
            final ProducerTemplate subscriberTemplate,
            final ResultsService resultsService) {
        this.bin = bin;
        this.priority = priority;
        this.predicate = predicate;
        this.routingChannel = config.getRoutingChannel();
        this.subscribeUri = config.getSubscribeUri();
        this.receiverScheme = config.getReceiverScheme();
        this.consumeUri = receiverScheme + ":" + bin;
        this.subscriberTemplate = subscriberTemplate;
        this.resultsService = resultsService;
    }

    /**
     * This method consumes messages that have matched the participant's rules
     * and have been routed to the participant.  It adds the results to the
     * results service.
     *
     * @param number the numeric message body that is received
     */
    @Consume(property = "consumeUri")
    public void consumeMessage(final int number) {
        resultsService.addResult(bin, number);
    }

    /**
     * Gets the consumer URI.
     *
     * @return the consumer URI
     */
    public String getConsumeUri() {
        return this.consumeUri;
    }

    /**
     * Sends the subscribe message to the dynamic router control channel.
     */
    public void subscribe() {
        subscriberTemplate.sendBody(subscribeUri, createSubscribeMessage());
    }

    /**
     * Gets the bin / category.
     *
     * @return the bin / category
     */
    protected String getBin() {
        return bin;
    }

    /**
     * Gets the implementation's rule priority.
     *
     * @return the rule priority
     */
    protected int getPriority() {
        return priority;
    }

    /**
     * The implementation's rule as a {@link Predicate}.
     * @return the {@link Predicate} rule
     */
    protected Predicate getPredicate() {
        return predicate;
    }

    /**
     * Create a {@link DynamicRouterControlMessage} based on parameters from the
     * implementing class.
     *
     * @return the {@link DynamicRouterControlMessage}
     */
    protected DynamicRouterControlMessage createSubscribeMessage() {
        return new SubscribeMessageBuilder()
                .id(getBin())
                .channel(routingChannel)
                .priority(getPriority())
                .endpointUri(getConsumeUri())
                .predicate(getPredicate())
                .build();
    }
}
