#!/bin/bash

echo running amq broker and lra-coordinator
docker-compose -f local-resources/docker-compose.yml up -d

echo compiling project
mvn clean package

echo running payment service
java -Dserver.port=8081 -jar saga-payment-service/target/*.jar > payment.log 2>&1 &
echo "$!" > payment.pid

echo running flight service
java -Dserver.port=8082 -jar saga-flight-service/target/*.jar > flight.log 2>&1 &
echo "$!" > flight.pid

echo running train service
java -Dserver.port=8083 -jar saga-train-service/target/*.jar > train.log 2>&1 &
echo "$!" > train.pid

echo running saga application
java -Dserver.port=8084 -jar saga-app/target/*.jar > app.log 2>&1 &
echo "$!" > app.pid
