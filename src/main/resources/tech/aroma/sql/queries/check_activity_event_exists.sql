------------------------------------------------------------------------------
-- CHECKS WHETHER A MESSAGE EXISTS OR NOT
------------------------------------------------------------------------------

SELECT count(*) > 0
FROM activity
WHERE recipient_user_id = ?
      AND event_id = ?