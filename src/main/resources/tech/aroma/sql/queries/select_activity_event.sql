------------------------------------------------------------------------------
-- SELECTS AN EVENT FROM THE ACTIVITY TABLE
------------------------------------------------------------------------------

SELECT *
FROM activity
WHERE recipient_user_id = ?
      AND event_id = ?