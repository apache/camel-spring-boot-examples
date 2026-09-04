#!/bin/bash

echo running amq broker and lra-coordinator
docker compose -f local-resources/compose.yaml up -d

echo compiling project
mvn clean package

echo running payment service
mvn -f saga-payment-service/ spring-boot:run > payment.log 2>&1 &
echo $! > payment.pid

echo running flight service
mvn -f saga-flight-service/ spring-boot:run > flight.log 2>&1 &
echo $! > flight.pid

echo running train service
mvn -f saga-train-service/ spring-boot:run > train.log 2>&1 &
echo $! > train.pid

echo running saga application
mvn -f saga-app/ spring-boot:run > app.log 2>&1 &
echo $! > app.pid

wait
