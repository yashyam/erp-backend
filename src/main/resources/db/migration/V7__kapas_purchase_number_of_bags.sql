ALTER TABLE kapas_purchase_entries
    ADD COLUMN number_of_bags NUMERIC(19,3) NOT NULL DEFAULT 0;

ALTER TABLE kapas_purchase_entries
    ADD CONSTRAINT chk_kapas_purchase_entries_number_of_bags_non_negative
    CHECK (number_of_bags >= 0);