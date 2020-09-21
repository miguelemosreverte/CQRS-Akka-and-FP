CREATE KEYSPACE IF NOT EXISTS akka_projection
WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
