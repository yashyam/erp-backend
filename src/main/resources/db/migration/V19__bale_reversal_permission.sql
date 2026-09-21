INSERT INTO permissions (name, resource, action, description)
VALUES ('BALE_DELETE', 'BALE', 'DELETE', 'Reverse a bale production record')
ON CONFLICT (name) DO UPDATE
SET description = EXCLUDED.description;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name = 'BALE_DELETE'
WHERE r.name IN ('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_SUPERVISOR')
ON CONFLICT DO NOTHING;
