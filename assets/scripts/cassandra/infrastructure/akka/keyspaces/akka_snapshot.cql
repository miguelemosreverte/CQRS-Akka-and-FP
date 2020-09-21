

CREATE KEYSPACE IF NOT EXISTS akka_snapshot
 WITH REPLICATION = { 'class' : 'SimpleStrategy','replication_factor':1 };