INSERT INTO roles (name, description)
VALUES
    ('ROLE_SUPER_ADMIN', 'Full system access, including user and role administration'),
    ('ROLE_ADMIN', 'Administrative access to master data, transactions, and reports'),
    ('ROLE_SUPERVISOR', 'Supervisory access to purchase transactions and reports'),
    ('ROLE_WORKER', 'Operational access to create and view purchase transactions'),
    ('ROLE_TECHNICAL_SUPPORT', 'Support access to inspect users, permissions, and application data')
ON CONFLICT (name) DO UPDATE
SET description = EXCLUDED.description;

INSERT INTO permissions (name, resource, action, description)
VALUES
    ('USER_READ', 'USER', 'READ', 'View users and assigned roles'),
    ('USER_ROLE_ASSIGN', 'USER', 'ROLE_ASSIGN', 'Assign roles to users'),
    ('MASTER_DATA_READ', 'MASTER_DATA', 'READ', 'View suppliers, customers, and godowns'),
    ('MASTER_DATA_WRITE', 'MASTER_DATA', 'WRITE', 'Create, update, and delete master data'),
    ('KAPAS_PURCHASE_READ', 'KAPAS_PURCHASE', 'READ', 'View kapas purchase entries'),
    ('KAPAS_PURCHASE_CREATE', 'KAPAS_PURCHASE', 'CREATE', 'Create kapas purchase entries'),
    ('KAPAS_PURCHASE_UPDATE', 'KAPAS_PURCHASE', 'UPDATE', 'Update kapas purchase entries'),
    ('KAPAS_PURCHASE_DELETE', 'KAPAS_PURCHASE', 'DELETE', 'Delete kapas purchase entries'),
    ('REPORT_READ', 'REPORT', 'READ', 'View operational reports')
ON CONFLICT (name) DO UPDATE
SET description = EXCLUDED.description;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON
    r.name = 'ROLE_SUPER_ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN (
    'MASTER_DATA_READ', 'MASTER_DATA_WRITE',
    'KAPAS_PURCHASE_READ', 'KAPAS_PURCHASE_CREATE',
    'KAPAS_PURCHASE_UPDATE', 'KAPAS_PURCHASE_DELETE', 'REPORT_READ'
)
WHERE r.name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN (
    'MASTER_DATA_READ', 'KAPAS_PURCHASE_READ',
    'KAPAS_PURCHASE_CREATE', 'KAPAS_PURCHASE_UPDATE', 'REPORT_READ'
)
WHERE r.name = 'ROLE_SUPERVISOR'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN ('MASTER_DATA_READ', 'KAPAS_PURCHASE_READ', 'KAPAS_PURCHASE_CREATE')
WHERE r.name = 'ROLE_WORKER'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN ('USER_READ', 'MASTER_DATA_READ', 'KAPAS_PURCHASE_READ', 'REPORT_READ')
WHERE r.name = 'ROLE_TECHNICAL_SUPPORT'
ON CONFLICT DO NOTHING;
