-- Тревоги PRTG (видны только администраторам)
CREATE TABLE prtg_alerts (
    id          BIGSERIAL PRIMARY KEY,
    prtg_status VARCHAR(32),
    in_message  VARCHAR(512),
    solution    TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Интервалы простоя тревоги
CREATE TABLE prtg_alert_intervals (
    id             BIGSERIAL PRIMARY KEY,
    prtg_alert_id  BIGINT NOT NULL REFERENCES prtg_alerts(id) ON DELETE CASCADE,
    date_from      TIMESTAMPTZ,
    date_to        TIMESTAMPTZ,
    diff_minutes   INTEGER
);

CREATE INDEX idx_prtg_alert_intervals_alert_id ON prtg_alert_intervals(prtg_alert_id);

-- Файлы тревоги
CREATE TABLE prtg_alert_files (
    id            BIGSERIAL PRIMARY KEY,
    prtg_alert_id BIGINT NOT NULL REFERENCES prtg_alerts(id) ON DELETE CASCADE,
    file_name     VARCHAR(512) NOT NULL,
    content_type  VARCHAR(255),
    file_size     BIGINT,
    data          BYTEA NOT NULL,
    uploaded_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_prtg_alert_files_alert_id ON prtg_alert_files(prtg_alert_id);
