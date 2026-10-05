CREATE TABLE server_metrics_history (
    id            BIGSERIAL PRIMARY KEY,
    server_id     BIGINT          NOT NULL REFERENCES dic_server(id) ON DELETE CASCADE,
    collected_at  TIMESTAMP       NOT NULL,
    cpu_percent   DOUBLE PRECISION,
    memory_percent DOUBLE PRECISION,
    disk_percent  DOUBLE PRECISION,
    memory_used_mb BIGINT,
    memory_total_mb BIGINT,
    disk_used_gb  BIGINT,
    disk_total_gb BIGINT
);

CREATE INDEX idx_smh_server_collected
    ON server_metrics_history(server_id, collected_at DESC);
