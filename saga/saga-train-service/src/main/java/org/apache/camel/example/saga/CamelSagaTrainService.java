package org.apache.camel.example.saga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//CHECKSTYLE:OFF
@SpringBootApplication
public class CamelSagaTrainService {

    public static void main(String[] args) {
        SpringApplication.run(CamelSagaTrainService.class, args);
    }
}
// CHECKSTYLE:ON
