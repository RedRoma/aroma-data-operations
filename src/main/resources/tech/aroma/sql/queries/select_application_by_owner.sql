------------------------------------------------------------------------------
-- SELECTS AND APPLICATION BY OWNER
------------------------------------------------------------------------------

-- SELECT *
-- FROM applications
-- WHERE ? = ANY (owners)

SELECT *
FROM application_owners
    INNER JOIN applications USING (app_id)
WHERE owner_id = ?