# High-Level Design — News Feed System

A small social news-feed app: register/login, a global feed of public posts (plus your own private ones), text+image posting, likes with "who liked" lists, and two-level comments/replies with their own likes.

## 1. System Context (deployables + external users)

```mermaid
flowchart LR
    User["User\n(Browser)"]
    Admin["(Future)\nOperator"]

    subgraph Sys["News Feed System"]
      FE["frontend\nReact + TS + Vite\n(TanStack Query)"]
      GW["gateway-service\nSpring Cloud Gateway (WebMVC)\n- CORS\n- Bucket4j rate limiter\n  (login / post-create)"]
      BE["backend-service\nSpring Boot modular monolith\n- auth\n- users\n- posts\n- comments\n- likes\n- media"]
    end

    subgraph Backing["Backing services"]
      PG[("PostgreSQL\nusers, posts, comments,\nlikes, refresh_tokens")]
      RS[("Redis\npost:like_count:{id}\ndirty:post_like_counts")]
      Disk[("Local disk\n/media — uploaded images")]
    end

    User -->|"HTTPS\n(httpOnly Secure cookies)"| FE
    FE -->|"REST /api/...\n(credentials: include)"| GW
    GW -->|"reverse proxy\n(forwards cookies)"| BE
    BE --> PG
    BE --> RS
    BE --> Disk
    Admin -.-> Sys
```

### Three deployables

| Deployable | Tech | Role |
|------------|------|------|
| `frontend` | React + TypeScript + Vite, TanStack Query | Mustache-style template + missing features (private toggle, "who liked" list). Holds no auth token in JS — the JWT lives in an httpOnly Secure cookie so XSS can't read it. |
| `gateway-service` | Spring Cloud Gateway (servlet/WebMVC flavor) | Single public entry point (replaces a load balancer when only one backend exists), CORS, and Bucket4j in-memory per-IP rate limiter on `POST /api/auth/login` and `POST /api/posts`. |
| `backend-service` | Spring Boot modular monolith | All business logic: `auth/`, `user/`, `post/`, `comment/`, `like/`, `media/`, plus `security/` (CookieService, JwtAuthenticationFilter, CSRF) and `common/` (cursor pagination). |

### Two backing services (one staged via docker-compose)

| Service | Used for | Source of truth? |
|---------|----------|------------------|
| **PostgreSQL** | `users`, `posts`, `comments`, `likes`, `refresh_tokens` | Yes — relational source of truth for everything except the live like counter |
| **Redis** | `post:like_count:{id}` STRING counter; `dirty:post_like_counts` SET | Live like count (eventually reconciled into `posts.like_count` by `LikeCountFlushJob` every 5s) |
| **Local disk** (`/media`) | Uploaded post images | Yes (today) — explicit limitation: doesn't survive >1 backend instance; `FileStorageService` interface lets you swap to S3+CDN later |

## 2. Request lifecycle (auth + JWT cookies)

```mermaid
sequenceDiagram
    autonumber
    participant U as Browser
    participant F as frontend (React)
    participant G as gateway-service
    participant B as backend-service
    participant PG as PostgreSQL

    Note over U,B: Login — cookie issuance
    U->>F: click "Sign in"
    F->>G: POST /api/auth/login  (no cookie yet)
    G->>B: forward (rate-limiter checks IP bucket)
    B->>PG: SELECT user by email
    B->>PG: INSERT refresh_tokens (hashed)
    B-->>G: 200 + Set-Cookie: access_token (HttpOnly, Secure, SameSite=Lax)   +   Set-Cookie: refresh_token (HttpOnly, Secure, SameSite=Strict, Path=/api/auth/refresh)
    G-->>F: 200 + Set-Cookie headers (gateway forwards them)
    F-->>U: 200 (browser stores cookies)

    Note over U,B: Subsequent request — cookie auto-attached
    U->>F: open feed
    F->>G: GET /api/posts  (browser adds Cookie: access_token=...)
    G->>B: forward (Cookie preserved by gateway config)
    B->>B: JwtAuthenticationFilter reads access_token cookie, validates JWT
    B->>PG: SELECT posts WHERE visibility=PUBLIC OR author_id=:me  (keyset)
    B-->>G: 200 JSON (feed page)
    G-->>F: 200
    F-->>U: render feed
```

### Key security choices
- **JWT lives in `httpOnly + Secure + SameSite=Lax` cookie**, never `localStorage`. Any JS (incl. XSS) cannot read it → token-theft-resistant.
- **Refresh token is opaque** (random bytes), SHA-256 **hashed** at rest in `refresh_tokens`, scoped to `Path=/api/auth/refresh`, `SameSite=Strict`, rotated on every refresh (replaced-by chain stored for replay detection).
- **CSRF** defense-in-depth is mandatory because the browser auto-attaches the cookie on cross-site requests. Handled by Spring Security's `CookieCsrfTokenRepository` double-submit pattern (`XSRF-TOKEN` cookie JS-readable, read by SPA and echoed back as `X-XSRF-TOKEN` header).
- **Logout** = `clearAuthCookies` (cookies with `Max-Age=0`) + revoke the refresh-token row → real server-side revocation, no Redis denylist needed for V1.

## 3. Backend modules (modular monolith boundary)

```mermaid
flowchart TB
    subgraph Gateway["gateway-service"]
      GWFilter["RateLimitFilter\nlogin + post buckets"]
      GWConfig["GatewayConfig\nroutes, cookie passthrough"]
      GWCors["WebMvcConfigurer\nCORS"]
    end

    subgraph Backend["backend-service"]
      subgraph Edge["edge/web"]
        AC[AuthController]
        UC[UserController]
        PC[PostController]
        CC[CommentController]
        LC[LikeController]
        MC[MediaController]
      end

      subgraph Security["security"]
        SC["SecurityConfig: filter chain + CSRF + cookie repo"]
        JF[JwtAuthenticationFilter]
        CS[CookieService]
      end

      subgraph Domain["domain modules"]
        AS["AuthService + RefreshToken rotation"]
        US[UserService]
        PS[PostService]
        CMS[CommentService]
        LS[LikeService]
        MS["FileStorageService\n(local-disk impl)"]
        LJ["LikeCountFlushJob\n@Scheduled"]
      end

      subgraph Pers["persistence"]
        PR["PostRepository + Impl\n(custom native feed query)"]
        LR[LikeRepository]
        CR[CommentRepository]
        RR[RefreshTokenRepository]
        UR[UserRepository]
      end
    end

    GWConfig -->|"proxy /api/*"| AC
    GWConfig --> UC
    GWConfig --> PC
    GWConfig --> CC
    GWConfig --> LC
    GWFilter -.->|"intercepts POST /api/auth/login\nand POST /api/posts"| GWConfig

    SC --> JF
    JF -->|reads access_token cookie| CS
    AC --> AS
    AS --> RR
    AS --> CS
    PC --> PS --> PR
    CC --> CMS --> CR
    LC --> LS
    LS --> LR
    LS -.->|"INCR/DECR/SADD"| Redis[("Redis")]
    LJ -.->|"SMEMBERS, SREM, GET"| Redis
    LJ --> PR
    MC --> MS
```

## 4. Data model (PostgreSQL)

```mermaid
erDiagram
    users ||--o{ posts : "authors"
    users ||--o{ comments : "authors"
    users ||--o{ likes : "writers"
    users ||--o{ refresh_tokens : "owns"
    posts ||--o{ comments : "has"
    posts ||--o{ likes : "receives"
    comments ||--o{ likes : "receives"
    comments }o--o| comments : "parent_comment_id\n(2-level replies)"

    users {
      bigint   id PK
      varchar  first_name
      varchar  last_name
      citext   email UK
      varchar  password_hash "BCrypt, 60 chars"
      varchar  avatar_url
      timestamptz created_at
    }

    posts {
      bigint   id PK
      bigint   author_id FK
      text     content
      varchar  image_url
      varchar  visibility "PUBLIC | PRIVATE"
      int      like_count "denormalized; flush job writes"
      int      comment_count
      timestamptz created_at
      timestamptz deleted_at
    }

    comments {
      bigint   id PK
      bigint   post_id FK
      bigint   parent_comment_id FK "NULL = top-level"
      bigint   author_id FK
      text     content
      int      like_count
      int      reply_count
      timestamptz deleted_at
    }

    likes {
      bigint   id PK
      bigint   user_id FK
      bigint   post_id FK "NULLABLE — exactly one of post_id / comment_id"
      bigint   comment_id FK "NULLABLE"
      timestamptz created_at
    }

    refresh_tokens {
      bigint       id PK
      bigint       user_id FK
      char         token_hash UK "SHA-256 hex, 64 chars"
      timestamptz  issued_at
      timestamptz  expires_at
      timestamptz  revoked_at "NULL = active"
      char         replaced_by_token_hash "rotation chain"
      varchar      user_agent
      varchar      ip_address
    }
```

### Schema design choices
- **One `comments` table** for both top-level comments and replies (self-referencing `parent_comment_id`, capped at 2 levels) — avoids duplicate near-identical code paths; reply endpoints mirror comment endpoints.
- **One `likes` table** for both posts and comments — `CHECK ((post_id IS NOT NULL)::int + (comment_id IS NOT NULL)::int = 1)` enforces exactly one target. Partial unique indexes `uq_likes_user_post` / `uq_likes_user_comment` make duplicate-likes physically impossible at the DB layer.
- **No friend/follow graph** — visibility is a single `PUBLIC`/`PRIVATE` flag on `posts`. Every user sees every public post; users see their own private posts. Eliminates the fanout-on-write worker, graph DB, and hot-key problems from the ByteByteGo reference design.
- **Partial indexes everywhere** they ship predicate-shaped scans:
  - `idx_posts_public_feed` on `(created_at DESC, id DESC) WHERE visibility='PUBLIC' AND deleted_at IS NULL`
  - `idx_posts_author_feed` on `(author_id, created_at DESC, id DESC) WHERE deleted_at IS NULL`
  
  The feed query is a **`UNION ALL` of these two branches** — a single composite index can't serve an OR'd visibility predicate as a bounded ordered scan.

## 5. Feed read path (keyset pagination everywhere)

```mermaid
sequenceDiagram
    autonumber
    participant U as Browser
    participant F as frontend
    participant G as gateway-service
    participant B as backend-service
    participant PG as PostgreSQL

    U->>F: open feed
    F->>G: GET /api/posts?cursor=<base64>&limit=20  (Cookie auto-attached)
    G->>B: forward
    B->>B: JwtAuthenticationFilter → resolve viewer id
    B->>B: Cursor.decode(cursor) → (createdAt, id)
    B->>PG: (SELECT * FROM posts WHERE visibility='PUBLIC' AND (created_at,id) < (cct,cid)\n      UNION ALL\n      SELECT * FROM posts WHERE author_id=:me AND (created_at,id) < (cct,cid)\n     ) ORDER BY created_at DESC, id DESC LIMIT 21
    PG-->>B: 21 rows (over-fetch by 1 to detect next page)
    B->>B: CursorPageResponse.fromOverFetch(...) → drop 21st, encode next cursor
    B-->>G: 200 JSON
    G-->>F: 200
    F-->>U: render 20 posts + "Load more" button if hasNext
```

- **Keyset not offset** — `WHERE (created_at, id) < (:cursor_created_at, :cursor_id)` keeps the scan bounded as the table grows; the spec calls out "millions of posts and reads" and offset pagination degrades to skipping N rows.
- **Over-fetch by 1** (`limit+1`) is how `hasNext` is computed — no separate count query.
- **Visibility is enforced only in SQL `WHERE` clauses**, never as a post-fetch application filter. Every comment/like sub-resource endpoint keyed by `postId` re-checks that post's visibility — otherwise guessing a private post's ID would leak its comments/likers.

## 6. Like write path (write-behind cache)

```mermaid
sequenceDiagram
    autonumber
    participant U as User
    participant F as frontend
    participant G as gateway
    participant B as backend-service
    participant RR as Redis
    participant FJ as LikeCountFlushJob
    participant PG as PostgreSQL

    U->>F: click Like
    F->>G: POST /api/posts/{id}/likes (Cookie)
    G->>B: forward
    B->>PG: INSERT INTO likes (user_id, post_id) VALUES (?, ?)\n  ON CONFLICT (user_id, post_id) DO NOTHING
    PG-->>B: rows-affected = 1 (new like) | 0 (already liked)
    alt rows > 0
        B->>RR: INCR post:like_count:{id}
        B->>RR: SADD dirty:post_like_counts {id}
    end
    B->>RR: GET post:like_count:{id}
    alt miss
        B->>PG: SELECT like_count FROM posts WHERE id=? (cache-through fallback)
        B->>RR: SETNX post:like_count:{id} {dbCount}
    end
    B-->>G: 200 {"likeCount": N}
    G-->>F: 200
    F-->>U: heart turns solid, count N

    Note over FJ,PG: Background reconciliation — every 5s, decoupled from request traffic
    FJ->>RR: SMEMBERS dirty:post_like_counts
    loop for each dirty postId
        FJ->>RR: GET post:like_count:{id}
        FJ->>PG: UPDATE posts SET like_count=? WHERE id=?   (absolute SET, not +1)
        FJ->>RR: SREM dirty:post_like_counts {id}
    end
```

Design rationale — the **hot Postgres row is touched once per 5s per post, regardless of how many likes landed**. A viral post getting 10k likes in 5s costs one DB write, not 10k. Absolute `SET` (not `+1`) makes it crash-safe and idempotent: a mid-flush crash only leaves Postgres stale, never corrupted.

## 7. Auth & refresh-token rotation

```mermaid
sequenceDiagram
    autonumber
    participant U as Browser
    participant F as frontend
    participant G as gateway
    participant B as backend-service
    participant PG as PostgreSQL

    Note over U,PG: Normal request with valid access_token cookie — happens silently
    U->>F: action
    F->>G: GET /api/posts (Cookie: access_token=...)
    G->>B: forward
    B->>B: JwtAuthenticationFilter validates JWT signature + exp
    B-->>G: 200

    Note over U,PG: Access token expired — refresh flow
    U->>F: action
    F->>G: GET /api/posts (Cookie: access_token=<expired>, refresh_token=<valid>)
    G->>B: forward
    B->>B: JWT validation → 401
    F->>G: POST /api/auth/refresh  (Cookie: refresh_token=<valid> — browser sends because Path matches)
    G->>B: forward
    B->>PG: SELECT refresh_tokens WHERE token_hash = SHA256(raw) AND revoked_at IS NULL
    PG-->>B: row
    B->>PG: UPDATE old row SET revoked_at=now(), replaced_by_token_hash = <new hash>
    B->>PG: INSERT new refresh_tokens (token_hash=<new>, user_agent, ip)
    B-->>G: 200 + Set-Cookie: access_token=<new jwt>  +  Set-Cookie: refresh_token=<new opaque>
    G-->>F: 200 + Set-Cookie (forwards)
    F-->>U: original request retried with fresh access_token

    Note over U,PG: Logout
    U->>F: click Logout
    F->>G: POST /api/auth/logout
    G->>B: forward
    B->>PG: UPDATE refresh_tokens SET revoked_at=now() WHERE token_hash=SHA256(...)
    B-->>G: 200 + Set-Cookie: access_token= (Max-Age=0)  +  refresh_token= (Max-Age=0)
    G-->>F: 200 + clear cookies
    F-->>U: redirect to /login
```

Tokens are stored only as **SHA-256 hashes** in `refresh_tokens` — the raw value exists only transiently to be written into the Set-Cookie header, never persisted, never read back.

## 8. Cross-cutting concerns

```mermaid
flowchart LR
    subgraph CC["Cross-cutting"]
        RL["Rate limiting\n(RateLimitFilter)\nper-IP token bucket via Bucket4j\nspring.cloud.gateway native would tie us to unstable SPI"]
        CORS["CORS\nWebMvcConfigurer bean\n(allows-credentials=true for cookies)"]
        CSRF["CSRF\nCookieCsrfTokenRepository (double-submit)\nmandatory because httpOnly cookies auto-attach cross-site"]
        JWT["JWT validation\nJwtAuthenticationFilter\nreads access_token cookie, never an Authorization header"]
        VIS["Visibility enforcement\nWHERE clause in every post-keyed query (feed, get, comments, likers)"]
        PAG["Cursor pagination\ncommon.pagination.Cursor\nbase64 of (createdAt,id) — over-fetch by 1 for hasNext"]
        MIG["Migrations\nFlyway V1__init_schema.sql (schema + partial indexes)"]
    end
    RL -.-> CC
    CORS -.-> CC
    CSRF -.-> CC
    JWT -.-> CC
    VIS -.-> CC
    PAG -.-> CC
    MIG -.-> CC
```

## 9. Future scalability (documented in DOCUMENTATION.md, not built)

```mermaid
flowchart LR
    Now["Today\n1 backend instance"] -->|"add instances"| Scale["Scale out\nN backend instances"]
    Now -->|"add read QPS"| RCache["Redis cache first page(s) of public feed"]
    Now -->|"feed UNION bottleneck"| Fan["Hybrid fanout-on-write for the shared public timeline only\n(cheap: one shared cache, no follow graph)"]
    Now -->|"add replicas"| Repl["Postgres read replicas\n(reads split off writes)"]
    Now -->|"swap impl"| S3["S3 + CDN for images\n(new FileStorageService impl)"]
    Now -->|"swap impl"| RToken["Refresh-token store + rate limiter backed by Redis\n(shared across instances per IP)"]
    Now -->|"swap route"| LB["Gateway route becomes lb://backend-service\n(no other code changes)"]
```

## Key design principles referenced by the diagrams
- **Fanout-on-read, no follow graph** — the entire feed is one `UNION ALL` query, not a precomputed per-user timeline.
- **Write-behind cache for like counts** — Redis counters per hot post; Postgres denormalized `like_count` reconciled every 5s by `LikeCountFlushJob` with absolute `SET`.
- **PostgreSQL over MongoDB** — relationship-dense domain (`users → posts → comments → likes`), real joins, real FK integrity.
- **JWT in httpOnly Secure cookies** (not `Authorization` header / `localStorage`) — XSS-resistant at the cost of mandatory CSRF defense.
- **Keyset pagination everywhere** — bounded scans as the tables grow to millions of rows.
- **Visibility enforced in SQL `WHERE` only**, never post-fetch — every `postId`-keyed endpoint re-checks.
- **Single `comments` table** (self-ref, 2 levels) and **single `likes` table** (CHECK exactly-one-target) — DRY for the only two shapes the spec needs.
- **Local-disk images behind `FileStorageService`** — the limitation (no multi-instance survival) is documented; swapping to S3+CDN is a new impl, not a caller rewrite.