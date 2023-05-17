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
                .port(8080).host("localhost")
                .bindingMode(RestBindingMode.json);

        rest().get("/bookFlight")
                .to("direct:bookFlight");

        from("direct:bookFlight").routeId("bookFlight-http")
                .log(LoggingLevel.INFO, "New book flight request with traceId=${header.x-b3-traceid}")
                .bean(new AvailableFlights(),"getAvailableFlight")
                .unmarshal().json(JsonLibrary.Jackson);

        // kafka based 
        from("kafka:flight_input?brokers=kafka:9092").routeId("bookFlight-kafka")
                .log(LoggingLevel.INFO, "New book flight request via Kafka topic")
                // .to("log:debug?showAll=true&multiline=true")
                .bean(new AvailableFlights(),"getAvailableFlight")
                .to("kafka:flight_output?brokers=kafka:9092");

    }
}