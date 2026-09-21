-- Track the attachment a text extraction was produced from.
ALTER TABLE text_extractions
    ADD COLUMN source_file_name VARCHAR(255),
    ADD COLUMN source_file_type VARCHAR(100),
    ADD COLUMN source_file_size BIGINT;
