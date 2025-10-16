package imbuy.backend.service;

import imbuy.backend.domain.User;
import imbuy.backend.dto.PageResponse;
import imbuy.backend.dto.RegisterRequest;
import imbuy.backend.dto.UserDto;
import imbuy.backend.exception.UserNotFoundException;
import imbuy.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PageResponse<UserDto> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return PageResponse.of(users.map(this::mapToDto));
    }

    public UserDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public UserDto updateProfileById(Long userId, RegisterRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getPassword() != null)
            user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);
        return mapToDto(user);
    }

    private UserDto mapToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setCreatedAt(user.getCreatedAt().toString());
        return dto;
    }
}
