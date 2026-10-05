ALTER TABLE my_services ADD COLUMN IF NOT EXISTS contract_file BYTEA;
ALTER TABLE my_services ADD COLUMN IF NOT EXISTS contract_filename VARCHAR(255);
ALTER TABLE my_services ADD COLUMN IF NOT EXISTS contract_content_type VARCHAR(100);
