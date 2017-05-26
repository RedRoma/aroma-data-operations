------------------------------------------------------------------------------
-- SELECTS A USER'S MOBILE DEVICES FROM THE USER-PREFERENCES TABLE
------------------------------------------------------------------------------

SELECT serialized_devices
FROM user_preferences
WHERE user_id = ?