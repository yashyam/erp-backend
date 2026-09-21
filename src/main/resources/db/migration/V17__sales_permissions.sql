INSERT INTO permissions (name, resource, action, description)
VALUES
    ('SALES_READ', 'SALES', 'READ', 'View sales'),
    ('SALES_CREATE', 'SALES', 'CREATE', 'Create sales'),
    ('SALES_UPDATE', 'SALES', 'UPDATE', 'Update sales'),
    ('SALES_DELETE', 'SALES', 'DELETE', 'Delete sales')
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.name = 'ROLE_ADMIN' AND p.name IN ('SALES_READ', 'SALES_CREATE', 'SALES_UPDATE', 'SALES_DELETE')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.name = 'ROLE_SUPERVISOR' AND p.name IN ('SALES_READ', 'SALES_CREATE', 'SALES_UPDATE')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.name = 'ROLE_WORKER' AND p.name IN ('SALES_READ', 'SALES_CREATE')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.name = 'ROLE_TECHNICAL_SUPPORT' AND p.name = 'SALES_READ'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.name = 'ROLE_SUPER_ADMIN'
ON CONFLICT DO NOTHING;
