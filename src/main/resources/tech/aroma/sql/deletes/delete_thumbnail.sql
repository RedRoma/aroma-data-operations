------------------------------------------------------------------------------
-- DELETES AN IMAGE THUMBNAIL
------------------------------------------------------------------------------

DELETE
FROM Media_Thumbnails
WHERE media_id = ?
      AND width = ?
      AND height = ?