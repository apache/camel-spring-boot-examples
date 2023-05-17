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
                .log(LoggingLevel.INFO, "New book hotel request with traceId=${header.x-b3-traceid}")
                .bean(new AvailableHotels(),"getAvailableHotel")
                .unmarshal().json(JsonLibrary.Jackson);

                        
        // kafka based 
        from("kafka:hotel_input?brokers=kafka:9092").routeId("bookHotel-kafka")
                .log(LoggingLevel.INFO, "New book hotel request via Kafka topic")
                // .to("log:debug?showAll=true&multiline=true")
                .bean(new AvailableHotels(),"getAvailableHotel")
                .to("kafka:hotel_output?brokers=kafka:9092");
    }
}