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
package org.apache.camel.example.observability;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class RandomNumberRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        rest("/api")
            .get("/random")
                .routeId("get-random")
                .produces("text/plain")
                .to("direct:generateRandomNumber");

        from("direct:generateRandomNumber")
            .routeId("generate-random-number")
            .process(exchange -> {
                Random random = new Random();
                int randomNumber = random.nextInt(1000) + 1;
                int delay = random.nextInt(1900) + 100;
                exchange.setVariable("delay", delay);
                exchange.getIn().setBody(String.valueOf(randomNumber));
            })
            .delay(simple("${variable.delay}"))
            .log("Generated random number: ${body} with delay: ${variable.delay}ms");
    }
}