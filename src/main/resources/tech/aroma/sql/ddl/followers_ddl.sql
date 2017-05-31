------------------------------------------------------------------------------
-- STORES AN APP'S FOLLOWERS AND REMEMBERS WHAT APP A User Follows
------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS Followers
(
    app_id         UUID,
    user_id        UUID,
    time_of_follow TIMESTAMP DEFAULT now(),

    PRIMARY KEY (user_id, app_id)
)
;

CREATE INDEX IF NOT EXISTS App_Followers
    ON Followers (app_id)
;