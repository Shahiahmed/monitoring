-- Users (email-only)
CREATE TABLE IF NOT EXISTS users
(
    id                bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    is_active         boolean NOT NULL DEFAULT true,

    avatar            bytea,

    email             varchar(255) NOT NULL,   -- login по email
    password_hash     varchar(255) NOT NULL,   -- хранить bcrypt/argon2 hash

    first_name        varchar(255),
    last_name         varchar(255),
    second_name       varchar(255),

    mail_token        varchar(255),
    password_hint     varchar(255),

    registration_date timestamptz NOT NULL DEFAULT now(),
    last_login_date   timestamptz
    );

-- Уникальность email
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email ON users (email);

-- mail_token обычно лучше уникальным (если используешь подтверждение/сброс)
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_mail_token
    ON users (mail_token)
    WHERE mail_token IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_users_is_active ON users (is_active);

-- Индексы по ФИО оставляй только если есть поиск/фильтры
CREATE INDEX IF NOT EXISTS idx_users_first_name  ON users (first_name);
CREATE INDEX IF NOT EXISTS idx_users_last_name   ON users (last_name);
CREATE INDEX IF NOT EXISTS idx_users_second_name ON users (second_name);
