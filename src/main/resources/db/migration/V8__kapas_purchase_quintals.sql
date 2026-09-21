ALTER TABLE kapas_purchase_entries
    ADD COLUMN quintals NUMERIC(19,3);

UPDATE kapas_purchase_entries
SET quintals = ROUND(net_kapas_weight / 100, 3)
WHERE net_kapas_weight IS NOT NULL;

UPDATE kapas_purchase_entries
SET amount = ROUND((net_kapas_weight / 100) * rate, 2)
WHERE net_kapas_weight IS NOT NULL AND rate IS NOT NULL;