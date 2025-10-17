package imbuy.backend.utils;

import imbuy.backend.domain.User;
import imbuy.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public String getCurrentUserEmail(String token) {
        return jwtTokenProvider.getEmailFromToken(token);
    }

    public User getCurrentUser(String token) {
        String email = getCurrentUserEmail(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Long getCurrentUserId(String token) {
        return getCurrentUser(token).getId();
    }
}
