------------------------------------------------------------------------------
-- CHECKS WHETHER AN APPLICATION EXISTS OR NOT
------------------------------------------------------------------------------

SELECT count(app_id) > 0
FROM applications
WHERE app_id = ?