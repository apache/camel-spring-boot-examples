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
package org.apache.camel.example.saga;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.SagaDefinition;
import org.apache.camel.model.rest.RestParamType;

import org.springframework.stereotype.Component;

@Component
public class SagaRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        rest().post("/saga")
                .param().type(RestParamType.query).name("id").dataType("int").required(true).endParam()
                .to("direct:saga");

        from("direct:saga")
                .saga()
                .compensation("direct:cancelOrder")
                    .log("Executing saga #${header.id} with LRA ${header.Long-Running-Action}")
                    .setHeader("payFor", constant("train"))
                    .to("activemq:queue:{{example.services.train}}?exchangePattern=InOut" +
                            "&replyTo={{example.services.train}}.reply")
                    .log("train seat reserved for saga #${header.id} with payment transaction: ${body}")
                    .setHeader("payFor", constant("flight"))
                    .to("activemq:queue:{{example.services.flight}}?exchangePattern=InOut" +
                            "&replyTo={{example.services.flight}}.reply")
                    .log("flight booked for saga #${header.id} with payment transaction: ${body}")
                .setBody(header("Long-Running-Action"))
                .end();

        from("direct:cancelOrder")
                .log("Transaction ${header.Long-Running-Action} has been cancelled due to flight or train failure");

    }

}
