------------------------------------------------------------------------------
-- DELETES A MESSAGE
------------------------------------------------------------------------------

DELETE
FROM messages
WHERE app_id = ?
      AND message_id = ?