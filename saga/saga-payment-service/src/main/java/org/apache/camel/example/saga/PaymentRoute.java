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

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.SagaPropagation;

import org.springframework.stereotype.Component;

@Component
public class PaymentRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

                from("jms:queue:{{example.services.payment}}")
                .routeId("payment-service")
                .saga()
                    .propagation(SagaPropagation.MANDATORY)
                    .option("id", header("id"))
                    .compensation("direct:cancelPayment")
                    .log("Paying ${header.payFor} for order #${header.id}")
                    .setBody(header("JMSCorrelationID"))
                    .choice()
                        .when(x -> Math.random() >= 0.85)
                            .log("Payment ${header.payFor} for saga #${header.id} fails!")
                            .throwException(new RuntimeException("Random failure during payment"))
                        .endChoice()
                    .end()
                    .log("Payment ${header.payFor} done for order #${header.id} with payment transaction ${body}")
                .end();

        from("direct:cancelPayment")
                .routeId("payment-cancel")
                .log("Payment for order #${header.id} has been cancelled");
    }
}
