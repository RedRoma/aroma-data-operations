------------------------------------------------------------------------------
-- CHECKS WHETHER AN ORGANIZATION EXISTS OR NOT
------------------------------------------------------------------------------

SELECT count(*) > 0
FROM organizations
WHERE organization_id = ?