---------------------------------------
-- DDL FOR AROMA INBOX MESSAGES
--
-- The inbox is where messages are kept individually for
-- Aroma users
---------------------------------------

CREATE TABLE IF NOT EXISTS Inbox
(
    user_id       UUID,
    message_id    UUID,
    app_id        UUID,
    app_name      TEXT,
    title         TEXT,
    body          TEXT,
    priority      TEXT,
    time_created  TIMESTAMP DEFAULT now(),
    time_received TIMESTAMP DEFAULT now(),
    hostname      TEXT,
    mac_address   TEXT,
    device_name   TEXT,

    PRIMARY KEY (user_id, message_id)
);