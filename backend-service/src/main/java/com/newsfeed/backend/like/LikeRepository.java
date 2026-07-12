package com.newsfeed.backend.like;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUser_IdAndPost_Id(Long userId, Long postId);

    Optional<Like> findByUser_IdAndComment_Id(Long userId, Long commentId);

    long countByPost_Id(Long postId);

    long countByComment_Id(Long commentId);

    /**
     * Atomic insert-or-noop via Postgres's {@code ON CONFLICT DO NOTHING} against the partial
     * unique index {@code uq_likes_user_post} - deliberately NOT a JPA {@code save()} wrapped in a
     * try/catch for the unique violation: on PostgreSQL a failed statement aborts the whole
     * transaction, so catching the exception and continuing in the same transaction (as an
     * app-level check-then-insert would tempt you to do) would make the very next statement in
     * that transaction throw instead. Returns 1 if a new row was inserted, 0 if it already existed.
     */
    @Modifying
    @Query(value = """
            INSERT INTO likes (user_id, post_id, created_at) VALUES (:userId, :postId, now())
            ON CONFLICT (user_id, post_id) WHERE post_id IS NOT NULL DO NOTHING
            """, nativeQuery = true)
    int insertPostLikeIfAbsent(@Param("userId") Long userId, @Param("postId") Long postId);

    @Modifying
    @Query(value = """
            INSERT INTO likes (user_id, comment_id, created_at) VALUES (:userId, :commentId, now())
            ON CONFLICT (user_id, comment_id) WHERE comment_id IS NOT NULL DO NOTHING
            """, nativeQuery = true)
    int insertCommentLikeIfAbsent(@Param("userId") Long userId, @Param("commentId") Long commentId);

    /** Batched "does the current user like these posts" lookup - avoids N+1 when hydrating a feed page. */
    @Query("SELECT l.post.id FROM Like l WHERE l.user.id = :userId AND l.post.id IN :postIds")
    Set<Long> findLikedPostIds(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);

    @Query("SELECT l.comment.id FROM Like l WHERE l.user.id = :userId AND l.comment.id IN :commentIds")
    Set<Long> findLikedCommentIds(@Param("userId") Long userId, @Param("commentIds") List<Long> commentIds);

    // Keyset pagination emulated via OR rather than a native row-value comparison (unlike the feed
    // query in PostRepositoryImpl) - simpler here since there's no visibility UNION to also express,
    // and Postgres still walks idx_likes_post_feed / idx_likes_comment_feed for it.
    @Query("""
            SELECT l FROM Like l JOIN FETCH l.user
            WHERE l.post.id = :postId
              AND (cast(:cursorCreatedAt as Instant) IS NULL OR l.createdAt < :cursorCreatedAt
                   OR (l.createdAt = :cursorCreatedAt AND l.id < :cursorId))
            ORDER BY l.createdAt DESC, l.id DESC
            """)
    List<Like> findPostLikersPage(
            @Param("postId") Long postId,
            @Param("cursorCreatedAt") Instant cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable);

    @Query("""
            SELECT l FROM Like l JOIN FETCH l.user
            WHERE l.comment.id = :commentId
              AND (cast(:cursorCreatedAt as Instant) IS NULL OR l.createdAt < :cursorCreatedAt
                   OR (l.createdAt = :cursorCreatedAt AND l.id < :cursorId))
            ORDER BY l.createdAt DESC, l.id DESC
            """)
    List<Like> findCommentLikersPage(
            @Param("commentId") Long commentId,
            @Param("cursorCreatedAt") Instant cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable);

    // Explicit bulk-delete JPQL (rather than a derived `deleteBy...` method) so the @Modifying +
    // affected-row-count contract is unambiguous.
    @Modifying
    @Query("DELETE FROM Like l WHERE l.user.id = :userId AND l.post.id = :postId")
    long deleteByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    @Modifying
    @Query("DELETE FROM Like l WHERE l.user.id = :userId AND l.comment.id = :commentId")
    long deleteByUserIdAndCommentId(@Param("userId") Long userId, @Param("commentId") Long commentId);
}
