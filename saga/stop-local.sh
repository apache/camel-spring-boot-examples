#!/bin/bash

# mvn spring-boot:run always forks a child JVM, so the pid files hold the
# mvn process id, not the actual Spring Boot process; kill the forked
# child(ren) first, then the mvn process itself.
kill_mvn_and_children() {
  local pid="$1"
  pkill -9 -P "$pid" 2>/dev/null
  kill -9 "$pid" 2>/dev/null
}

echo stopping saga application
kill_mvn_and_children $(cat app.pid) && rm app.pid

echo stopping flight service
kill_mvn_and_children $(cat flight.pid) && rm flight.pid

echo stopping train service
kill_mvn_and_children $(cat train.pid) && rm train.pid

echo stopping payment service
kill_mvn_and_children $(cat payment.pid) && rm payment.pid

echo stopping amq broker and lra-coordinator
docker compose -f local-resources/compose.yaml stop
