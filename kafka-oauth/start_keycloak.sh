#!/bin/bash

cd containers/keycloak
podman run -it --rm \
  -v ./realms:/opt/keycloak/data/import:Z \
  -v ./certificates/keycloak.server.keystore.p12:/opt/keycloak/data/certs/keycloak.server.keystore.p12:Z \
  -p 8080:8080 -p 8443:8443 \
  -e KC_BOOTSTRAP_ADMIN_USERNAME=admin \
  -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin \
  -e KC_HOSTNAME=keycloak \
  -e KC_HOSTNAME_ADMIN_URL=https://keycloak:8443 \
  -e KC_HTTP_ENABLED=true \
  --name keycloak \
  --network host \
  quay.io/keycloak/keycloak:26.0 \
  -v start --import-realm --features=token-exchange,authorization,scripts --https-key-store-file=/opt/keycloak/data/certs/keycloak.server.keystore.p12 --https-key-store-password=changeit
cd -
