package com.newsfeed.backend.user;

import com.newsfeed.backend.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /** Returns null (not Optional/exception) - callers like login intentionally treat "no such user" as data, not an error. */
    public User findByEmailOrNull(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Transactional
    public User create(User user) {
        return userRepository.save(user);
    }
}
