CREATE TABLE todo (
  id SERIAL PRIMARY KEY,
  title VARCHAR NOT NULL,
  completed BOOLEAN NOT NULL,
  ordering INTEGER
);