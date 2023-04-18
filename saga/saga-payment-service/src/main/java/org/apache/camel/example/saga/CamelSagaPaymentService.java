package org.apache.camel.example.saga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//CHECKSTYLE:OFF
@SpringBootApplication
public class CamelSagaPaymentService {

    public static void main(String[] args) {
        SpringApplication.run(CamelSagaPaymentService.class, args);
    }
}
// CHECKSTYLE:ON
