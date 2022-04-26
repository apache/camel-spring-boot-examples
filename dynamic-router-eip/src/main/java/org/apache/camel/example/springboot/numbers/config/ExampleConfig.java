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

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.CountDownLatch;

/**
 * The config properties object from the application.yml file.
 */
@ConfigurationProperties(prefix = "camel.spring-boot.example.dynamic-router-eip")
public class ExampleConfig {

    /**
     * The dynamic router channel.
     */
    private String routingChannel;

    /**
     * The dynamic router control channel URI
     */
    private String subscribeUri;

    /**
     * The scheme that routing participants will use to listen for messages.
     */
    private String receiverScheme;

    /**
     * The URI where messages will be sent to as the starting point for the
     * route that feeds the dynamic router.
     */
    private String startUri;

    /**
     * The recipient mode -- first matching filter only, or all matching filters.
     */
    private String recipientMode;

    /**
     * The number of messages to send.
     */
    private int sendMessageCount;

    /**
     * If the recipient mode is "firstMatch", then this is the number of
     * messages that are expected to be received.  This value is also used
     * to create a {@link CountDownLatch} so that all messages can be
     * received before printing out messaging statistics.
     */
    private int expectedFirstMatchMessageCount;

    /**
     * If the recipient mode is "allMatch", then this is the number of
     * messages that are expected to be received.  There is probably not an
     * easy way to calculate this number, so the number needs to be known
     * for a given {@link #sendMessageCount} value.  This value is also used
     * to create a {@link CountDownLatch} so that all messages can be
     * received before printing out messaging statistics.
     */
    private int expectedAllMatchMessageCount;

    /**
     * The URI where messages will be sent to as the starting point for the
     * route that feeds the dynamic router.
     */
    public String getStartUri() {
        return startUri;
    }

    public void setStartUri(String startUri) {
        this.startUri = startUri;
    }

    /**
     * The dynamic router channel.
     */
    public String getRoutingChannel() {
        return routingChannel;
    }

    public void setRoutingChannel(String routingChannel) {
        this.routingChannel = routingChannel;
    }

    /**
     * The dynamic router control channel URI
     */
    public String getSubscribeUri() {
        return subscribeUri;
    }

    public void setSubscribeUri(String subscribeUri) {
        this.subscribeUri = subscribeUri;
    }

    /**
     * The scheme that routing participants will use to listen for messages.
     */
    public String getReceiverScheme() {
        return receiverScheme;
    }

    public void setReceiverScheme(String receiverScheme) {
        this.receiverScheme = receiverScheme;
    }

    /**
     * The recipient mode -- first matching filter only, or all matching filters.
     */
    public String getRecipientMode() {
        return this.recipientMode;
    }

    public void setRecipientMode(String recipientMode) {
        this.recipientMode = recipientMode;
    }

    /**
     * The number of messages to send.
     */
    public int getSendMessageCount() {
        return sendMessageCount;
    }

    public void setSendMessageCount(int sendMessageCount) {
        this.sendMessageCount = sendMessageCount;
    }

    /**
     * If the recipient mode is "firstMatch", then this is the number of
     * messages that are expected to be received.  This value is also used
     * to create a {@link CountDownLatch} so that all messages can be
     * received before printing out messaging statistics.
     */
    public int getExpectedFirstMatchMessageCount() {
        return expectedFirstMatchMessageCount;
    }

    public void setExpectedFirstMatchMessageCount(int expectedFirstMatchMessageCount) {
        this.expectedFirstMatchMessageCount = expectedFirstMatchMessageCount;
    }

    /**
     * If the recipient mode is "allMatch", then this is the number of
     * messages that are expected to be received.  There is probably not an
     * easy way to calculate this number, so the number needs to be known
     * for a given {@link #sendMessageCount} value.  This value is also used
     * to create a {@link CountDownLatch} so that all messages can be
     * received before printing out messaging statistics.
     */
    public int getExpectedAllMatchMessageCount() {
        return expectedAllMatchMessageCount;
    }

    public void setExpectedAllMatchMessageCount(int expectedAllMatchMessageCount) {
        this.expectedAllMatchMessageCount = expectedAllMatchMessageCount;
    }
}
