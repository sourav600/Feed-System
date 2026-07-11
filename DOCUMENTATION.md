# Documentation

## What this is

A small social "news feed" app: register/login, a global feed of public posts plus your own private ones, posting text+image, likes (with "who liked" lists), and two-level comments/replies with their own likes. Built against the client's task spec, using the ByteByteGo "Design a News Feed System" chapter as the architectural reference (adapted - see below).

## Architecture

Three deployables:

- **`gateway-service`** (Spring Cloud Gateway Server WebMVC) - single public entry point, replaces a load balancer (there's only one backend instance today; the route becomes `lb://backend-service` with zero other changes the moment a second instance exists), CORS, and an in-memory Bucket4j rate limiter on login and post-creation.
- **`backend-service`** (Spring Boot, modular monolith) - all business logic: auth, users, posts, comments, likes, media.
- **`frontend`** (React + TypeScript, Vite, TanStack Query) - built against the provided Login/Register/Feed HTML/CSS template, with the missing pieces (first/last name fields, a public/private toggle, a "who liked" list) added in the template's own visual language.

## Key decisions, and why

**Fanout-on-read, no follow graph.** The ByteByteGo reference design fans posts out to *friends'* caches. This app has no friend/follow concept - every user sees every other user's public posts, full stop, with only a private/public flag as the visibility axis. That's a much simpler problem than the reference architecture: the "feed" is one global query (public posts + your own private ones, newest first), not a per-user precomputed timeline. No graph DB, no fanout workers, no hotkey problem.

**PostgreSQL**, chosen over MongoDB: the domain is relationally dense (users → posts → comments → likes, all FK-linked, with "who liked X" and visibility filters needing real joins and real referential integrity), and Spring Data JPA + Flyway is the natural fit for a Spring Boot backend.

**Keyset (cursor) pagination everywhere**, not offset pagination - required to stay fast as post/comment/like counts grow, per the spec's "assume millions of posts and reads." The feed query specifically needed a hand-written native SQL query (`UNION ALL` of two partial-index-backed branches: public posts, and the viewer's own private posts) because a single composite index can't serve an OR'd visibility predicate as an ordered, `LIMIT`-bounded scan at scale. Everything else (comments, replies, likers lists) uses a simpler JPQL keyset pattern since it doesn't need that OR/UNION.

**One `comments` table for both comments and replies** (self-referencing via `parent_comment_id`), and **one `likes` table for likes on both posts and comments** (nullable `post_id`/`comment_id`, `CHECK` exactly one is set). Both choices avoid duplicating near-identical code paths for what the spec only ever needs two levels of; a reply's endpoints are literally identical in shape to a top-level comment's.

**Visibility is enforced only in SQL `WHERE` clauses**, never as an application-level filter after fetching - and every comment/like endpoint keyed by a `postId` re-checks that post's visibility, not just the feed and get-post endpoints. Without that, a user could bypass a private post's privacy entirely by guessing its ID and hitting its comments/likes sub-resources directly.

**Auth: JWT in an httpOnly Secure cookie**, not `localStorage` - mitigates token theft via XSS. Paired with a short-lived access token + a server-side, rotating refresh-token table (real revocation on logout, without needing Redis for a V1). CSRF is handled by Spring Security's built-in cookie-based double-submit repository (not hand-rolled), since httpOnly cookies auto-attach cross-site and need that defense-in-depth.

**Local-disk image storage** for now, behind a `FileStorageService` interface - the explicit tradeoff is that this doesn't survive running more than one backend instance; swapping to S3/object storage later is a new implementation of that interface, not a rewrite of callers.

## Deviations from the original plan (found while building)

Two things surfaced only once written out in full and reviewed line-by-line (no JDK was available in the build environment to catch them via compilation - see below):

- The original like/unlike design was a JPA `save()` wrapped in a try/catch for the unique-constraint violation. On PostgreSQL, a failed statement poisons the whole transaction, so that pattern would have broken the very next query in the same request. Fixed to a single atomic `INSERT ... ON CONFLICT DO NOTHING` native query instead.
- Spring Cloud Gateway Server WebMVC (the servlet-based Gateway flavor used here, matching the rest of the stack) turned out to bind routes under `spring.cloud.gateway.server.webmvc.*`, not the classic reactive Gateway's `spring.cloud.gateway.*`, and has no `globalcors` equivalent at all. Routes were moved to the correct property path; CORS is instead a plain `WebMvcConfigurer` bean, which applies to gateway-routed requests the same as any other Spring MVC endpoint.

## Known limitation: unverified build

**No JDK, Maven, PostgreSQL, or Docker were available in the environment this was built in** - only Node.js was present. The Java code (backend-service, gateway-service) was therefore never compiled or run; the frontend was type-checked, linted, and production-built successfully, and its dev server was smoke-tested serving the login page. The backend/gateway code was instead put through a dedicated static read-through review pass (checking imports, method signatures against our own definitions, Lombok/JPA/Hibernate correctness, and the two Gateway-specific API questions above against upstream source) - seven real issues were found and fixed that way, but that is not a substitute for an actual `mvn compile`/`mvn test` run. Treat first-build compiler output as authoritative over anything claimed here.

## Explicitly out of scope

Forgot-password, push notifications, a load balancer (replaced by the gateway), a friend/follow graph, and the GitHub/video/live-deploy deliverables (per instruction - only this document was requested).

## Future scalability (documented, not built)

Redis cache for the public feed's first page(s) if read QPS grows; Postgres read replicas; a hybrid fanout-on-write *for the shared public timeline only* if the feed's `UNION ALL` merge becomes the bottleneck (cheap here since it's one shared cache, not one per follower - there's no follow graph); move image storage to S3/object storage + CDN and the refresh-token store/rate limiter to Redis once running more than one backend instance.
