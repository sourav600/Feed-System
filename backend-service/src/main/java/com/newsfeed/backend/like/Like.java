package com.newsfeed.backend.like;

import com.newsfeed.backend.comment.Comment;
import com.newsfeed.backend.post.Post;
import com.newsfeed.backend.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Exactly one of {@code post}/{@code comment} is set - enforced by the DB {@code CHECK}
 * constraint. See {@link com.newsfeed.backend.comment.Comment} for why replies don't need a
 * third target column.
 *
 * <p>Read-only from the application's point of view: rows are written exclusively via the native
 * {@code INSERT ... ON CONFLICT DO NOTHING} statements in {@link LikeRepository} (see
 * {@link LikeService} for why - a plain JPA {@code save()} + catch-the-unique-violation pattern
 * is unsafe on PostgreSQL, since a failed statement poisons the whole transaction). This entity
 * exists purely so JPQL queries (likers lists, counts, "does this user like X") have something to
 * map result rows onto.
 */
@Getter
@Entity
@Table(name = "likes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Column(nullable = false)
    private Instant createdAt;
}
