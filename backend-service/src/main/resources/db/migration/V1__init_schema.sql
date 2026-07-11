CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE users (
  id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  first_name     VARCHAR(100) NOT NULL,
  last_name      VARCHAR(100) NOT NULL,
  email          CITEXT NOT NULL UNIQUE,
  password_hash  VARCHAR(60) NOT NULL,
  avatar_url     VARCHAR(500),
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE posts (
  id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  author_id      BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  content        TEXT NOT NULL,
  image_url      VARCHAR(500),
  visibility     VARCHAR(10) NOT NULL DEFAULT 'PUBLIC' CHECK (visibility IN ('PUBLIC','PRIVATE')),
  like_count     INT NOT NULL DEFAULT 0,
  comment_count  INT NOT NULL DEFAULT 0,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at     TIMESTAMPTZ
);

CREATE TABLE comments (
  id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  post_id            BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
  parent_comment_id  BIGINT REFERENCES comments(id) ON DELETE CASCADE,
  author_id          BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  content            TEXT NOT NULL,
  like_count         INT NOT NULL DEFAULT 0,
  reply_count        INT NOT NULL DEFAULT 0,
  created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at         TIMESTAMPTZ
);

CREATE TABLE likes (
  id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  post_id     BIGINT REFERENCES posts(id) ON DELETE CASCADE,
  comment_id  BIGINT REFERENCES comments(id) ON DELETE CASCADE,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT chk_likes_exactly_one_target
    CHECK ( (post_id IS NOT NULL)::int + (comment_id IS NOT NULL)::int = 1 )
);

CREATE TABLE refresh_tokens (
  id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id                 BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token_hash              CHAR(64) NOT NULL UNIQUE,
  issued_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
  expires_at              TIMESTAMPTZ NOT NULL,
  revoked_at              TIMESTAMPTZ,
  replaced_by_token_hash  CHAR(64),
  user_agent              VARCHAR(255),
  ip_address              VARCHAR(45)
);

-- Likes: unique per (user, target) + fast keyset scans per target
CREATE UNIQUE INDEX uq_likes_user_post    ON likes (user_id, post_id)    WHERE post_id IS NOT NULL;
CREATE UNIQUE INDEX uq_likes_user_comment ON likes (user_id, comment_id) WHERE comment_id IS NOT NULL;
CREATE INDEX idx_likes_post_feed    ON likes (post_id, created_at DESC, id DESC)    WHERE post_id IS NOT NULL;
CREATE INDEX idx_likes_comment_feed ON likes (comment_id, created_at DESC, id DESC) WHERE comment_id IS NOT NULL;

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id) WHERE revoked_at IS NULL;

CREATE INDEX idx_comments_post_top_level ON comments (post_id, created_at DESC, id DESC)
  WHERE parent_comment_id IS NULL AND deleted_at IS NULL;
CREATE INDEX idx_comments_parent ON comments (parent_comment_id, created_at DESC, id DESC)
  WHERE deleted_at IS NULL;

-- Global feed: two purpose-built partial indexes, unioned at query time (see PostRepositoryImpl)
CREATE INDEX idx_posts_public_feed ON posts (created_at DESC, id DESC)
  WHERE visibility = 'PUBLIC' AND deleted_at IS NULL;
CREATE INDEX idx_posts_author_feed ON posts (author_id, created_at DESC, id DESC)
  WHERE deleted_at IS NULL;
