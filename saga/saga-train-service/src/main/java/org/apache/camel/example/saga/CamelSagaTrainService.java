package org.apache.camel.example.saga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisAutoConfiguration;

//CHECKSTYLE:OFF
@SpringBootApplication
@EnableAutoConfiguration(exclude = {
        ArtemisAutoConfiguration.class,
        JmsAutoConfiguration.class,
        ActiveMQAutoConfiguration.class
})
public class CamelSagaTrainService {

    public static void main(String[] args) {
        SpringApplication.run(CamelSagaTrainService.class, args);
    }
}
// CHECKSTYLE:ON
