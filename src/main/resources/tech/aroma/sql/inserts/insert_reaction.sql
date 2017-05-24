------------------------------------------------------------------------------
-- INSERT REACTIONS for a User
------------------------------------------------------------------------------

INSERT INTO reactions (owner_id, serialized_reactions)
VALUES (?, string_to_array(?, ','))
ON CONFLICT
    DO UPDATE
        SET serialized_reactions = EXCLUDED.serialized_reactions