------------------------------------------------------------------------------
-- RECORDS AN APP FOLLOW
------------------------------------------------------------------------------

INSERT INTO Followings (app_id, user_id)
VALUES (app_id, user_id)
ON CONFLICT (app_id, user_id)
    DO NOTHING