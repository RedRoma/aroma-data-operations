------------------------------------------------------------------------------
-- CHECKS WHETHER A USER EXISTS OR NOT
------------------------------------------------------------------------------

SELECT count(*) > 0
FROM users
WHERE user_id = ?