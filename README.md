# News Feed App

Three services: `gateway-service` (Spring Cloud Gateway, single entry point), `backend-service` (Spring Boot API), `frontend` (React + TypeScript). See [DOCUMENTATION.md](DOCUMENTATION.md) for what was built and why.

## Prerequisites

- JDK 21 (Maven itself is not required - both Java services ship `mvnw`/`mvnw.cmd`)
- PostgreSQL 14+ running locally
- Node 20+

None of this was verified locally in the environment this was built in (no JDK/Maven/PostgreSQL were installed there) - the code was written and the backend design reviewed carefully, but you'll be the first to actually compile/run it. Start with the backend and fix forward from whatever the compiler says.

## 1. Database

```sql
CREATE DATABASE newsfeed;
CREATE USER newsfeed WITH PASSWORD 'newsfeed';
GRANT ALL PRIVILEGES ON DATABASE newsfeed TO newsfeed;
```

Flyway (`backend-service/src/main/resources/db/migration/V1__init_schema.sql`) creates the schema - including the `citext` extension - automatically on first run. No manual DDL needed beyond creating the empty database above.

## 2. backend-service (port 8081)

```
cd backend-service
./mvnw spring-boot:run
```

Key environment variables (all have dev-safe defaults in `application.yml` - override for anything beyond local dev):

| Variable | Default | Notes |
|---|---|---|
| `DB_USERNAME` / `DB_PASSWORD` | `newsfeed` / `newsfeed` | |
| `JWT_SECRET` | a dev placeholder | **must** be overridden outside local dev, 256+ bits |
| `COOKIE_SECURE` | `true` | set to `false` only for plain-HTTP local dev |
| `FRONTEND_ORIGIN` | `http://localhost:5173` | CORS allow-list |
| `MEDIA_STORAGE_PATH` | `./media` | local-disk image uploads |

## 3. gateway-service (port 8080)

```
cd gateway-service
./mvnw spring-boot:run
```

Env vars: `BACKEND_URI` (default `http://localhost:8081`), `FRONTEND_ORIGIN` (default `http://localhost:5173`).

## 4. frontend (port 5173)

```
cd frontend
npm install
npm run dev
```

Talks to the gateway at `VITE_API_BASE_URL` (default `http://localhost:8080` - copy `.env.example` to `.env.local` to override).

## Smoke test

1. Open `http://localhost:5173/register`, create an account.
2. You should land on the feed, logged in.
3. Create a public post and a private post; open a second browser/incognito session, register a second user, confirm only the public one is visible.
4. Like/unlike, comment, reply, like a reply, open "who liked" on a post and a comment.
5. Log out, confirm `/` redirects to `/login`.
