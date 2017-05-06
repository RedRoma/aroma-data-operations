------------------------------------------------------------------------------
-- COUNT ALL OF THE MESSAGES FOR AN APP
------------------------------------------------------------------------------

SELECT count(message_id)
FROM messages
WHERE app_id = ?