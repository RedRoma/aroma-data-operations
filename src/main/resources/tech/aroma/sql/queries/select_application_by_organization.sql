------------------------------------------------------------------------------
-- SELECTS ALL OF AN ORGANIZATION'S APPLICATIONS
------------------------------------------------------------------------------

SELECT *
FROM applications
WHERE organization_id = ?