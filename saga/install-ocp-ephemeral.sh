#!/bin/bash

echo creating amq-broker instance
oc create -f ocp-resources/amq-broker-ephemeral.yaml

echo creating lra-coordinator instance
oc create -f ocp-resources/lra-coordinator-ephemeral.yaml

echo deploying services
mvn clean package -Popenshift
