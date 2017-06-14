------------------------------------------------------------------------------
-- SELECTS ALL OF THE MESSAGES BELONGING TO AN APP
------------------------------------------------------------------------------

SELECT *
FROM messages
WHERE app_id = ?
      AND time_created < now() - INTERVAL '2 days'