#!/bin/bash

CYAN="\033[36m"
NORMAL="\033[0m"

message() {
  echo -e "${CYAN}[$1]${NORMAL}"
}

message "Setting up kubernetes context and namespace..."
kubectl config use-context docker-for-desktop
#eval $(minikube docker-env)
sbt pcs/docker:publishLocal
sbt readside/docker:publishLocal

kubectl apply -f assets/k8s/namespace.yml
kubectl config set-context --current --namespace=copernico
message "Kubernetes setup completed."

message "Starting up kafka"
kubectl apply -f assets/k8s/infra/kafka.yml
message "kafka started up"

message "Starting up cassandra"
kubectl apply -f assets/k8s/infra/cassandra.yml
message "cassandra started up"

message "awainting for cassandra to be ready"
sleep 30

message "Setting up cassandra"
export pod_name=$(kubectl get pod --selector app=cassandra | grep cassandra | cut -d' ' -f 1)

# call setup_cassandra.sh
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/keyspaces/akka.cql

kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/keyspaces/akka.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/keyspaces/akka_snapshot.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/tables/all_persistence_ids.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/tables/messages.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/tables/metadata.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/tables/snapshots.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/tables/tag_scanning.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/tables/tag_views.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/infrastructure/akka/tables/tag_write_progress.cql

kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/infrastructure/cqrs/keyspaces/akka_projection.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/infrastructure/cqrs/tables/offset_store.cql

kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/keyspaces/read_side.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_actividades_sujeto.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_contactos.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_declaraciones_juradas.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_domicilios_objeto.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_domicilios_sujeto.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_exenciones.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_juicios.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_obligaciones.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_planes_pago.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_subastas.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_sujeto.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_sujeto_objeto.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_tramites.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_etapas_procesales.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_param_plan.cql
kubectl exec -i $pod_name cqlsh < assets/scripts/cassandra/domain/read_side/tables/buc_param_recargo.cql
message "cassandra setup completed."


message "Starting up write side"
kubectl apply -f assets/k8s/pcs/pcs-rbac.yml
kubectl apply -f assets/k8s/pcs/pcs-service.yml
kubectl apply -f assets/k8s/pcs/pcs-deployment.yml
kubectl apply -f assets/k8s/pcs/pcs-service-monitor.yml

#helm install prometheus stable/prometheus-operator --namespace copernico
# grafana password is prom-operator
#kubectl apply -f assets/k8s/pcs/pcs-service-monitor.yml


message "write side started"

message "Starting up read side"
kubectl apply -f assets/k8s/readside/readside-rbac.yml
kubectl apply -f assets/k8s/readside/readside-service.yml
kubectl apply -f assets/k8s/readside/readside-deployment.yml
kubectl apply -f assets/k8s/readside/readside-service-monitor.yml
message "read side started"
