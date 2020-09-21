
CREATE TABLE IF NOT EXISTS akka.tag_scanning(
  persistence_id text,
  sequence_nr bigint,
  PRIMARY KEY (persistence_id));
