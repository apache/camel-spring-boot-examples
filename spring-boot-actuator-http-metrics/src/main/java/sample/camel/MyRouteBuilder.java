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

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

@Component
public class MyRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // First, we have to configure our jetty component, which will be the rest
        // in charge of querying the REST endpoints from actuator
        restConfiguration()
                .host("0.0.0.0")
                .port(8080)
                .bindingMode(RestBindingMode.json);

        // First, let's show the routes we have exposed. Let's create a timer
        // consumer that will only fire once and show us the exposed mappings
        from("timer:queryTimer?repeatCount=1")
                .to("rest:get:/actuator/mappings")
                .unmarshal()
                .json(true)
                .to("log:INFO?multiline=true");

        // Then, we will be querying the cpu consumption periodically. For more options, you can check
        // https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html#production-ready-metrics-endpoint
        from("timer:metricsTimer?period={{metricsPeriod}}")
                .to("rest:get:/actuator/metrics/system.cpu.usage")
                .unmarshal()
                .json(true)
                .to("log:INFO?multiline=true");

        // Finally, let's see how to shutdown our application using the actuator endpoint
        from("timer:shutdownTimer?delay={{shutdownTime}}&repeatCount=1")
                .log("Shutting down")
                .to("rest:post:/actuator/shutdown");
    }
}
