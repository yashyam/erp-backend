INSERT INTO roles (name, description)
VALUES ('ROLE_USER', 'Default application role for ERP users')
ON CONFLICT (name) DO NOTHING;

INSERT INTO users (
    email,
    username,
    password_hash,
    first_name,
    last_name,
    enabled,
    account_non_locked,
    account_non_expired,
    credentials_non_expired,
    status
)
VALUES (
    'demo@demo.com',
    'demo',
    crypt('Demo@12345', gen_salt('bf', 10)),
    'Demo',
    'User',
    TRUE,
    TRUE,
    TRUE,
    TRUE,
    'ACTIVE'
)
ON CONFLICT (username)
DO UPDATE SET
    email = EXCLUDED.email,
    password_hash = EXCLUDED.password_hash,
    first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    enabled = EXCLUDED.enabled,
    account_non_locked = EXCLUDED.account_non_locked,
    account_non_expired = EXCLUDED.account_non_expired,
    credentials_non_expired = EXCLUDED.credentials_non_expired,
    status = EXCLUDED.status,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ROLE_USER'
WHERE u.username = 'demo'
ON CONFLICT DO NOTHING;
