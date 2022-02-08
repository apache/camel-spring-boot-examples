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

import java.util.Properties;
import java.util.function.Consumer;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;

import org.apache.camel.component.infinispan.remote.InfinispanRemoteComponent;
import org.apache.camel.component.infinispan.remote.InfinispanRemoteConfiguration;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.configuration.cache.CacheMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.org.apache.commons.lang.SystemUtils;


// CHECKSTYLE:OFF
@SpringBootApplication
public class Application {
    
    
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);
    
    private static final String CONTAINER_IMAGE = "quay.io/infinispan/server:13.0.5.Final-1";
    private static final String CONTAINER_NAME = "infinispan";
    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "password";
    private static final int CONTAINER_PORT = 11222;
    
    private GenericContainer container;
    /**
     * Main method to start the application.
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    
    
    @Configuration
    public class InfinispanConfiguration {

        private void initContainer() {
            LOG.info("start infinispan docker container");
            Consumer<CreateContainerCmd> cmd = e -> {
                e.getHostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(CONTAINER_PORT),
                                                   new ExposedPort(CONTAINER_PORT)));

            };
            final Logger containerLog = LoggerFactory.getLogger("container." + CONTAINER_NAME);
            final Consumer<OutputFrame> logConsumer = new Slf4jLogConsumer(containerLog);

            container = new GenericContainer<>(CONTAINER_IMAGE).withNetworkAliases(CONTAINER_NAME)
                .withEnv("USER", DEFAULT_USERNAME).withEnv("PASS", DEFAULT_PASSWORD)
                .withLogConsumer(logConsumer)
                .withClasspathResourceMapping("infinispan.xml", "/user-config/infinispan.xml",
                                              BindMode.READ_ONLY)
                .withCommand("-c", "/user-config/infinispan.xml").withExposedPorts(CONTAINER_PORT)
                .withCreateContainerCmdModifier(cmd).waitingFor(Wait.forListeningPort())
                .waitingFor(Wait.forLogMessage(".*Infinispan.*Server.*started.*", 1));
            container.start();

        }

        protected ConfigurationBuilder getConfiguration() {
            ConfigurationBuilder clientBuilder = new ConfigurationBuilder();

            clientBuilder.forceReturnValues(true);


            clientBuilder.addServer().host("localhost").port(CONTAINER_PORT);

            // add security info
            clientBuilder.security().authentication().username(DEFAULT_USERNAME).password(DEFAULT_PASSWORD)
                .serverName("infinispan").saslMechanism("DIGEST-MD5").realm("default");
            if (SystemUtils.IS_OS_MAC) {
                Properties properties = new Properties();
                properties.put("infinispan.client.hotrod.client_intelligence", "BASIC");
                clientBuilder.withProperties(properties);
            }

            return clientBuilder;
        }

        @Bean(name = "infinispanRemoteComponent")
        public InfinispanRemoteComponent infinispanRemoteComponent() {
            initContainer();
            InfinispanRemoteConfiguration infinispanRemoteConfiguration = new InfinispanRemoteConfiguration();

            infinispanRemoteConfiguration.setHosts("localhost" + ":" + CONTAINER_PORT);

            infinispanRemoteConfiguration.setUsername(DEFAULT_USERNAME);
            infinispanRemoteConfiguration.setPassword(DEFAULT_PASSWORD);

            RemoteCacheManager cacheContainer = new RemoteCacheManager(getConfiguration().build());
            cacheContainer.administration()
                .getOrCreateCache("default", new org.infinispan.configuration.cache.ConfigurationBuilder()
                    .clustering().cacheMode(CacheMode.DIST_SYNC).build());

            infinispanRemoteConfiguration.setCacheContainer(cacheContainer);
            InfinispanRemoteComponent component = new InfinispanRemoteComponent();
            component.setConfiguration(infinispanRemoteConfiguration);

            return component;
        }
    }

}
// CHECKSTYLE:ON
