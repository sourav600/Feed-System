package com.newsfeed.backend.comment;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Comments/replies have no visibility flag of their own - they inherit the parent post's, so
     * every comment query here joins back to {@code c.post} and re-applies the same PUBLIC-or-own
     * predicate used by the feed. This is the check called out in the plan as easiest to forget:
     * without it, a user could bypass a private post's visibility by guessing its id and hitting
     * the comments/replies/likes sub-resources directly, even though it never appears in their feed.
     */
    @Query("""
            SELECT c FROM Comment c
            WHERE c.id = :id AND c.deletedAt IS NULL
              AND (c.post.visibility = 'PUBLIC' OR c.post.author.id = :viewerId)
            """)
    Optional<Comment> findVisibleForViewer(@Param("id") Long id, @Param("viewerId") Long viewerId);

    @Query("""
            SELECT c FROM Comment c JOIN FETCH c.author
            WHERE c.post.id = :postId AND c.parentComment IS NULL AND c.deletedAt IS NULL
              AND (c.post.visibility = 'PUBLIC' OR c.post.author.id = :viewerId)
              AND (cast(:cursorCreatedAt as Instant) IS NULL OR c.createdAt < :cursorCreatedAt
                   OR (c.createdAt = :cursorCreatedAt AND c.id < :cursorId))
            ORDER BY c.createdAt DESC, c.id DESC
            """)
    List<Comment> findTopLevelPage(
            @Param("postId") Long postId,
            @Param("viewerId") Long viewerId,
            @Param("cursorCreatedAt") Instant cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable);

    @Query("""
            SELECT c FROM Comment c JOIN FETCH c.author
            WHERE c.parentComment.id = :parentCommentId AND c.deletedAt IS NULL
              AND (c.post.visibility = 'PUBLIC' OR c.post.author.id = :viewerId)
              AND (cast(:cursorCreatedAt as Instant) IS NULL OR c.createdAt < :cursorCreatedAt
                   OR (c.createdAt = :cursorCreatedAt AND c.id < :cursorId))
            ORDER BY c.createdAt DESC, c.id DESC
            """)
    List<Comment> findRepliesPage(
            @Param("parentCommentId") Long parentCommentId,
            @Param("viewerId") Long viewerId,
            @Param("cursorCreatedAt") Instant cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable);

    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount - 1 WHERE c.id = :id AND c.likeCount > 0")
    void decrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Comment c SET c.replyCount = c.replyCount + 1 WHERE c.id = :id")
    void incrementReplyCount(@Param("id") Long id);
}
