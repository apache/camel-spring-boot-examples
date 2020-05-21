#!/usr/bin/env bash

rm truststore.jks

kubectl get secret/my-cluster-cluster-ca-cert -o 'go-template={{index .data "ca.crt"}}' -n camel-example-strimzi | base64 -D > ca.crt

echo "yes" | keytool -import -trustcacerts -file ca.crt -keystore truststore.jks -storepass 123456

kubectl delete configmap truststore-config && kubectl create configmap truststore-config --from-file=truststore.jks=truststore.jks -n camel-example-strimzi

rm ca.crt