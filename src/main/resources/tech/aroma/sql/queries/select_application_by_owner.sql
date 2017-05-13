------------------------------------------------------------------------------
-- SELECTS AND APPLICATION BY OWNER
------------------------------------------------------------------------------

SELECT *
FROM applications
WHERE ? = ANY (owners);

SELECT *
FROM applications
    INNER JOIN application_owners ON (app_id)
WHERE owner_id = ?