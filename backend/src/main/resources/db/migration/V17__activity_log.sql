CREATE TABLE activity_log (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT,
    user_email  VARCHAR(255),
    action      VARCHAR(50)  NOT NULL,
    entity_type VARCHAR(50),
    entity_id   BIGINT,
    description TEXT         NOT NULL,
    ip          VARCHAR(45),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_activity_log_created_at ON activity_log (created_at DESC);
CREATE INDEX idx_activity_log_user_id    ON activity_log (user_id);
