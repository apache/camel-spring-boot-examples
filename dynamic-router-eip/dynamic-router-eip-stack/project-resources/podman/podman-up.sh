#!/bin/bash
#
#   Licensed to the Apache Software Foundation (ASF) under one or more
#   contributor license agreements.  See the NOTICE file distributed with
#   this work for additional information regarding copyright ownership.
#   The ASF licenses this file to You under the Apache License, Version 2.0
#   (the "License"); you may not use this file except in compliance with
#   the License.  You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#

check_service_healthy() {
  local service_name="${1}"
  podman ps --format "{{.Names}} {{.Status}}" --filter name="${service_name}" | grep -q "healthy"
}

wait_for_service() {
  local service_name="${1}"
  local count=60
  while ! check_service_healthy "${service_name}" && [[ count -gt 0 ]]
  do
    printf "\r\033[KWaiting for service '${service_name}' [%2d]" $((count--))
    sleep 1
  done
  echo
}

kafka() {
  podman pod create \
    --name broker \
    --hostname broker \
    --infra-name broker_infra \
    --network test_net \
    --replace

  podman run -d \
    --name kafka_broker \
    --pod broker \
    --env KAFKA_ENABLE_KRAFT=yes \
    --env KAFKA_CFG_NODE_ID=1 \
    --env KAFKA_CFG_PROCESS_ROLES=broker,controller \
    --env KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER \
    --env KAFKA_CFG_INTER_BROKER_LISTENER_NAME=PLAINTEXT \
    --env KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT \
    --env KAFKA_CFG_LISTENERS=PLAINTEXT://broker:9092,CONTROLLER://broker:9093 \
    --env KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://broker:9092 \
    --env KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=1@broker:9093 \
    --env KAFKA_CFG_NUM_PARTITIONS=128 \
    --env ALLOW_PLAINTEXT_LISTENER=yes \
    --env KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE=true \
    --health-cmd '[ "kafka-topics.sh", "--bootstrap-server", "broker:9092", "--list" ]' \
    --health-interval 30s \
    --health-timeout 10s \
    --health-retries 5 \
    --health-start-period 10s \
    docker.io/bitnami/kafka:3.6.1

    wait_for_service "kafka_broker"
}

main_router() {
  podman pod create \
    --name main_router \
    --hostname main_router \
    --infra-name main_router_infra \
    --publish 8082:8082 \
    --network test_net \
    --replace

  podman run -d \
    --name main_router_service \
    --pod main_router \
    --env THC_PATH=/main-router/actuator/health \
    --env THC_PORT=8082 \
    --health-cmd '[ "/cnb/process/health-check" ]' \
    --health-interval 30s \
    --health-timeout 10s \
    --health-retries 5 \
    --health-start-period 10s \
    docker.io/library/main-router:latest

    wait_for_service "main_router_service"
}

all_numbers_service() {
  podman pod create \
    --name all_numbers \
    --hostname all_numbers \
    --infra-name all_numbers_infra \
    --publish 8911:8911 \
    --network test_net \
    --replace

  podman run -d \
    --name all_numbers_service \
    --pod all_numbers \
    --env THC_PATH=/all-numbers/actuator/health \
    --env THC_PORT=8911 \
    --health-cmd '[ "/cnb/process/health-check" ]' \
    --health-interval 30s \
    --health-timeout 10s \
    --health-retries 5 \
    --health-start-period 10s \
    docker.io/library/all-numbers-service:latest
}

even_numbers_service() {
  podman pod create \
    --name even_numbers \
    --hostname even_numbers \
    --infra-name even_numbers_infra \
    --publish 8902:8902 \
    --network test_net \
    --replace

  podman run -d \
    --name even_numbers_service \
    --pod even_numbers \
    --env THC_PATH=/even-numbers/actuator/health \
    --env THC_PORT=8902 \
    --health-cmd '[ "/cnb/process/health-check" ]' \
    --health-interval 30s \
    --health-timeout 10s \
    --health-retries 5 \
    --health-start-period 10s \
    docker.io/library/even-numbers-service:latest
}

odd_numbers_service() {
  podman pod create \
    --name odd_numbers \
    --hostname odd_numbers \
    --infra-name odd_numbers_infra \
    --publish 8901:8901 \
    --network test_net \
    --replace

  podman run -d \
    --name odd_numbers_service \
    --pod odd_numbers \
    --env THC_PATH=/odd-numbers/actuator/health \
    --env THC_PORT=8901 \
    --health-cmd '[ "/cnb/process/health-check" ]' \
    --health-interval 30s \
    --health-timeout 10s \
    --health-retries 5 \
    --health-start-period 10s \
    docker.io/library/odd-numbers-service:latest
}

start_services() {
  kafka
  main_router
  all_numbers_service
  even_numbers_service
  odd_numbers_service
}

prepare_resources() {
  podman network create test_net --ignore
}

prepare_resources
start_services
