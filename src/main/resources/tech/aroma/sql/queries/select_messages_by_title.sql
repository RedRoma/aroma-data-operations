------------------------------------------------------------------------------
-- SELECT MESSAGES BY TITLE
------------------------------------------------------------------------------

SELECT *
FROM messages
WHERE app_id = ?
      AND title = ?