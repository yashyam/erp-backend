ALTER TABLE users
    ADD COLUMN auth_provider VARCHAR(30) NOT NULL DEFAULT 'LOCAL',
    ADD COLUMN provider_subject VARCHAR(255);

CREATE UNIQUE INDEX uk_users_provider_identity
    ON users (auth_provider, provider_subject)
    WHERE provider_subject IS NOT NULL;
