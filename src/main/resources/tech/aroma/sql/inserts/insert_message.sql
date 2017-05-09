-- INSERTS A NEW MESSAGE INTO THE TABLE
-- ========================================

INSERT INTO messages (message_id, title, body, priority, time_created, time_received, expiration, hostname, ip_address, app_id, app_name, device_name)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)