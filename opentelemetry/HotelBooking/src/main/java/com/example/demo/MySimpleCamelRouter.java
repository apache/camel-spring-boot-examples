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
package com.example.demo;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

@Component
public class MySimpleCamelRouter extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        restConfiguration()
                .component("servlet")
                .bindingMode(RestBindingMode.json);

        rest().get("/bookHotel")
                .to("direct:bookHotel");

        from("direct:bookHotel").routeId("bookHotel-http")
                .log(LoggingLevel.INFO, "New book hotel request with trace=${header.traceparent}")
                .bean(new AvailableHotels(),"getAvailableHotel")
                .unmarshal().json(JsonLibrary.Jackson);

        // kafka based 
        from("kafka:hotel_input?brokers=kafka:9092").routeId("bookHotel-kafka")
                .log(LoggingLevel.INFO, "New book hotel request via Kafka topic")
                .bean(new AvailableHotels(),"getAvailableHotel")
                .to("kafka:hotel_output?brokers=kafka:9092");
    }
}