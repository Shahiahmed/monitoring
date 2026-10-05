CREATE TABLE IF NOT EXISTS certificate_assets
(
    id
    BIGSERIAL
    PRIMARY
    KEY,

    user_id
    BIGINT
    NOT
    NULL,

    -- ECP | SSL | BOTH
    type
    VARCHAR
(
    16
) NOT NULL DEFAULT 'ECP',

    -- active | expired | revoked | disabled
    status VARCHAR
(
    16
) NOT NULL DEFAULT 'active',

    -- Где лежат файлы (disk path / S3 key / object storage key)
    key_storage TEXT,
    cert_storage TEXT,

    -- Оригинальные имена загруженных файлов
    key_original_name TEXT,
    cert_original_name TEXT,

    -- Отпечаток сертификата (hex sha256)
    cert_fingerprint_sha256 VARCHAR
(
    64
),

    -- Метаданные сертификата (для отображения/поиска)
    cert_subject TEXT,
    cert_issuer TEXT,
    cert_serial_number TEXT,

    valid_from TIMESTAMPTZ,
    valid_to TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now
(
),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now
(
)
    );

-- Быстрый поиск по пользователю/статусу
CREATE INDEX IF NOT EXISTS certificate_assets_user_status_idx
    ON certificate_assets (user_id, status);

-- Частичный уникальный индекс (позволяет много NULL)
CREATE UNIQUE INDEX IF NOT EXISTS certificate_assets_fingerprint_uq
    ON certificate_assets (cert_fingerprint_sha256)
    WHERE cert_fingerprint_sha256 IS NOT NULL;

-- Если у тебя есть таблица users(id) и нужна связность — раскомментируй:
-- DO $$
-- BEGIN
--   IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'users') THEN
--     ALTER TABLE certificate_assets
--       ADD CONSTRAINT certificate_assets_user_fk
--       FOREIGN KEY (user_id) REFERENCES users(id)
--       ON DELETE CASCADE;
--   END IF;
-- END $$;

-- Авто-обновление updated_at
CREATE
OR REPLACE FUNCTION set_updated_at()
RETURNS trigger AS $$
BEGIN
  NEW.updated_at
= now();
RETURN NEW;
END;
$$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS certificate_assets_set_updated_at ON certificate_assets;

CREATE TRIGGER certificate_assets_set_updated_at
    BEFORE UPDATE
    ON certificate_assets
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();