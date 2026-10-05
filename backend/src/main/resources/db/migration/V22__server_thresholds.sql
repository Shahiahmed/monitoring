-- warn_ram / crit_ram  : ГБ свободной RAM (ниже порога = предупреждение/критично)
-- warn_disk / crit_disk: ГБ свободного диска (ниже порога = предупреждение/критично)
ALTER TABLE dic_server
    ADD COLUMN IF NOT EXISTS warn_ram  integer DEFAULT 20,
    ADD COLUMN IF NOT EXISTS warn_disk integer DEFAULT 50,
    ADD COLUMN IF NOT EXISTS crit_ram  integer DEFAULT 10,
    ADD COLUMN IF NOT EXISTS crit_disk integer DEFAULT 20;
