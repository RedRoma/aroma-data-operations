---------------------------------------
-- DDL FOR AROMA TOKENS
--
-- Tokens are used to authenticate Users
-- and Applications
---------------------------------------

CREATE TABLE IF NOT EXISTS Tokens
(
    token_id           UUID,
    owner_id           UUID,
    organization_id    UUID,
    owner_name         TEXT,
    time_of_creation   TIMESTAMPTZ DEFAULT now(),
    time_of_expiration TIMESTAMPTZ DEFAULT now() + INTERVAL '30 Days',
    token_type         TEXT,
    token_status       TEXT,

    PRIMARY KEY (token_id)
);

CREATE INDEX IF NOT EXISTS Tokens_By_Type
    ON Tokens (token_type);

CREATE INDEX IF NOT EXISTS Tokens_By_Owner
    ON Tokens (owner_id);