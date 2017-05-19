---------------------------------------
-- DDL FOR AROMA USERS
--
-- USERS ARE ACTUAL PEOPLE THAT INTERACT
-- WITH THE AROMA SERVICE OR APP
---------------------------------------

CREATE TABLE IF NOT EXISTS Users
(
    user_id              UUID,
    first_name           TEXT,
    middle_name          TEXT,
    last_name            TEXT,
    full_name            TEXT,
    email                TEXT UNIQUE NOT NULL,
    roles                TEXT[],
    gender               TEXT,
    birthdate            DATE,
    profile_image_id     UUID,
    github_profile       TEXT UNIQUE,
    time_account_created TIMESTAMP DEFAULT now(),
    time_last_updated    TIMESTAMP DEFAULT now(),

    PRIMARY KEY (user_id)
);


CREATE INDEX IF NOT EXISTS Users_By_Last_Name
    ON Users (last_name);

CREATE INDEX IF NOT EXISTS Users_By_First_Name
    ON Users (first_name);

CREATE INDEX IF NOT EXISTS Users_By_Email
    ON Users (email);

CREATE INDEX IF NOT EXISTS Users_By_Github
    ON Users (github_profile);