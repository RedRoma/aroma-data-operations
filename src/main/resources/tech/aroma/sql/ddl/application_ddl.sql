------------------------------------------------------------------------------
-- APPLICATIONS PROVISIONED TO WORK WITH AROMA
------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS Applications
(
    app_id                   UUID,
    name                     TEXT,
    app_description          TEXT,
    organization_id          UUID,
    organization_name        TEXT,
    programming_language     TEXT,
    time_provisioned         TIMESTAMP DEFAULT now(),
    time_last_updated        TIMESTAMP DEFAULT now(),
    tier                     TEXT,
    time_of_token_expiration TIMESTAMP,
    app_icon_media_id        UUID,

    PRIMARY KEY (app_id)
);

CREATE INDEX IF NOT EXISTS Applications_By_Organization
    ON Applications (organization_id);

-- USED FOR REMEMBERING AN Application's Owners
CREATE TABLE IF NOT EXISTS Application_Owners
(
    app_id   UUID,
    owner_id UUID,
    since    TIMESTAMP DEFAULT now(),

    PRIMARY KEY (app_id, owner_id)
)