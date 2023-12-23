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

import org.apache.camel.CamelContext;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.example.springboot.numbers.common.model.CommandMessage;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.camel.example.springboot.numbers.common.util.NumbersCommonUtil.COMMAND_RESET_STATS;
import static org.apache.camel.example.springboot.numbers.common.util.NumbersCommonUtil.COMMAND_STATS;
import static org.apache.camel.example.springboot.numbers.common.util.NumbersCommonUtil.HEADER_COMMAND;
import static org.apache.camel.example.springboot.numbers.common.util.NumbersCommonUtil.HEADER_NUMBER;

public abstract class ProcessNumbersRoutingParticipant extends RoutingParticipant {

    protected final String numberName;

    /**
     * Counter for the number of messages that this participant has processed.
     */
    protected final AtomicInteger processedCount = new AtomicInteger(0);

    /**
     * The count that was last reported via "stats" command message.
     */
    protected final AtomicInteger reportedCount = new AtomicInteger(0);

    protected ProcessNumbersRoutingParticipant( // NOSONAR
            String numberName,
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
        super(subscriberId, subscribeUri, routingChannel, subscriptionPriority, predicate, expressionLanguage,
                consumeUri, commandUri, producerTemplate, camelContext);
        this.numberName = numberName;
        producerTemplate.start();
    }

    /**
     * This method consumes messages that have matched the participant's rules
     * and have been routed to the participant.
     *
     * @param body the serialized command message
     */
    @Override
    @Consume(property = "consumeUri")
    public void consumeMessage(final String body, @Header(value = HEADER_NUMBER) String number) {
        if (COMMAND_RESET_STATS.equals(body)) {
            processedCount.set(0);
            reportedCount.set(0);
        } else {
            processedCount.incrementAndGet();
        }
    }

    @Scheduled(fixedRate = 2, timeUnit = TimeUnit.SECONDS)
    public void sendStats() {
        int pCount = processedCount.get();
        if (pCount > reportedCount.get()) {
            CommandMessage command = new CommandMessage(COMMAND_STATS, Map.of(numberName, String.valueOf(pCount)));
            producerTemplate.sendBodyAndHeader(commandUri, command.toString(), HEADER_COMMAND, COMMAND_STATS);
            reportedCount.set(pCount);
        }
    }
}
