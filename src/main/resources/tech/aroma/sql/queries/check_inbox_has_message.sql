------------------------------------------------------------------------------
-- CHECKS WHETHER A MESSAGE EXISTS IN AN INBOX OR NOT
------------------------------------------------------------------------------

SELECT count(*) > 0
FROM inbox
WHERE user_id = ?
      AND message_id = ?