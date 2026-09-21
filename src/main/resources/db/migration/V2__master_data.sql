CREATE TABLE suppliers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(200) NOT NULL,
    contact_person VARCHAR(150),
    phone VARCHAR(20),
    email VARCHAR(255),
    address VARCHAR(500),
    gstin VARCHAR(20),
    pan VARCHAR(20),
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT uk_suppliers_code UNIQUE (code)
);

CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(200) NOT NULL,
    contact_person VARCHAR(150),
    phone VARCHAR(20),
    email VARCHAR(255),
    address VARCHAR(500),
    gstin VARCHAR(20),
    pan VARCHAR(20),
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT uk_customers_code UNIQUE (code)
);

CREATE TABLE godowns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(200) NOT NULL,
    address VARCHAR(500),
    contact_person VARCHAR(150),
    phone VARCHAR(20),
    capacity NUMERIC(15,3),
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT uk_godowns_code UNIQUE (code)
);

CREATE INDEX idx_suppliers_active ON suppliers (deleted_at, name);
CREATE INDEX idx_customers_active ON customers (deleted_at, name);
CREATE INDEX idx_godowns_active ON godowns (deleted_at, name);
