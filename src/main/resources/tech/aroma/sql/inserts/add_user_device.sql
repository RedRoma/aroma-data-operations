------------------------------------------------------------------------------
-- ADDS A NEW DEVICE.
------------------------------------------------------------------------------

INSERT INTO user_preferences (user_id, serialized_devices)
VALUES (?, ARRAY [?])
ON CONFLICT
    DO UPDATE
        SET serialized_devices = serialized_devices || EXCLUDED.serialized_devices