#!/bin/bash

echo running amq broker and lra-coordinator
docker compose -f local-resources/compose.yaml up -d

echo compiling project
mvn clean package

echo running payment service
mvn -f saga-payment-service/ spring-boot:run &

echo running flight service
mvn -f saga-flight-service/ spring-boot:run &

echo running train service
mvn -f saga-train-service/ spring-boot:run &

echo running saga application
mvn -f saga-app/ spring-boot:run &

wait
