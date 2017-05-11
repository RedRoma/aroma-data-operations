------------------------------------------------------------------------------
-- INSERTS A NEW TOKEN
------------------------------------------------------------------------------

INSERT INTO tokens (token_id, owner_id, organization_id, owner_name, features, time_of_expiration, time_of_creation, token_type, token_staus)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)