/******************************************************************
New tables
******************************************************************/
DROP TABLE IF EXISTS record;
CREATE TABLE record (
  id          BIGINT       NOT NULL IDENTITY PRIMARY KEY,
  record_number INTEGER NOT NULL,
  content varchar(255)

);
CREATE INDEX record_id_idx
  ON record (id);
