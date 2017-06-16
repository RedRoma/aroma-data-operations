---------------------------------------
-- DDL FOR AROMA REACTIONS
--
--  Used to store User Preferences and secondary information about Users, such as:
-- 	    + Mobile Devices they own or use (for push notifications)
-- 	    + Their Personal Reactions which are run on messages that show up in their Inbox.
-- 	    + User Activity

---------------------------------------

CREATE TABLE IF NOT EXISTS User_Preferences
(
    user_id            UUID,
    serialized_devices TEXT [] DEFAULT ARRAY[]::TEXT[],
    last_sign_in       TIMESTAMPTZ DEFAULT now(),

    PRIMARY KEY (user_id)
);