package com.newsfeed.backend.comment;

import com.newsfeed.backend.common.BaseEntity;
import com.newsfeed.backend.post.Post;
import com.newsfeed.backend.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single self-referencing table backs both top-level comments and replies ({@code
 * parentComment == null} means top-level) - the spec only needs 2 levels of nesting, so depth is
 * enforced in {@link CommentService}, not the schema. Every row denormalizes {@code post}
 * directly so a visibility re-check never has to walk the parent chain, and it means a reply's
 * likes/replies endpoints are identical in shape to a top-level comment's - see {@link
 * com.newsfeed.backend.like.Like} for the matching decision on likes.
 */
@Getter
@Setter
@Entity
@Table(name = "comments")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    private int likeCount = 0;

    @Builder.Default
    private int replyCount = 0;

    private Instant deletedAt;

    public boolean isTopLevel() {
        return parentComment == null;
    }
}
