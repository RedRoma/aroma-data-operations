------------------------------------------------------------------------------
-- DELETES A USER'S DEVICES
------------------------------------------------------------------------------

UPDATE user_preferences
SET serialized_devices = ARRAY[]
WHERE user_id = ?