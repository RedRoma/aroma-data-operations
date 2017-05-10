---------------------------------------
-- DDL FOR AROMA MESSAGES
--
-- Organizations represent Businesses and other
-- groups of people that use Aroma.
---------------------------------------

CREATE TABLE IF NOT EXISTS Organizations
(
  org_id UUID,
  org_name TEXT,
  owners TEXT[],
  icon_link TEXT,
  industry TEXT,
  contact_email TEXT,
  github_profile TEXT,
  stock_name TEXT,
  tier TEXT,
  description TEXT,
  website TEXT,

  PRIMARY KEY (org_id)
);

CREATE INDEX IF NOT EXISTS Organizations_By_Tier ON Organizations(tier);

CREATE INDEX IF NOT EXISTS Organizations_By_Industry ON Organizations(industry);

-- Stores Information about an Organization's owners
CREATE TABLE IF NOT EXISTS Organization_Owners
(
  org_id UUID,
  user_id UUID,
  time_joined TIMESTAMP,

  PRIMARY KEY (org_id, user_id)
);

CREATE INDEX IF NOT EXISTS Organization_Owners_By_User ON Organization_Owners(user_id);


-- Stores Information relating to an Organization's members
CREATE TABLE IF NOT EXISTS Organization_Members
(
  org_id UUID,
  user_id UUID,
  time_joined TIMESTAMP,

  PRIMARY KEY (org_id, user_id)
);

CREATE INDEX IF NOT EXISTS Organization_Members_By_Email ON Organization_Members(user_email);
CREATE INDEX IF NOT EXISTS Organization_Members_By_User ON Organization_Members(user_id);