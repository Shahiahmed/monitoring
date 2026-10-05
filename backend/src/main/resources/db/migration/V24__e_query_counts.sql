CREATE TABLE e_query_counts (
    id           BIGSERIAL PRIMARY KEY,
    oracle_id    BIGINT,
    year_month   VARCHAR(7)   NOT NULL,
    code         VARCHAR(300) NOT NULL,
    shep_service_id VARCHAR(300),
    monthly_count   BIGINT NOT NULL,
    synced_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_e_query_counts_unique
    ON e_query_counts (year_month, code, COALESCE(shep_service_id, ''));
