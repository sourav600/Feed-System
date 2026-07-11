package com.newsfeed.backend.post;

import com.newsfeed.backend.common.BaseEntity;
import com.newsfeed.backend.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Getter
@Setter
@Entity
@Table(name = "posts")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private Visibility visibility = Visibility.PUBLIC;

    @Builder.Default
    private int likeCount = 0;

    @Builder.Default
    private int commentCount = 0;

    private Instant deletedAt;

    public boolean isVisibleTo(Long viewerId) {
        return visibility == Visibility.PUBLIC || author.getId().equals(viewerId);
    }
}
