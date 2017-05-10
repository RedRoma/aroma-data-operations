------------------------------------------------------------------------------
-- CHECKS WHETHER A USER BELONGS TO AN ORGANIZATION OR NOT
------------------------------------------------------------------------------

SELECT count(*) > 0
FROM organization_members
WHERE org_id = ?
AND user_id = ?