#!/bin/bash
GEN_DIR="ssl"

K_PASS=pass123
SERVER_JKS=$GEN_DIR/server.jks
SERVER_CERT=$GEN_DIR/server.pem
SERVER_TRUST=$GEN_DIR/server-truststore.jks
CLIENT_JKS=$GEN_DIR/client.jks
CLIENT_CERT=$GEN_DIR/client.pem
CLIENT_TRUST=$GEN_DIR/client-truststore.jks

echo remove directory $GEN_DIR if exists
[ -e $GEN_DIR ] && rm -rf $GEN_DIR

echo create directory $GEN_DIR
mkdir -p $GEN_DIR

echo generate server certificates
keytool -alias server -dname "cn=localhost, ou=ssl-server, o=csb-http-ssl, c=US" -genkeypair -storepass $K_PASS -keyalg RSA -keystore $SERVER_JKS

echo generate client certificates
keytool -alias client -dname "cn=localhost, ou=ssl-client, o=csb-http-ssl, c=US" -genkeypair -storepass $K_PASS -keyalg RSA -keystore $CLIENT_JKS

echo export server certificates
keytool -exportcert -alias server -storepass $K_PASS -keystore $SERVER_JKS -rfc -file $SERVER_CERT

echo export client certificates
keytool -exportcert -alias client -storepass $K_PASS -keystore $CLIENT_JKS -rfc -file $CLIENT_CERT

echo import server in client truststore
keytool -import -keystore $CLIENT_TRUST -storepass $K_PASS -file $SERVER_CERT -alias server -noprompt -trustcacerts

echo import client in server truststore
keytool -import -keystore $SERVER_TRUST -storepass $K_PASS -file $CLIENT_CERT -alias client -noprompt -trustcacerts

echo copy $SERVER_JKS in ssl-server/src/main/resources
[ -e ssl-server/src/main/resources/server.jks ] && rm ssl-server/src/main/resources/server.jks
cp $SERVER_JKS ssl-server/src/main/resources/server.jks

echo copy $SERVER_TRUST in ssl-server/src/main/resources
[ -e ssl-server/src/main/resources/server-truststore.jks ] && rm ssl-server/src/main/resources/server-truststore.jks
cp $SERVER_TRUST ssl-server/src/main/resources/server-truststore.jks

echo copy $CLIENT_JKS in ssl-client/src/main/resources
[ -e ssl-client/src/main/resources/client.jks ] && rm ssl-client/src/main/resources/client.jks
cp $CLIENT_JKS ssl-client/src/main/resources/client.jks

echo copy $CLIENT_TRUST in ssl-client/src/main/resources
[ -e ssl-client/src/main/resources/client-truststore.jks ] && rm ssl-client/src/main/resources/client-truststore.jks
cp $CLIENT_TRUST ssl-client/src/main/resources/client-truststore.jks

echo copy $SERVER_JKS in ssl-camel-server/src/main/resources
[ -e ssl-camel-server/src/main/resources/server.jks ] && rm ssl-camel-server/src/main/resources/server.jks
cp $SERVER_JKS ssl-camel-server/src/main/resources/server.jks
