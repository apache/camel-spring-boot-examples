package org.apache.camel.example.saga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//CHECKSTYLE:OFF
@SpringBootApplication
public class CamelSagaFlightService {

    public static void main(String[] args) {
        SpringApplication.run(CamelSagaFlightService.class, args);
    }
}
// CHECKSTYLE:ON
