------------------------------------------------------------------------------
-- INSERTS A NEW TOKEN
------------------------------------------------------------------------------

INSERT INTO tokens (token_id, owner_id, organization_id, owner_name, time_of_creation, time_of_expiration, token_type, token_status)
VALUES (?, ?, ?, ?, ?, ?, ?, ?)
ON CONFLICT (token_id)
    DO UPDATE
        SET
            owner_name         = EXCLUDED.owner_name,
            time_of_expiration = EXCLUDED.time_of_expiration,
            token_type         = EXCLUDED.token_type,
            token_status       = EXCLUDED.token_status