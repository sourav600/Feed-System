package com.newsfeed.backend.user;

import com.newsfeed.backend.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    // columnDefinition pinned to citext (matches the V1 migration exactly) - without it, Hibernate
    // infers a plain varchar for schema validation and ddl-auto=validate fails at startup on a
    // type mismatch against the actual DB column.
    @Column(nullable = false, unique = true, columnDefinition = "citext")
    private String email;

    @Column(nullable = false, length = 60)
    private String passwordHash;

    @Column(length = 500)
    private String avatarUrl;
}
