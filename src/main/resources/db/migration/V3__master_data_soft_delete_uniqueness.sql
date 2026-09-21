ALTER TABLE suppliers DROP CONSTRAINT uk_suppliers_code;
ALTER TABLE customers DROP CONSTRAINT uk_customers_code;
ALTER TABLE godowns DROP CONSTRAINT uk_godowns_code;

CREATE UNIQUE INDEX uk_suppliers_active_code
    ON suppliers (LOWER(code))
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_customers_active_code
    ON customers (LOWER(code))
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_godowns_active_code
    ON godowns (LOWER(code))
    WHERE deleted_at IS NULL;
