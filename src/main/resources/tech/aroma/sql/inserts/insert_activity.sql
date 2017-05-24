------------------------------------------------------------------------------
-- INSERTS A NEW EVENT INTO THE ACTIVITY TABLE
------------------------------------------------------------------------------

INSERT INTO activity (recipient_user_id,
                      event_id,
                      app_id,
                      actor_user_id,
                      time_of_event,
                      event_type,
                      serialized_event)

VALUES (?, ?, ?, ?, ?, ?, ?)