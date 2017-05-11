------------------------------------------------------------------------------
-- SELECTS ALL OF THE RECENTLY UPDATED APPLICATIONS
------------------------------------------------------------------------------

SELECT *
FROM applications
WHERE time_last_updated > now() - INTERVAL '48 hours'