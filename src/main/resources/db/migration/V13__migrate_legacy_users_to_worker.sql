INSERT INTO user_roles (user_id, role_id)
SELECT ur.user_id, worker.id
FROM user_roles ur
JOIN roles legacy ON legacy.id = ur.role_id AND legacy.name = 'ROLE_USER'
JOIN roles worker ON worker.name = 'ROLE_WORKER'
ON CONFLICT DO NOTHING;

DELETE FROM user_roles
WHERE role_id IN (SELECT id FROM roles WHERE name = 'ROLE_USER');
