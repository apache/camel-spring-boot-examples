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

    public String getStartUri() {
        return startUri;
    }

    public void setStartUri(String startUri) {
        this.startUri = startUri;
    }

    public String getRoutingChannel() {
        return routingChannel;
    }

    public void setRoutingChannel(String routingChannel) {
        this.routingChannel = routingChannel;
    }

    public String getSubscribeUri() {
        return subscribeUri;
    }

    public void setSubscribeUri(String subscribeUri) {
        this.subscribeUri = subscribeUri;
    }

    public String getReceiverScheme() {
        return receiverScheme;
    }

    public void setReceiverScheme(String receiverScheme) {
        this.receiverScheme = receiverScheme;
    }
}
