DROP TABLE IF EXISTS messages;
CREATE TABLE messages (
    message VARCHAR(255),
    ts TIMESTAMP NOT NULL DEFAULT now()
);