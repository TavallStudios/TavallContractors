CREATE SCHEMA IF NOT EXISTS contractors;

CREATE TABLE IF NOT EXISTS contractors.users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    display_name VARCHAR(120) NOT NULL,
    primary_role VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS contractors.oauth_identities (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider VARCHAR(20) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    provider_email VARCHAR(320),
    CONSTRAINT fk_oauth_identities_user FOREIGN KEY (user_id) REFERENCES contractors.users(id)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_oauth_provider_user
    ON contractors.oauth_identities (provider, provider_user_id);

CREATE TABLE IF NOT EXISTS contractors.talent_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    headline VARCHAR(160),
    hourly_rate NUMERIC(12, 2) NOT NULL,
    CONSTRAINT fk_talent_profiles_user FOREIGN KEY (user_id) REFERENCES contractors.users(id)
);

CREATE TABLE IF NOT EXISTS contractors.talent_skills (
    talent_profile_id BIGINT NOT NULL,
    skill VARCHAR(80) NOT NULL,
    CONSTRAINT fk_talent_skills_profile FOREIGN KEY (talent_profile_id) REFERENCES contractors.talent_profiles(id)
);
CREATE INDEX IF NOT EXISTS idx_talent_skills_profile ON contractors.talent_skills (talent_profile_id);

CREATE TABLE IF NOT EXISTS contractors.checkout_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_key VARCHAR(80) NOT NULL,
    project_count INTEGER NOT NULL,
    total_budget NUMERIC(14, 2) NOT NULL,
    addon_count INTEGER NOT NULL DEFAULT 0,
    addon_total NUMERIC(14, 2) NOT NULL DEFAULT 0,
    grand_total NUMERIC(14, 2) NOT NULL DEFAULT 0,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(20) NOT NULL DEFAULT 'FUNDED',
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_checkout_records_user FOREIGN KEY (user_id) REFERENCES contractors.users(id)
);
CREATE INDEX IF NOT EXISTS idx_checkout_records_user ON contractors.checkout_records (user_id);
CREATE INDEX IF NOT EXISTS idx_checkout_records_session_key ON contractors.checkout_records (session_key);
