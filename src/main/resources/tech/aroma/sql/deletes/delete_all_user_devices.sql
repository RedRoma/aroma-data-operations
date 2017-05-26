------------------------------------------------------------------------------
-- DELETES A USER'S DEVICES
------------------------------------------------------------------------------

UPDATE user_preferences
SET serialized_devices = NULL
WHERE user_id = ?