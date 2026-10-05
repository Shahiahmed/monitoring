ALTER TABLE my_service_clients
    ADD COLUMN smart_bridge_ticket VARCHAR(500),
    ADD COLUMN connection_basis     TEXT,
    ADD COLUMN connection_date      DATE,
    ADD COLUMN contract_file_name   VARCHAR(500),
    ADD COLUMN contract_file_data   BYTEA;
