------------------------------------------------------------------------------
-- INSERTS A NEW MESSAGE INTO THE TABLE
------------------------------------------------------------------------------

INSERT INTO messages (message_id, app_id, app_name, title, body, priority, time_created, time_received, hostname, ip_address, device_name)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)