
CREATE TABLE IF NOT EXISTS akka.metadata(
  persistence_id text PRIMARY KEY,
  deleted_to bigint,
  properties map<text,text>);