------------------------------------------------------------------------------
-- INSERTS A NEW TOKEN
------------------------------------------------------------------------------

INSERT INTO tokens (token_id, owner_id, organization_id, owner_name, time_of_creation, time_of_expiration, token_type, token_staus)
VALUES (?, ?, ?, ?, ?, ?, ?, ?)