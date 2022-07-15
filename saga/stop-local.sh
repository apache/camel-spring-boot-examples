#!/bin/bash

echo stopping saga application
kill -9 $(cat app.pid) && rm app.pid

echo stopping flight service
kill -9 $(cat flight.pid) && rm flight.pid

echo stopping train service
kill -9 $(cat train.pid) && rm train.pid

echo stopping payment service
kill -9 $(cat payment.pid) && rm payment.pid

echo stopping amq broker and lra-coordinator
docker-compose -f local-resources/docker-compose.yml stop
