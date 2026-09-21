ALTER TABLE kapas_purchase_entries
    ADD COLUMN supplier_id UUID,
    ADD COLUMN created_by UUID;

UPDATE kapas_purchase_entries p
SET supplier_id = s.id
FROM suppliers s
WHERE p.supplier_id IS NULL
  AND lower(trim(p.supplier_name)) = lower(trim(s.name))
  AND s.deleted_at IS NULL;

ALTER TABLE kapas_purchase_entries
    ADD CONSTRAINT fk_kapas_purchase_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    ADD CONSTRAINT fk_kapas_purchase_creator FOREIGN KEY (created_by) REFERENCES users(id);

CREATE INDEX idx_kapas_purchase_entries_supplier_id ON kapas_purchase_entries(supplier_id);
CREATE INDEX idx_kapas_purchase_entries_created_by ON kapas_purchase_entries(created_by);