CREATE TABLE mongo_inout_stat (
    id         BIGSERIAL PRIMARY KEY,
    subsystem  VARCHAR(200) NOT NULL,
    sender_id  VARCHAR(200),
    stat_year  INTEGER      NOT NULL,
    stat_month INTEGER      NOT NULL,
    cnt        BIGINT       NOT NULL,
    insert_date TIMESTAMP,
    synced_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_mongo_inout_stat_unique
    ON mongo_inout_stat (subsystem, COALESCE(sender_id, ''), stat_year, stat_month);
