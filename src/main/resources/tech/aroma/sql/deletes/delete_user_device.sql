------------------------------------------------------------------------------
-- DELETES A USER'S DEVICES
------------------------------------------------------------------------------

UPDATE user_preferences
SET serialized_devices = array_remove(serialized_devices, ?)
WHERE user_id = ?