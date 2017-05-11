------------------------------------------------------------------------------
-- CHECKS WHETHER A USER BELONGS TO AN ORGANIZATION OR NOT
------------------------------------------------------------------------------

SELECT count(*) > 0
FROM organization_members
WHERE organization_id = ?
AND user_id = ?