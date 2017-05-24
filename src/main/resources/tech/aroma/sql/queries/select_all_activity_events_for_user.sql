------------------------------------------------------------------------------
-- SELECTS ALL A USER'S EVENTS
------------------------------------------------------------------------------

SELECT serialized_event
FROM activity
WHERE recipient_user_id = ?