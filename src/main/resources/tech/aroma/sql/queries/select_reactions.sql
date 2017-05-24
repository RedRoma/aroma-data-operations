------------------------------------------------------------------------------
-- GETS A MESSAGE USING THE MESSAGE ID AND APP ID
------------------------------------------------------------------------------

SELECT serialized_reactions
FROM reactions
WHERE owner_id = ?