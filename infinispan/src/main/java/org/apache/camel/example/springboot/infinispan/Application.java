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
package org.apache.camel.example.springboot.infinispan;

import org.apache.camel.component.infinispan.remote.InfinispanRemoteComponent;
import org.apache.camel.component.infinispan.remote.InfinispanRemoteConfiguration;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.shaded.org.apache.commons.lang.SystemUtils;

import java.util.Properties;


// CHECKSTYLE:OFF
@SpringBootApplication
public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    /**
     * Main method to start the application.
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Configuration
    @ConfigurationProperties(prefix = "infinispan")
    public class InfinispanConfiguration {

        private String host;
        
        private Integer port;

        private String username;

        private String password;

        private String serverName;

        protected ConfigurationBuilder getConfiguration() {
            ConfigurationBuilder clientBuilder = new ConfigurationBuilder();

            clientBuilder.forceReturnValues(true);


            clientBuilder.addServer().host(getHost()).port(getPort());

            // add security info
            clientBuilder.security().authentication().username(getUsername()).password(getPassword())
                .serverName(getServerName()).saslMechanism("DIGEST-MD5").realm("default");
            if (SystemUtils.IS_OS_MAC) {
                Properties properties = new Properties();
                properties.put("infinispan.client.hotrod.client_intelligence", "BASIC");
                clientBuilder.withProperties(properties);
            }

            return clientBuilder;
        }

        @Bean(name = "infinispanRemoteComponent")
        public InfinispanRemoteComponent infinispanRemoteComponent() {
            InfinispanRemoteConfiguration infinispanRemoteConfiguration = new InfinispanRemoteConfiguration();
            infinispanRemoteConfiguration.setHosts(getHost() + ":" + getPort());
            infinispanRemoteConfiguration.setUsername(getUsername());
            infinispanRemoteConfiguration.setPassword(getPassword());

            RemoteCacheManager cacheContainer = new RemoteCacheManager(getConfiguration().build());
            infinispanRemoteConfiguration.setCacheContainer(cacheContainer);

            InfinispanRemoteComponent component = new InfinispanRemoteComponent();
            component.setConfiguration(infinispanRemoteConfiguration);
            return component;
        }

        public String getHost() {
            return host;
        }

        public void setHost(final String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(final Integer port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(final String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(final String password) {
            this.password = password;
        }

        public String getServerName() {
            return serverName;
        }

        public void setServerName(final String serverName) {
            this.serverName = serverName;
        }
    }

}
// CHECKSTYLE:ON
