------------------------------------------------------------------------------
-- THIS TABLE STORES ANY ACTIVITY THAT HAPPENS WITHIN AROMA
-- THAT MAY BE OF INTEREST TO USERS
------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS Activity
(
    recipient_user_id UUID,
    event_id          UUID,
    app_id            UUID,
    actor_user_id     UUID,
    time_of_event     TIMESTAMP,
    event_type        TEXT,
    serialized_event  TEXT,

    PRIMARY KEY (recipient_user_id, event_id)
);


CREATE INDEX IF NOT EXISTS Activity_By_Application
    ON Activity (app_id);