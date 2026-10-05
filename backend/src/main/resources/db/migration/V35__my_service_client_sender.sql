ALTER TABLE my_service_clients
    ADD COLUMN IF NOT EXISTS shep_sender_id VARCHAR(300);
