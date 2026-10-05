CREATE TABLE my_service_formats (
    id         BIGSERIAL PRIMARY KEY,
    service_id BIGINT NOT NULL REFERENCES my_services(id) ON DELETE CASCADE,
    name       VARCHAR(500) NOT NULL,
    sort_order INT DEFAULT 0
);

ALTER TABLE my_service_fields
    ADD COLUMN format_id BIGINT REFERENCES my_service_formats(id) ON DELETE SET NULL;
