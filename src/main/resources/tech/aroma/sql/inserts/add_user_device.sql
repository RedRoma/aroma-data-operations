------------------------------------------------------------------------------
-- ADDS A NEW DEVICE
------------------------------------------------------------------------------

INSERT INTO user_preferences (user_id, serialized_devices)
VALUES (?, ARRAY[?])
ON CONFLICT (user_id)
    DO UPDATE
        SET serialized_devices = user_preferences.serialized_devices || EXCLUDED.serialized_devices