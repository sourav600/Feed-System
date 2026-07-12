package com.newsfeed.backend.post;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    /** Single-post visibility check baked into the WHERE clause - see the class doc on {@link Post#isVisibleTo}. */
    @Query("""
            SELECT p FROM Post p
            WHERE p.id = :id AND p.deletedAt IS NULL AND (p.visibility = 'PUBLIC' OR p.author.id = :viewerId)
            """)
    Optional<Post> findVisibleForViewer(@Param("id") Long id, @Param("viewerId") Long viewerId);

    /**
     * Global feed, fanout-on-read: everyone's PUBLIC posts unioned with the viewer's own PRIVATE
     * posts, newest first, keyset-paginated. Two purpose-built partial indexes back the two
     * branches ({@code idx_posts_public_feed}, {@code idx_posts_author_feed} - see the V1
     * migration) - a single composite index on (visibility, created_at, id) can't serve an OR
     * predicate as an ordered, LIMIT-bounded scan at "millions of posts" scale, so this is
     * deliberately native SQL rather than a JPQL/Criteria query. {@code fetchSize} is
     * {@code limit + 1} (over-fetch by one so the caller can detect hasMore without a second
     * COUNT query) - see {@code CursorPageResponse.fromOverFetch}.
     */
    @Query(value = """
            (SELECT * FROM posts p
               WHERE p.visibility = 'PUBLIC' AND p.deleted_at IS NULL
                 AND (CAST(:cursorCreatedAt AS timestamptz) IS NULL
                      OR (p.created_at, p.id) < (CAST(:cursorCreatedAt AS timestamptz), CAST(:cursorId AS bigint)))
             ORDER BY p.created_at DESC, p.id DESC
             LIMIT :fetchSize)
            UNION ALL
            (SELECT * FROM posts p
               WHERE p.visibility = 'PRIVATE' AND p.author_id = :viewerId AND p.deleted_at IS NULL
                 AND (CAST(:cursorCreatedAt AS timestamptz) IS NULL
                      OR (p.created_at, p.id) < (CAST(:cursorCreatedAt AS timestamptz), CAST(:cursorId AS bigint)))
             ORDER BY p.created_at DESC, p.id DESC
             LIMIT :fetchSize)
            ORDER BY created_at DESC, id DESC
            LIMIT :fetchSize
            """, nativeQuery = true)
    List<Post> findFeedPage(
            @Param("viewerId") Long viewerId,
            @Param("cursorCreatedAt") Instant cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            @Param("fetchSize") int fetchSize);

    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.id = :id AND p.likeCount > 0")
    void decrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.id = :id")
    void incrementCommentCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Post p SET p.commentCount = p.commentCount - 1 WHERE p.id = :id AND p.commentCount > 0")
    void decrementCommentCount(@Param("id") Long id);
}
