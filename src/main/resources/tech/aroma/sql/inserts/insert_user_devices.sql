------------------------------------------------------------------------------
-- INSERT A USER'S DEVICES
------------------------------------------------------------------------------

INSERT INTO user_preferences (user_id, serialized_devices)
VALUES (?, ?)
ON CONFLICT (user_id)
    DO UPDATE
        SET serialized_devices = EXCLUDED.serialized_devices