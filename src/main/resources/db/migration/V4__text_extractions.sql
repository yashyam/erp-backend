CREATE SEQUENCE text_extraction_number_seq AS BIGINT START WITH 1 INCREMENT BY 1;
CREATE TABLE text_extractions (
 id UUID PRIMARY KEY DEFAULT gen_random_uuid(), created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
 extraction_number VARCHAR(40) NOT NULL UNIQUE, status VARCHAR(20) NOT NULL DEFAULT 'PENDING', source_text TEXT NOT NULL,
 supplier_name VARCHAR(255), invoice_number VARCHAR(100), invoice_date DATE, vehicle_number VARCHAR(50), quantity NUMERIC(19,6), rate NUMERIC(19,6), tax NUMERIC(19,6), total_amount NUMERIC(19,6), extracted_data JSONB, extraction_method VARCHAR(20), confidence_score NUMERIC(5,4), error_message TEXT,
 CONSTRAINT chk_text_extraction_status CHECK (status IN ('PENDING','PROCESSING','COMPLETED','FAILED','REVIEW_REQUIRED')), CONSTRAINT chk_text_extraction_method CHECK (extraction_method IS NULL OR extraction_method IN ('RULE_BASED','AI','MANUAL')), CONSTRAINT chk_text_extraction_confidence CHECK (confidence_score IS NULL OR confidence_score BETWEEN 0 AND 1)
);
CREATE INDEX idx_text_extractions_status ON text_extractions(status); CREATE INDEX idx_text_extractions_method ON text_extractions(extraction_method); CREATE INDEX idx_text_extractions_created_at ON text_extractions(created_at); CREATE INDEX idx_text_extractions_invoice_date ON text_extractions(invoice_date);
