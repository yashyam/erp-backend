CREATE SEQUENCE sales_number_seq AS BIGINT START WITH 1 INCREMENT BY 1;

CREATE TABLE sales (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sales_id VARCHAR(40) NOT NULL UNIQUE,
    bill_id VARCHAR(100) NOT NULL UNIQUE,
    kapas_purchase_entry_id UUID NOT NULL,
    customer_id UUID,
    customer_name VARCHAR(255) NOT NULL,
    supplier_id UUID,
    supplier_name VARCHAR(255) NOT NULL,
    lot_number VARCHAR(100),
    raw_material_in_date DATE NOT NULL,
    raw_material_out_date DATE NOT NULL,
    raw_material_net_weight NUMERIC(19,3) NOT NULL,
    raw_material_cost NUMERIC(19,2) NOT NULL,
    bale_kg NUMERIC(19,3),
    seed_kg NUMERIC(19,3),
    sold_product_weight NUMERIC(19,3) NOT NULL,
    sale_price NUMERIC(19,2),
    sale_rate NUMERIC(19,2),
    sales_value NUMERIC(19,2) NOT NULL,
    buyer_contact VARCHAR(255),
    buyer_address VARCHAR(500),
    notes VARCHAR(2000),
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_sales_purchase FOREIGN KEY (kapas_purchase_entry_id) REFERENCES kapas_purchase_entries(id),
    CONSTRAINT fk_sales_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_sales_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    CONSTRAINT chk_sales_weights CHECK (
        raw_material_net_weight >= 0 AND raw_material_cost >= 0 AND sold_product_weight >= 0 AND
        (bale_kg IS NULL OR bale_kg >= 0) AND (seed_kg IS NULL OR seed_kg >= 0) AND
        (sale_price IS NULL OR sale_price >= 0) AND (sale_rate IS NULL OR sale_rate >= 0) AND sales_value >= 0
    )
);

CREATE INDEX idx_sales_purchase ON sales(kapas_purchase_entry_id);
CREATE INDEX idx_sales_customer ON sales(customer_id);
CREATE INDEX idx_sales_supplier ON sales(supplier_id);
CREATE INDEX idx_sales_lot ON sales(lot_number);
CREATE INDEX idx_sales_active_dates ON sales(deleted_at, raw_material_out_date);
CREATE UNIQUE INDEX uk_sales_active_bill_id ON sales(LOWER(bill_id)) WHERE deleted_at IS NULL;
