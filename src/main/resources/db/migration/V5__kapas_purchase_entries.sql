CREATE SEQUENCE kapas_purchase_entry_number_seq AS BIGINT START WITH 1 INCREMENT BY 1;

CREATE TABLE kapas_purchase_entries (
 id UUID PRIMARY KEY DEFAULT gen_random_uuid(), created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
 entry_number VARCHAR(40) NOT NULL UNIQUE, bill_date DATE NOT NULL, supplier_name VARCHAR(255) NOT NULL, bill_number VARCHAR(100) NOT NULL, vehicle_number VARCHAR(50),
 gross_weight NUMERIC(19,3), tare_weight NUMERIC(19,3), net_kapas_weight NUMERIC(19,3), rate NUMERIC(19,2), amount NUMERIC(19,2),
 lot_number VARCHAR(100), godown_id UUID, text_extraction_id UUID, deleted_at TIMESTAMP WITHOUT TIME ZONE,
 CONSTRAINT fk_kapas_purchase_entries_godown FOREIGN KEY (godown_id) REFERENCES godowns(id),
 CONSTRAINT fk_kapas_purchase_entries_text_extraction FOREIGN KEY (text_extraction_id) REFERENCES text_extractions(id),
 CONSTRAINT chk_kapas_purchase_entries_non_negative CHECK (
   (gross_weight IS NULL OR gross_weight >= 0) AND (tare_weight IS NULL OR tare_weight >= 0) AND
   (net_kapas_weight IS NULL OR net_kapas_weight >= 0) AND (rate IS NULL OR rate >= 0) AND (amount IS NULL OR amount >= 0))
);

CREATE INDEX idx_kapas_purchase_entries_entry_number ON kapas_purchase_entries(entry_number);
CREATE INDEX idx_kapas_purchase_entries_bill_date ON kapas_purchase_entries(bill_date);
CREATE INDEX idx_kapas_purchase_entries_supplier_name ON kapas_purchase_entries(supplier_name);
CREATE INDEX idx_kapas_purchase_entries_lot_number ON kapas_purchase_entries(lot_number);
CREATE INDEX idx_kapas_purchase_entries_godown_id ON kapas_purchase_entries(godown_id);

CREATE UNIQUE INDEX uk_kapas_purchase_entries_active_bill
    ON kapas_purchase_entries (LOWER(bill_number), LOWER(supplier_name))
    WHERE deleted_at IS NULL;
