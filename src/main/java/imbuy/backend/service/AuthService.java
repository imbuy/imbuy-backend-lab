package imbuy.backend.service;

import imbuy.backend.domain.User;
import imbuy.backend.dto.PageResponse;
import imbuy.backend.dto.RegisterRequest;
import imbuy.backend.dto.UserDto;
import imbuy.backend.mapper.UserMapper;
import imbuy.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = User.builder()
                .email(request.email())
                .password(request.password())
                .username(request.username())
                .build();

        userRepository.save(user);
        return user;
    }

    public UserDto updateProfile(Long userId, RegisterRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        User updatedUser = user.toBuilder()
                .username(request.username())
                .password(request.password() != null && !request.password().isEmpty()
                        ? request.password()
                        : user.getPassword())
                .build();

        userRepository.save(updatedUser);
        return userMapper.mapToDto(updatedUser);
    }

    public PageResponse<UserDto> findAllUsers(Pageable pageable) {
        return PageResponse.of(userRepository.findAll(pageable).map(userMapper::mapToDto));
    }

    public UserDto findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return userMapper.mapToDto(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
