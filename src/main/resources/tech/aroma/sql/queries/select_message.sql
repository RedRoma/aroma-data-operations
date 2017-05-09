------------------------------------------------------------------------------
-- GETS A MESSAGE USING THE MESSAGE ID AND APP ID
------------------------------------------------------------------------------

SELECT *
FROM messages
WHERE app_id = ?
AND message_id = ?