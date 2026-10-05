-- V3__seed_roles.sql
-- Отдельная миграция для ролей:
--  1) создание таблицы roles
--  2) создание таблицы user_roles (связка многие‑ко‑многим)
--  3) наполнение базовыми ролями SUPER_ADMIN / ADMIN / USER

-- 1) Roles
CREATE TABLE IF NOT EXISTS roles
(
    id        bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code      varchar(64) NOT NULL,          -- например: SUPER_ADMIN / ADMIN / USER
    name_en   varchar(255),
    name_kz   varchar(255),
    name_ru   varchar(255)
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_roles_code ON roles (code);


-- 2) User <-> Roles (many-to-many)
CREATE TABLE IF NOT EXISTS user_roles
(
    user_id bigint NOT NULL,
    role_id bigint NOT NULL,
    PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_roles_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE,

    CONSTRAINT fk_user_roles_role
    FOREIGN KEY (role_id) REFERENCES roles(id)
    ON DELETE CASCADE
    );

-- Для запросов "все пользователи с ролью X" лучше композитный индекс:
CREATE INDEX IF NOT EXISTS idx_user_roles_role_user ON user_roles (role_id, user_id);


-- 3) Наполнение базовыми ролями
INSERT INTO roles (code, name_en, name_kz, name_ru)
VALUES
  ('SUPER_ADMIN', 'Super administrator', 'Жоғары әкімші', 'Супер администратор'),
  ('ADMIN',       'Administrator',       'Әкімші',        'Администратор'),
  ('USER',        'User',                'Пайдаланушы',   'Пользователь')
ON CONFLICT (code) DO NOTHING;

