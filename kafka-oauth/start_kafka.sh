#!/bin/bash

cd containers/kafka
podman run -it --rm \
   -p 9091:9091 \
   -p 9092:9092 \
   -e LOG_DIR=/home/kafka/logs \
   -e KAFKA_PROCESS_ROLES="broker,controller" \
   -e KAFKA_NODE_ID="1" \
   -e KAFKA_CONTROLLER_QUORUM_VOTERS="1@kafka:9091" \
   -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER \
   -e KAFKA_SASL_MECHANISM_CONTROLLER_PROTOCOL=PLAIN \
   -e KAFKA_LISTENERS="CONTROLLER://kafka:9091,CLIENT://kafka:9092" \
   -e KAFKA_ADVERTISED_LISTENERS="CLIENT://kafka:9092" \
   -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP="CONTROLLER:SASL_PLAINTEXT,CLIENT:SASL_PLAINTEXT" \
   -e KAFKA_INTER_BROKER_LISTENER_NAME=CLIENT \
   -e KAFKA_SASL_MECHANISM_INTER_BROKER_PROTOCOL=OAUTHBEARER \
   -e KAFKA_PRINCIPAL_BUILDER_CLASS="io.strimzi.kafka.oauth.server.OAuthKafkaPrincipalBuilder" \
   -e KAFKA_LISTENER_NAME_CONTROLLER_SASL_ENABLED_MECHANISMS=PLAIN \
   -e KAFKA_LISTENER_NAME_CONTROLLER_PLAIN_SASL_JAAS_CONFIG="org.apache.kafka.common.security.plain.PlainLoginModule required username=\"admin\" password=\"admin-password\" user_admin=\"admin-password\"    user_bobby=\"bobby-secret\" ;" \
   -e KAFKA_LISTENER_NAME_CLIENT_SASL_ENABLED_MECHANISMS=OAUTHBEARER \
   -e KAFKA_LISTENER_NAME_CLIENT_OAUTHBEARER_SASL_JAAS_CONFIG="org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;" \
   -e KAFKA_LISTENER_NAME_CLIENT_OAUTHBEARER_SASL_LOGIN_CALLBACK_HANDLER_CLASS=io.strimzi.kafka.oauth.client.JaasClientOauthLoginCallbackHandler \
   -e KAFKA_LISTENER_NAME_CLIENT_OAUTHBEARER_SASL_SERVER_CALLBACK_HANDLER_CLASS=io.strimzi.kafka.oauth.server.JaasServerOauthValidatorCallbackHandler \
   -e KAFKA_SUPER_USERS="User:admin,User:service-account-kafka-broker" \
   -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
   -e OAUTH_CLIENT_ID="kafka-broker" \
   -e OAUTH_CLIENT_SECRET="kafka-broker-secret" \
   -e OAUTH_TOKEN_ENDPOINT_URI="http://keycloak:8080/realms/demo/protocol/openid-connect/token" \
   -e OAUTH_VALID_ISSUER_URI="https://keycloak:8443/realms/demo" \
   -e OAUTH_JWKS_ENDPOINT_URI="http://keycloak:8080/realms/demo/protocol/openid-connect/certs" \
   -e OAUTH_USERNAME_CLAIM=preferred_username \
   -e OAUTH_CONNECT_TIMEOUT_SECONDS="20" \
   -e KEYCLOAK_HOST=keycloak \
   -e KEYCLOAK_URI=https://keycloak:8443 \
   -u kafka \
   --name kafka \
   --network host \
   -v ./scripts:/opt/kafka/scripts:Z \
   -w /opt/kafka/scripts \
   --entrypoint "./start.sh" \
   quay.io/strimzi/kafka:0.45.0-kafka-3.9.0 \
   --kraft
