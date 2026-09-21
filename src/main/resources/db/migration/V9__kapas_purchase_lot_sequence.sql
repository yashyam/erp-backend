CREATE SEQUENCE kapas_purchase_lot_number_seq AS BIGINT START WITH 1 INCREMENT BY 1;

DO $$
DECLARE
    last_lot BIGINT;
BEGIN
    SELECT COALESCE(MAX((regexp_match(lot_number, '([0-9]+)$'))[1]::BIGINT), 0)
    INTO last_lot
    FROM kapas_purchase_entries
    WHERE lot_number IS NOT NULL;

    IF last_lot > 0 THEN
        PERFORM setval('kapas_purchase_lot_number_seq', last_lot, true);
    END IF;
END $$;