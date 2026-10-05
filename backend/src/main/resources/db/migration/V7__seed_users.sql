-- Начальные учётные записи для продакшена

-- Пользователь: sh.askar@enbek.kz (SUPER_ADMIN)
INSERT INTO users (is_active, avatar, email, password_hash, first_name, last_name, second_name, mail_token, password_hint, registration_date, last_login_date)
SELECT true, null, 'sh.askar@enbek.kz',
       '$2a$10$Q9/g4BQ6Wh4u7/5dhbdBAei7pgGgvsBTu.f3B6kt65.JlCeFX7EE2',
       'Аскар', 'Шахиахмед', '', null, 'flower + age',
       '2026-03-16 07:38:16.136240+00'::timestamptz, null
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'sh.askar@enbek.kz');

-- Назначить роль SUPER_ADMIN пользователю sh.askar@enbek.kz
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'sh.askar@enbek.kz'
  AND r.code  = 'SUPER_ADMIN'
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id
  );
