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
VALUES
    (
        'demo.superadmin@ginning.local',
        'demo_superadmin',
        crypt('SuperAdmin@123', gen_salt('bf', 10)),
        'Demo',
        'Super Admin',
        TRUE,
        TRUE,
        TRUE,
        TRUE,
        'ACTIVE'
    ),
    (
        'demo.admin@ginning.local',
        'demo_admin',
        crypt('Admin@123', gen_salt('bf', 10)),
        'Demo',
        'Admin',
        TRUE,
        TRUE,
        TRUE,
        TRUE,
        'ACTIVE'
    ),
    (
        'demo.supervisor@ginning.local',
        'demo_supervisor',
        crypt('Supervisor@123', gen_salt('bf', 10)),
        'Demo',
        'Supervisor',
        TRUE,
        TRUE,
        TRUE,
        TRUE,
        'ACTIVE'
    ),
    (
        'demo.worker@ginning.local',
        'demo_worker',
        crypt('Worker@123', gen_salt('bf', 10)),
        'Demo',
        'Worker',
        TRUE,
        TRUE,
        TRUE,
        TRUE,
        'ACTIVE'
    ),
    (
        'demo.support@ginning.local',
        'demo_support',
        crypt('Support@123', gen_salt('bf', 10)),
        'Demo',
        'Technical Support',
        TRUE,
        TRUE,
        TRUE,
        TRUE,
        'ACTIVE'
    )
ON CONFLICT (email) DO UPDATE SET
    username = EXCLUDED.username,
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
JOIN roles r ON r.name = 'ROLE_SUPER_ADMIN'
WHERE u.username = 'demo_superadmin'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ROLE_ADMIN'
WHERE u.username = 'demo_admin'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ROLE_SUPERVISOR'
WHERE u.username = 'demo_supervisor'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ROLE_WORKER'
WHERE u.username = 'demo_worker'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ROLE_TECHNICAL_SUPPORT'
WHERE u.username = 'demo_support'
ON CONFLICT DO NOTHING;
