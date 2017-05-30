------------------------------------------------------------------------------
-- CHECKS WHETHER A MESSAGE EXISTS IN AN INBOX OR NOT
------------------------------------------------------------------------------

SELECT count(*) > 0
FROM Media
WHERE media_id = ?