------------------------------------------------------------------------------
-- CHECKS WHETHER A MESSAGE EXISTS OR NOT
------------------------------------------------------------------------------

SELECT count(*) > 0
FROM messages
WHERE app_id = ?
AND message_id = ?