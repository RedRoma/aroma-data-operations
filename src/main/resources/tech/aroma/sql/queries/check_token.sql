------------------------------------------------------------------------------
-- CHECKS WHETHER A TOKEN EXISTS OR NOT
------------------------------------------------------------------------------

SELECT count(token_id)
FROM tokens
WHERE token_id = ?