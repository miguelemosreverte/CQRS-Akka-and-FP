#!/bin/bash

CYAN="\033[36m"
NORMAL="\033[0m"

message() {
  echo -e "${CYAN}[$1]${NORMAL}"
}

message "Stopping write side"
kubectl delete -f assets/k8s/pcs/pcs-service.yml
kubectl delete -f assets/k8s/pcs/pcs-rbac.yml
kubectl delete -f assets/k8s/pcs/pcs-deployment.yml
message "write side stopped"

message "Stopping read side"
kubectl delete -f assets/k8s/readside/readside-service.yml
kubectl delete -f assets/k8s/readside/readside-rbac.yml
kubectl delete -f assets/k8s/readside/readside-deployment.yml
message "read side stopped"

message "Stopping kafka"
kubectl delete -f assets/k8s/infra/kafka.yml
message "kafka stopped"

message "Stopping cassandra"
kubectl delete -f assets/k8s/infra/cassandra.yml
message "cassandra stopped"
