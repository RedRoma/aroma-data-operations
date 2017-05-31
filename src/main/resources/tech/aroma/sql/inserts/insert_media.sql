------------------------------------------------------------------------------
-- INSERTS A NEW MEDIA ITEM
------------------------------------------------------------------------------

INSERT INTO Media (media_id, media_type, width, height, data, creation_time)
VALUES (?, ?, ?, ?, ?, now())