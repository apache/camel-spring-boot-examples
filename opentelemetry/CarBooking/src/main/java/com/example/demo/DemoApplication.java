package com.example.demo;

import org.apache.camel.opentelemetry.starter.CamelOpenTelemetry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@CamelOpenTelemetry
@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
