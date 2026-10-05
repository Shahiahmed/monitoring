CREATE TABLE my_service_clients (
    id                 BIGSERIAL PRIMARY KEY,
    service_id         BIGINT NOT NULL REFERENCES my_services(id) ON DELETE CASCADE,
    organization_name  VARCHAR(1000) NOT NULL,
    information_system VARCHAR(500),
    is_paid            BOOLEAN NOT NULL DEFAULT FALSE,
    notes              TEXT
);
