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
package org.apache.camel.example.mail.oauth2;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
@EnableConfigurationProperties
public class CamelApplication {

    @Autowired
    ApplicationConfiguration conf;

    /**
     * A main method to start this application.
     */
    public static void main(String[] args) {
        SpringApplication.run(CamelApplication.class, args);
    }

    @Bean
    public Oauth2ExchangeMailAuthenticator exchangeAuthenticator(){
        return new Oauth2ExchangeMailAuthenticator(conf.getTenantId(),conf.getClientId(), conf.getClientSecret(), conf.getUser());
    }

    @Bean
    public RouteBuilder routeBuilder(){
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("imaps://outlook.office365.com:993?" +
                        "delay="+conf.getPollInterval()+"}&" +
                        "authenticator=#exchangeAuthenticator&" +
                        "mail.imaps.auth.mechanisms=XOAUTH2&" +
                        "debugMode="+conf.isDebug()+"&" +
                        "delete="+conf.isDelete()+"&"+
                        "bridgeErrorHandler=true")
                        .id("camel-mail-ms-exchange")
                        .log(LoggingLevel.INFO, "message Received: \nFrom: ${header.from}\nSubj: ${header.subject}\nBody:\n${body}");
            }
        };
    }
}
