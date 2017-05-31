------------------------------------------------------------------------------
-- GETS A USER'S ENCRYPTED PASSWORD
------------------------------------------------------------------------------

SELECT encrypted_password
FROM credentials
WHERE user_id = ?