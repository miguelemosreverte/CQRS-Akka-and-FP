CREATE KEYSPACE IF NOT EXISTS akka
WITH REPLICATION = { 'class' : 'SimpleStrategy','replication_factor':1 };
