CREATE SEQUENCE bale_number_seq AS BIGINT START WITH 1 INCREMENT BY 1;

ALTER TABLE kapas_purchase_entries
    ADD COLUMN IF NOT EXISTS lot_status VARCHAR(30) NOT NULL DEFAULT 'YET_TO_START',
    ADD COLUMN IF NOT EXISTS status_updated_at TIMESTAMP WITHOUT TIME ZONE;

UPDATE kapas_purchase_entries
SET lot_status = 'YET_TO_START',
    status_updated_at = COALESCE(status_updated_at, CURRENT_TIMESTAMP)
WHERE lot_status IS NULL;

CREATE TABLE bale_production (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    bale_number VARCHAR(40) NOT NULL UNIQUE,
    lot_id UUID NOT NULL,
    lot_number VARCHAR(100) NOT NULL,
    production_date DATE NOT NULL,
    serial_no BIGINT NOT NULL,
    bale_weight NUMERIC(19,3) NOT NULL,
    candy NUMERIC(19,3),
    quintals NUMERIC(19,3),
    created_by UUID,
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_bale_production_lot FOREIGN KEY (lot_id) REFERENCES kapas_purchase_entries(id),
    CONSTRAINT fk_bale_production_creator FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT chk_bale_production CHECK (
        serial_no > 0 AND bale_weight > 0 AND
        (candy IS NULL OR candy >= 0) AND
        (quintals IS NULL OR quintals >= 0)
    )
);

CREATE INDEX idx_bale_production_lot ON bale_production(lot_id);
CREATE INDEX idx_bale_production_lot_number ON bale_production(lot_number);
CREATE INDEX idx_bale_production_date ON bale_production(production_date);
CREATE UNIQUE INDEX uk_bale_production_lot_serial_active ON bale_production(lot_number, serial_no) WHERE deleted_at IS NULL;

INSERT INTO permissions (name, resource, action, description)
VALUES
    ('BALE_READ', 'BALE', 'READ', 'View bale production records and summaries'),
    ('BALE_CREATE', 'BALE', 'CREATE', 'Create bale production records')
ON CONFLICT (name) DO UPDATE
SET description = EXCLUDED.description;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN ('BALE_READ', 'BALE_CREATE')
WHERE r.name IN ('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_SUPERVISOR', 'ROLE_WORKER')
ON CONFLICT DO NOTHING;
