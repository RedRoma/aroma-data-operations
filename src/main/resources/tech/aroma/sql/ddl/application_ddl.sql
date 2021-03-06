------------------------------------------------------------------------------
-- APPLICATIONS PROVISIONED TO WORK WITH AROMA
------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS Applications
(
    app_id                   UUID,
    app_name                 TEXT,
    app_description          TEXT,
    organization_id          UUID,
    owners                   UUID[],
    programming_language     TEXT,
    time_provisioned         TIMESTAMPTZ DEFAULT now(),
    time_last_updated        TIMESTAMPTZ DEFAULT now(),
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