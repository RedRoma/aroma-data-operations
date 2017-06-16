------------------------------------------------------------------------------
-- STORES AN APP'S FOLLOWERS AND REMEMBERS WHAT APP A User Follows
------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS Followings
(
    app_id         UUID,
    user_id        UUID,
    time_of_follow TIMESTAMPTZ DEFAULT now(),

    PRIMARY KEY (user_id, app_id)
)
;

CREATE INDEX IF NOT EXISTS App_Followers
    ON Followings (app_id)
;