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
package sample.camel;

import java.util.function.Consumer;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;

import org.apache.camel.CamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

//CHECKSTYLE:OFF
/**
 * A sample Spring Boot application that starts the Camel routes.
 */
@SpringBootApplication
public class MyCamelApplication {
    
    private static final Logger LOG = LoggerFactory.getLogger(MyCamelApplication.class);
    private static final int CONTAINER_PORT = 8080;
    private static final String CONTAINER_IMAGE = "hapiproject/hapi:v4.2.0";

    private GenericContainer container;
    /**
     * A main method to start this application.
     */
    public static void main(String[] args) {
        SpringApplication.run(MyCamelApplication.class, args);
    }
    
    @Bean
    CamelContextConfiguration contextConfiguration() {
        return new CamelContextConfiguration() {
            @Override
            public void beforeApplicationStart(CamelContext context) {
                LOG.info("start hapi-fhir-jpaserver-starter docker container");
                Consumer<CreateContainerCmd> cmd = e -> {
                    e.withPortBindings(new PortBinding(Ports.Binding.bindPort(CONTAINER_PORT),
                    new ExposedPort(CONTAINER_PORT)));
                    
                };
                

                container = new GenericContainer(CONTAINER_IMAGE)
                    .withNetworkAliases("fhir")
                    .withExposedPorts(CONTAINER_PORT)
                    .withCreateContainerCmdModifier(cmd)
                    .withEnv("HAPI_FHIR_VERSION", "DSTU3")
                    .withEnv("HAPI_REUSE_CACHED_SEARCH_RESULTS_MILLIS", "-1")
                    .waitingFor(Wait.forListeningPort())
                    .waitingFor(Wait.forHttp("/hapi-fhir-jpaserver/fhir/metadata"));;
                container.start();
                
            }

            @Override
            public void afterApplicationStart(CamelContext camelContext) {
                // noop
            }
        };
    }


}
