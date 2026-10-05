# SARAP — Инструкция для Claude

> Этот файл создан для быстрого погружения в проект в начале каждой новой сессии.
> Читай его полностью перед тем, как начать работу.

---

## Что это за проект

**SARAP** — корпоративная система мониторинга инцидентов и серверов.

- Frontend: `http://localhost:3000` (Next.js dev)
- Backend API: `http://localhost:8081/api`
- БД: PostgreSQL `localhost:5432/monitoring`

---

## Структура проекта

```
monitoring/
├── frontend/       ← Next.js 16 + React 19 + TypeScript + Tailwind v4
├── backend/        ← Spring Boot 4.0.1 + Java 21 + PostgreSQL
└── CLAUDE.md       ← этот файл
```

---

## Как запустить

### Backend
```bash
cd backend
mvnw.cmd spring-boot:run      # Windows
./mvnw spring-boot:run        # Linux/Mac
# Слушает на :8081
```

### Frontend
```bash
cd frontend
npm install
npm run dev
# Слушает на :3000, API проксируется на :8081
```

### База данных
- PostgreSQL, локально на 5432
- БД: `monitoring`, user: `postgres`, pass: `Blacklotus01`
- Миграции автоматически через Flyway при старте

### Production (всё в одном JAR)
```bash
cd backend
mvnw.cmd clean package        # Windows
java -jar target/monitoring-0.0.1-SNAPSHOT.jar
# frontend + API на :8081
```

---

## Frontend — стек и соглашения

### Технологии
- **Next.js 16.1.4** + React 19.2.3, TypeScript 5, App Router
- **Tailwind CSS v4** — использовать канонические классы (`w-18`, `h-4.5`, `max-w-105`, `shrink-0`), НЕ произвольные значения в `[]`
- **ECharts 6** для графиков
- Shadcn UI / Radix UI / lucide-react **удалены** — не использовать

### Ключевые файлы
| Файл | Назначение |
|------|-----------|
| `frontend/app/globals.css` | Глобальные стили, CSS-переменные, `.glass`, `.glass-card`, `.shadow-card` |
| `frontend/app/lib/api.ts` | `apiFetch(path, init?)` и `apiUrl(path)` — все API-запросы через них |
| `frontend/app/components/Sidebar.tsx` | Навигация, массив `navigation[]` с маршрутами (нет `components/ui/` — папка удалена) |
| `frontend/app/components/Header.tsx` | Шапка |
| `frontend/app/components/ConditionalLayout.tsx` | Обёртка: скрывает sidebar/header на `/login` |
| `frontend/app/components/TourProvider.tsx` | Онбординг-тур (driver.js): автозапуск, `useTour().startTour()` |
| `frontend/app/components/tour/tourSteps.ts` | Шаги тура по маршрутам + `TOUR_VERSION` |
| `frontend/app/locales/ru.json` | Переводы RU |
| `frontend/app/locales/kz.json` | Переводы KZ |

### API утилиты
```typescript
import { apiFetch, apiUrl } from "../../lib/api";

// Обычный запрос (автоматически добавляет Bearer token)
const res = await apiFetch("incidents");
const data = await res.json();

// Запрос с телом
const res = await apiFetch("incidents", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify(body),
});

// Ссылка для скачивания (вставляется в href)
const url = apiUrl("incident-files/download/123");
```

- `NEXT_PUBLIC_API_BASE_URL` = `http://localhost:8081/api` (из `.env.local`)
- При 401 — автоматически чистит localStorage и редиректит на `/login`

### Аутентификация на фронте
```typescript
// Хранится в localStorage
localStorage.getItem("authToken")  // JWT токен
localStorage.getItem("authUser")   // JSON: { id, email, firstName, roles: [{code}] }

// Проверка роли (паттерн используется везде)
const user = JSON.parse(localStorage.getItem("authUser") ?? "{}");
const isAdmin = user?.roles?.some(r => ["ADMIN","SUPER_ADMIN"].includes(r.code ?? r));
```

### Дизайн-система (ВАЖНО)

#### CSS классы из globals.css
```css
.glass          /* glassmorphism — используется для сайдбара */
.glass-card     /* карточки с размытием — используется на страницах */
.shadow-card    /* мягкая тень */
.shadow-soft    /* очень мягкая тень */
.animate-app-reveal  /* fade+blur при входе */
.animate-warning-card /* пульсирующая рамка для предупреждений */
```

#### Цветовые переменные
- Светлая тема: oklch цвета, фон `#f8fbff → #eef4fb`
- Тёмная тема: `html.dark` класс, фон `#1e293b → #0f172a`

#### Паттерн кнопок-действий
```tsx
// Синяя градиентная кнопка
className="... bg-linear-to-r from-blue-600 to-blue-700"
// или через <style jsx global> с классом типа .ev-add-btn / .jrn-add-btn
```

#### Паттерн страниц (page-specific styles)
Каждая страница имеет свой `<style jsx global>` блок в конце с CSS-классами, prefixed именем страницы:
- `add/page.tsx` → `.add-card`, `.add-save-btn`, `.add-icon-box`
- `events/page.tsx` → `.jrn-panel`, `.jrn-chip`, `.jrn-chip-on`, `.jrn-modal`, `.jrn-row`
- `login/page.tsx` → `.login-form-card`, `.login-enter`, `.login-shake`

---

## Маршруты фронтенда

| Путь | Страница | Роль |
|------|----------|------|
| `/login` | Вход | Public |
| `/` | Главная / дашборд | USER+ |
| `/profile` | Профиль пользователя | USER+ |
| `/servers` | Мониторинг серверов | USER+ |
| `/services` | Список сервисов | USER+ |
| `/services/add` | Добавить сервис | ADMIN+ |
| `/services/registry` | Реестр сервисов | USER+ |
| `/services/my-services` | Мои сервисы | USER+ |
| `/services/my-services/detail` | Детали сервиса (статистика, клиенты, форматы) | USER+ |
| `/services/my-services/add` | Добавить / редактировать сервис | ADMIN+ |
| `/incidents` | Инциденты (обзор) | USER+ |
| `/incidents/events` | Журнал событий (3 режима: работы/инцидент/prtg) | USER+ |
| `/incidents/add` | Редирект → `/incidents/events` | — |
| `/incidents/add/works` | Добавить: Плановые/Внеплановые работы | ADMIN+ |
| `/incidents/add/incident` | Добавить: Инцидент | ADMIN+ |
| `/incidents/add/prtg` | Добавить: Тревоги PRTG | ADMIN+ |
| `/incidents/statistics` | Статистика инцидентов | USER+ |
| `/incidents/availability` | Доступность ИС | USER+ |
| `/statistics/integrations` | Статистика интеграций (E_QUERY_COUNTS + MONGO) | USER+ |
| `/users` | Список пользователей | USER+ |
| `/users/register` | Регистрация пользователя | ADMIN+ |
| `/users/roles` | Роли | USER+ |
| `/settings/ssl` | ЭЦП/SSL сертификаты | USER+ |
| `/settings/activity` | Журнал действий | USER+ |
| `/references/locations` | Справочник: Местоположения | USER+ |
| `/references/environments` | Справочник: Окружения | USER+ |
| `/references/government-bodies` | Справочник: Гос. органы | USER+ |
| `/references/information-systems` | Справочник: ИС | USER+ |
| `/references/interaction-types` | Справочник: Типы взаимодействия | USER+ |
| `/references/application-types` | Справочник: Типы приложения | USER+ |

---

## Архитектура инцидентов (ключевая часть проекта)

### 3 отдельные таблицы в БД (ВАЖНО!)

Все события хранятся в **трёх отдельных таблицах** — не в одной:

| Таблица | Назначение | API endpoint |
|---------|-----------|-------------|
| `works` | Плановые / Внеплановые работы | `/api/works` |
| `incidents` | Инциденты | `/api/incidents` |
| `prtg_alerts` | Тревоги PRTG (внутренние, только ADMIN) | `/api/prtg-alerts` |

У каждой таблицы свои junction/interval/files таблицы:
- `work_intervals`, `work_is`, `work_files`
- `incident_intervals`, `incident_is`, `incident_files`
- `prtg_alert_intervals`, `prtg_alert_files`

`dic_job` содержит только 2 записи: id=1 Плановые работы, id=2 Внеплановые работы.

### Связь works/incidents с тревогой PRTG

- Поле `source_prtg_id BIGINT REFERENCES prtg_alerts(id) ON DELETE SET NULL` в `works` и `incidents`
- Кнопка "Загрузить из тревоги PRTG" расположена **вверху** формы для работ и инцидентов:
  - Подгружает список тревог `GET /api/prtg-alerts`
  - При выборе → `GET /api/works/load-from-prtg/{id}` или `GET /api/incidents/load-from-prtg/{id}`
  - Возвращает prefilled request; поля подтягиваются в форму
  - При сохранении создаётся **НОВАЯ** запись с `sourcePrtgId`; тревога не изменяется
- В модальном окне: жёлтый баннер "Создано на основе тревоги PRTG №N" если `sourcePrtgId != null`

### Форма "Работы" (mode="works") — поля

Форма содержит только:
1. **Загрузить из тревоги PRTG** (dropdown, вверху)
2. **Тип работы** (select → `dic_job`)
3. **Номер письма** (`inMessage`)
4. **Примечание** (`solution`, col-span-2)
5. **ИС** (checkboxes — справочник информационных систем)
6. **Дата и время** (интервалы, правая колонка)

**Не отображаются** (но остаются в бэкенде со значениями по умолчанию):
- Исх. письмо (`outMessage`) — не нужно для работ
- Без времени простоя (`emptyTime`) — всегда `false`
- Учитывать в % доступности (`includeAvailability`) — всегда `true`

### Форма "Инцидент" (mode="incident") — поля

1. **Загрузить из тревоги PRTG** (dropdown, вверху)
2. Тип инцидента, Акт сбоя, Вх. письмо, Исх. письмо, Причина, Примечание
3. Чекбоксы: Зафиксировано в АО НИТ, Без времени простоя, Учитывать в % доступности
4. ИС (checkboxes — справочник информационных систем)
5. Дата и время (интервалы, правая колонка)

### Страницы добавления (3 отдельные страницы)
```
frontend/app/incidents/add/
  page.tsx               ← редирект на /incidents/events
  add-incident-form.tsx  ← общий компонент (mode: "works"|"incident"|"prtg")
  works/page.tsx         ← mode="works"
  incident/page.tsx      ← mode="incident"
  prtg/page.tsx          ← mode="prtg"
```

`AddIncidentForm` отправляет на правильный endpoint по `mode`:
- `works` → `POST/PUT /api/works`, файлы → `/api/work-files`
- `incident` → `POST/PUT /api/incidents`, файлы → `/api/incident-files`
- `prtg` → `POST/PUT /api/prtg-alerts`, файлы → `/api/prtg-alert-files`

Редактирование: `useSearchParams().get("id")` — загружает существующую запись.

### Журнал событий (`/incidents/events`)
- **3 режима** (вкладки): Работы / Инциденты / Тревоги PRTG
- Каждый режим загружает данные из своего endpoint:
  - Работы → `GET /api/works`
  - Инциденты → `GET /api/incidents`
  - Тревоги PRTG → `GET /api/prtg-alerts`
- Тревоги PRTG — вкладка видна только ADMIN+
- Год-чипы (year chips) загружаются из **режим-специфичного** endpoint:
  ```typescript
  const endpoint = activeMode === "incident" ? "incidents/years"
                 : activeMode === "works"    ? "works/years"
                 : "prtg-alerts/years";
  ```
- `getEditHref(mode, id)` → `/incidents/add/{mode}?id={id}`
- Местоположение (location) **убрано** из UI

---

## Статистика интеграций

### Два источника данных (ВАЖНО — только чтение!)

| Таблица | Источник | Назначение |
|---------|----------|-----------|
| `e_query_counts` | Oracle `ESERV.E_QUERY_COUNTS` | Статистика запросов по ШЭП (ключ сервиса) |
| `mongo_inout_stat` | Oracle `ESERV.MONGO_INOUT_STAT` | Статистика по подсистемам и sender_id |

**Oracle-таблицы (`ESERV.*`) — только SELECT. Никогда не изменять!**

### Синхронизация
- Кнопка "Синхронизировать" на странице `/statistics/integrations`
- `POST /api/e-query-counts/sync` — UPSERT из Oracle в локальную таблицу
- `POST /api/mongo-stat/sync` — UPSERT из Oracle в локальную таблицу
- При синхронизации удалённые в Oracle записи **остаются локально** (не удаляются)

### sender_client_map
Таблица `sender_client_map` (sender_id → client_name) — маппинг технических ID отправителей на названия организаций-клиентов. Используется в `GET /api/mongo-stat/service/{subsystem}` для отображения клиентских названий.

### Страница детали сервиса (`/services/my-services/detail`)
- Два графика: статистика запросов по месяцам (max из двух источников) + статистика по клиентам
- Tooltip показывает значения из обоих источников (E_QUERY_COUNTS и MONGO)
- Клиенты группируются по `sender_id`, цвет — по названию клиента (`clientName`)

---

## Backend — стек и API

### Технологии
- Spring Boot 4.0.1, Java 21
- Spring Security + JWT (JJWT 0.12.6, HS256)
- Spring Data JPA + Hibernate
- PostgreSQL + Flyway миграции
- SSHJ (SSH подключение для метрик серверов)
- Lombok, Jackson

### Конфигурация (`application.properties`)
```properties
server.port=8081
spring.datasource.url=jdbc:postgresql://localhost:5432/monitoring
spring.datasource.username=postgres
spring.datasource.password=Blacklotus01
monitoring.jwt.secret=dev-change-me-use-env-in-production-32chars!!
monitoring.jwt.expiration-ms=86400000  # 24 часа
monitoring.ssh.user=monitoringapp
monitoring.ssh.password=Qwerty123
monitoring.ssh.port=22
# Flyway — обязательно для предотвращения ошибки "схема не выбрана"
spring.flyway.schemas=public
spring.flyway.default-schema=public
spring.flyway.baseline-on-migrate=true
```

### Структура пакетов
```
com.example.monitoring/
├── config/          SecurityConfig, WebMvcConfig
├── controller/      REST контроллеры
├── entity/          JPA сущности
├── dto/             Request/Response DTO
├── repository/      Spring Data JPA репозитории
├── service/         IncidentService, WorkService, PrtgAlertService, SshMetricsService, ...
└── security/        JwtService, JwtAuthenticationFilter, MonitoringUserPrincipal
```

### Все API эндпоинты

#### Auth (публичный)
```
POST   /api/auth/login              → {token, user}
```

#### Users
```
GET    /api/users                   → список пользователей
GET    /api/users/me                → текущий пользователь
POST   /api/users/register          → регистрация (ADMIN+)
POST   /api/users/me/change-password
PUT    /api/users/{id}              → обновить профиль
PUT    /api/users/{id}/password     → сменить пароль (SUPER_ADMIN)
POST   /api/users/{id}/delete       → удалить (SUPER_ADMIN)
PATCH  /api/users/{id}/active       → активировать/деактивировать
POST   /api/users/{id}/avatar       → загрузить аватар (multipart)
GET    /api/users/{id}/avatar       → получить аватар
```

#### Roles
```
GET    /api/roles
```

#### Works
```
GET    /api/works                   → список (фильтры: dicJobId, isIds, emptyTime, dateFrom, dateTo)
GET    /api/works/years             → список годов (из work_intervals.date_from)
GET    /api/works/{id}
POST   /api/works                   → создать
PUT    /api/works/{id}              → обновить
DELETE /api/works/{id}              → удалить
DELETE /api/works/intervals/{intervalId}
GET    /api/works/load-from-prtg/{prtgAlertId}  → prefilled WorkRequest из тревоги
```

#### Work Files
```
GET    /api/work-files/{workId}              → список файлов
POST   /api/work-files/{workId}              → загрузить файл (multipart)
GET    /api/work-files/download/{fileId}     → скачать
DELETE /api/work-files/{fileId}              → удалить
```

#### Incidents
```
GET    /api/incidents               → список (фильтры: failureTypeId, isIds, fixed, emptyTime, dateFrom, dateTo)
GET    /api/incidents/stats         → статистика (dateFrom, dateTo) → IncidentStatsResponse
GET    /api/incidents/years         → список годов (из incident_intervals.date_from)
GET    /api/incidents/{id}
POST   /api/incidents               → создать
PUT    /api/incidents/{id}          → обновить
DELETE /api/incidents/{id}          → удалить
DELETE /api/incidents/intervals/{intervalId}
GET    /api/incidents/load-from-prtg/{prtgAlertId}  → prefilled IncidentRequest из тревоги
```

#### Incident Files
```
GET    /api/incident-files/{incidentId}          → список файлов
POST   /api/incident-files/{incidentId}          → загрузить файл (multipart)
GET    /api/incident-files/download/{fileId}     → скачать
DELETE /api/incident-files/{fileId}              → удалить
```

#### PRTG Alerts
```
GET    /api/prtg-alerts             → список (фильтры: dateFrom, dateTo)
GET    /api/prtg-alerts/years       → список годов (из prtg_alert_intervals.date_from)
GET    /api/prtg-alerts/{id}
POST   /api/prtg-alerts             → создать
PUT    /api/prtg-alerts/{id}        → обновить
DELETE /api/prtg-alerts/{id}        → удалить
DELETE /api/prtg-alerts/intervals/{intervalId}
```

#### PRTG Alert Files
```
GET    /api/prtg-alert-files/{prtgAlertId}           → список файлов
POST   /api/prtg-alert-files/{prtgAlertId}           → загрузить файл (multipart)
GET    /api/prtg-alert-files/download/{fileId}       → скачать
DELETE /api/prtg-alert-files/{fileId}                → удалить
```

#### Статистика интеграций
```
GET    /api/e-query-counts          → список (фильтры: shepServiceId, year)
POST   /api/e-query-counts/sync     → UPSERT из Oracle
GET    /api/e-query-counts/service/{key}  → по ключу сервиса, по месяцам

GET    /api/mongo-stat              → список
POST   /api/mongo-stat/sync         → UPSERT из Oracle
GET    /api/mongo-stat/service/{subsystem}  → по подсистеме, с clientName из sender_client_map
```

#### My Services
```
GET    /api/my-services             → список
GET    /api/my-services/{id}
POST   /api/my-services             → создать (ADMIN+)
PUT    /api/my-services/{id}        → обновить (ADMIN+)
DELETE /api/my-services/{id}        → удалить (ADMIN+)
```

#### Servers
```
GET    /api/servers
GET    /api/servers/{id}
GET    /api/servers/metrics          → SSH метрики (CPU, RAM, диск)
POST   /api/servers
PUT    /api/servers/{id}
DELETE /api/servers/{id}
```

#### Certificates
```
POST   /api/settings/certificates/ssl    → загрузить SSL (multipart: file + password)
POST   /api/settings/certificates/ecp    → загрузить ЭЦП
GET    /api/settings/certificates        → список (userId, type)
DELETE /api/settings/certificates/{id}
```

#### Справочники (все: GET list, GET /{id}, POST, PUT /{id}, DELETE /{id})
```
/api/environments          → Окружения (DicEnv)
/api/government-bodies     → Гос. органы (DicGo)
/api/information-systems   → Информационные системы (DicIs, FK → DicGo)
/api/locations             → Местоположения (DicLocation)
/api/job-types             → Типы работ (DicJob) — только GET
/api/dic-failure-types     → Типы инцидентов — только GET
```

### Роли и авторизация
```
USER        → базовый доступ (просмотр)
ADMIN       → управление инцидентами, регистрация пользователей
SUPER_ADMIN → полный доступ, удаление пользователей, смена паролей
```

CORS: разрешены все origins (`*`), все методы.

### Фильтрация по дате (ВАЖНО!)

Во всех трёх сервисах (IncidentService, WorkService, PrtgAlertService) фильтрация по дате происходит **по дате интервала** (`intervals.dateFrom`), а **не** по `created_at`. Это критично, потому что запись может быть создана сегодня, а интервал относиться к прошлому году.

Паттерн JPA Specification с JOIN:
```java
if (dateFrom != null || dateTo != null) {
    var iv = root.join("intervals", JoinType.LEFT);
    if (dateFrom != null) predicates.add(cb.greaterThanOrEqualTo(iv.get("dateFrom"), dateFrom));
    if (dateTo != null)   predicates.add(cb.lessThanOrEqualTo(iv.get("dateFrom"), dateTo));
    query.distinct(true);  // обязательно при JOIN с коллекцией
}
```

### IncidentStatsResponse

Используется страницами `/incidents/statistics` и `/incidents/availability`:
```java
IncidentStatsResponse {
    List<TypeCount> byType;        // { name, count, totalMinutes }
    List<MonthCount> byMonth;      // { month (YYYY-MM), count, totalMinutes }
    List<IsAvailability> byIsAvailability; // { id, nameRu, totalDowntimeMinutes, availabilityPercent }
}
```

### ИИ-ассистент и база знаний

Ассистент (`POST /api/ai/chat/stream`) — локальная Ollama, модель `qwen3:8b`.
`OllamaService.buildDataContext()` собирает системный промпт из двух блоков:

| Блок | Источник | Когда добавляется |
|------|----------|-------------------|
| `=== СПРАВКА О СИСТЕМЕ ===` | `resources/ai/knowledge.md` через `KnowledgeBaseService` | почти всегда |
| `=== АКТУАЛЬНЫЕ ДАННЫЕ ИЗ СИСТЕМЫ ===` | `IncidentService.stats()` и `SshMetricsService` | по ключевым словам в вопросе |

**Чтобы ассистент узнал что-то новое о сайте — правится только `backend/src/main/resources/ai/knowledge.md`, код трогать не нужно.**
Формат: раздел `## Заголовок`, следом строка `keywords: осн1, осн2`, дальше текст.

В `knowledge.md` НЕЛЬЗЯ класть пароли, секреты и внутренние адреса.

---

## Незавершённые задачи

> Проверяй этот раздел в начале сессии!

Нет незавершённых задач.

---

## Типовые ошибки и их решения

### Tailwind v4 классы
```
❌ w-[72px], h-[18px], max-w-[420px], flex-shrink-0, bg-gradient-to-r
✅ w-18,     h-4.5,    max-w-105,     shrink-0,      bg-linear-to-r
```

### Select в dark mode
```typescript
// ❌ Проблема: text не виден
dark:bg-white/5 dark:text-slate-200

// ✅ Решение: конкретные цвета
dark:bg-slate-800 dark:text-slate-100 dark:border-slate-600
```

### useSearchParams в Next.js App Router
Требует обёртку в `<Suspense>`:
```tsx
function PageContent() {
  const searchParams = useSearchParams();
  // ...
}
export default function Page() {
  return <Suspense><PageContent /></Suspense>;
}
```

### Checkbox peer-styling
```tsx
// ❌ Не работает: peer-checked не видит вложенные элементы
<input className="sr-only peer" /><span className="peer-checked:bg-blue-600">

// ✅ Работает: accent-color
<input type="checkbox" className="h-4 w-4 accent-blue-600" />
```

### Flyway — "схема для создания объектов не выбрана"
```properties
spring.flyway.schemas=public
spring.flyway.default-schema=public
spring.flyway.baseline-on-migrate=true
```

### Flyway — дублирующиеся версии при пересборке
Старые `.sql` файлы остаются в `target/classes/db/migration`. Всегда использовать `mvnw.cmd clean package`.

### JPA Specification с JOIN по коллекции
Если join идёт по OneToMany, обязательно добавить `query.distinct(true)`, иначе дубли строк.

### UPSERT в mongo_inout_stat
Уникальный индекс создан с `COALESCE`: `(subsystem, COALESCE(sender_id, ''), stat_year, stat_month)`.
`ON CONFLICT` в SQL должен использовать ту же формулу:
```sql
ON CONFLICT (subsystem, COALESCE(sender_id, ''), stat_year, stat_month)
DO UPDATE SET cnt = EXCLUDED.cnt, synced_at = EXCLUDED.synced_at
```

---

## Особенности проекта

- **Email**: все пользователи имеют домен `@enbek.kz`
- **Язык**: Russian (ru) + Kazakh (kz), хранится в `localStorage.language`
- **JWT**: 24 часа, HS256, secret в env `MONITORING_JWT_SECRET`
- **Аватары**: хранятся как `bytea` в PostgreSQL (max 5MB)
- **SSH метрики**: собираются через SSH (user: monitoringapp) с серверов
- **Скроллбар сайдбара**: класс `.sidebar-scroll` из globals.css

---

## Быстрый старт новой сессии

1. Прочти весь этот файл
2. Проверь раздел "Незавершённые задачи"
3. Уточни у пользователя что нужно сделать
4. Для изучения конкретного файла — используй `Read` напрямую (не переспрашивай)
5. При изменении стилей — проверяй что классы Tailwind v4 канонические
6. При добавлении новых страниц — обновляй `Sidebar.tsx` (массив `navigation`)
