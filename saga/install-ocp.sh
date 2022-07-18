#!/bin/bash

echo creating amq-broker instance
oc create -f ocp-resources/amq-broker.yaml

echo creating lra-coordinator instance
oc create -f ocp-resources/lra-coordinator.yaml

echo deploying services
mvn clean package -Popenshift
