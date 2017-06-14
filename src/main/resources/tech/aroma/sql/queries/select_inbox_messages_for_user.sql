------------------------------------------------------------------------------
-- GETS THE MESSAGES IN A USER'S INBOX
------------------------------------------------------------------------------

SELECT *
FROM inbox
WHERE user_id = ?
      AND time_created > now() - INTERVAL '3 days'