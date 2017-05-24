------------------------------------------------------------------------------
-- DELETES AN EVENT FROM THE ACTIVITY TABLE
------------------------------------------------------------------------------

DELETE
FROM activity
WHERE recipient_user_id = ?
      AND event_id = ?