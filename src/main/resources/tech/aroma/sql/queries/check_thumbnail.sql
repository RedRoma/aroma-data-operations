------------------------------------------------------------------------------
-- CHECKS WHETHER A MESSAGE EXISTS IN AN INBOX OR NOT
------------------------------------------------------------------------------

SELECT count(*) > 0
FROM Media_Thumbnails
WHERE media_id = ?
      AND width = ?
      AND height = ?