------------------------------------------------------------------------------
-- GETS A MEDIA ITEM USING THE MEDIA ID
------------------------------------------------------------------------------

SELECT *
FROM Media_Thumbnails
WHERE media_id = ?
      AND width = ?
      AND height = ?