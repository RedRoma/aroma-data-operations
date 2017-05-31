---------------------------------------
-- DDL FOR AROMA REACTIONS
--
-- Reactions are actions taken by Aroma when messages are received
-- matching a given criteria
---------------------------------------

CREATE TABLE IF NOT EXISTS Reactions
(
    owner_id             UUID,
    serialized_reactions TEXT[] DEFAULT ARRAY[]::TEXT[],

    PRIMARY KEY (owner_id)
);