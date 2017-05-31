------------------------------------------------------------------------------
-- CHECKS WHETHER A USER'S CREDENTIALS EXIST
------------------------------------------------------------------------------

SELECT count(encrypted_password) > 0
FROM credentials
WHERE user_id = ?