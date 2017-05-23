------------------------------------------------------------------------------
-- INSERTS A NEW MESSAGE INTO THE INBOX TABLE
------------------------------------------------------------------------------

INSERT INTO inbox (user_id,
                   message_id,
                   app_id,
                   app_name,
                   title,
                   body,
                   priority,
                   time_created,
                   time_received,
                   hostname,
                   mac_address,
                   device_name)

VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)