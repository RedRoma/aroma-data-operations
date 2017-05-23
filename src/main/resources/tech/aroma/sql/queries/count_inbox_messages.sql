------------------------------------------------------------------------------
-- COUNT ALL OF THE MESSAGES IN A USER'S INBOX
------------------------------------------------------------------------------

SELECT count(message_id)
FROM inbox
WHERE user_id = ?