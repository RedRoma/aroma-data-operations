---------------------------------------
-- DDL FOR AROMA MESSAGES
--
-- Messages are what Aroma logs when our
-- customers send us messages.
---------------------------------------

CREATE TABLE IF NOT EXISTS Messages
(
    message_id    UUID,
    app_id        UUID,
    app_name      TEXT,
    title         TEXT,
    body          TEXT,
    priority      TEXT,
    time_created  TIMESTAMPTZ,
    time_received TIMESTAMPTZ DEFAULT now(),
    expiration    TIMESTAMPTZ DEFAULT (now() + INTERVAL '3 days'),
    hostname      TEXT,
    ip_address    TEXT,
    device_name   TEXT,

    PRIMARY KEY (app_id, message_id)
);

CREATE INDEX IF NOT EXISTS Message_By_Title
    ON Messages (title);

CREATE INDEX IF NOT EXISTS Messages_By_Hostname
    ON Messages (hostname);

CREATE INDEX IF NOT EXISTS Messages_By_Device_Name
    ON Messages (device_name);

-- RATE LIMITING
CREATE TABLE IF NOT EXISTS Messages_Rate_Limit
(
    app_id       UUID,
    request_time TIMESTAMP,

    PRIMARY KEY (app_id, request_time)
);

-- MESSAGE TOTALS BY APP
CREATE TABLE IF NOT EXISTS Message_Totals_By_App
(
    app_id         UUID,
    total_messages BIGINT DEFAULT 0,

    PRIMARY KEY (app_id)
);

-- MESSAGE TOTALS BY TITLE
CREATE TABLE IF NOT EXISTS Message_Totals_By_Title
(
    app_id         UUID,
    title          TEXT,
    total_messages BIGINT DEFAULT 0,

    PRIMARY KEY (app_id, title)
);