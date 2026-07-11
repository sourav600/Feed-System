package com.newsfeed.backend.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    // email column is CITEXT (case-insensitive) at the DB level, so a plain equality lookup
    // already matches case-insensitively - no need for a LOWER()/ignoreCase query here.
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
