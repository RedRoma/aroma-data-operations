------------------------------------------------------------------------------
-- INSERT REACTIONS for a User
------------------------------------------------------------------------------

INSERT INTO reactions (owner_id, serialized_reactions)
VALUES (?, ?)
-- ON CONFLICT (owner_id)
--     DO UPDATE
--         SET serialized_reactions = EXCLUDED.serialized_reactions