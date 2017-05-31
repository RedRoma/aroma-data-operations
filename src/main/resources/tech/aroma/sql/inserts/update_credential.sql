------------------------------------------------------------------------------
-- UPDATES A USER'S CREDENTIALS
------------------------------------------------------------------------------

INSERT INTO credentials (user_id, encrypted_password)
VALUES (?, ?)
ON CONFLICT (user_id)
    DO UPDATE
        SET encrypted_password = EXCLUDED.encrypted_password,
            time_created       = now()