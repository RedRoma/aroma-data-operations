------------------------------------------------------------------------------
-- SELECTS AN EVENT FROM THE ACTIVITY TABLE
------------------------------------------------------------------------------

SELECT serialized_event
FROM activity
WHERE recipient_user_id = ?
      AND event_id = ?