package imbuy.backend.serviceTests;

import imbuy.backend.domain.User;
import imbuy.backend.dto.PageResponse;
import imbuy.backend.dto.RegisterRequest;
import imbuy.backend.dto.UserDto;
import imbuy.backend.repository.UserRepository;
import imbuy.backend.mapper.UserMapper;
import imbuy.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;
    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("test@example.com", "password123", "testuser");

        user = User.builder()
                .email("test@example.com")
                .password("password123")
                .username("testuser")
                .id(1L)
                .build();

        userDto = new UserDto(1L, "test@example.com", "testuser");
    }

    @Test
    void register_WithNewUser_ShouldReturnUser() {
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.register(registerRequest);

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getUsername(), result.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> userService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findAllUsers_ShouldReturnPaginatedUserList() {
        List<User> users = List.of(user);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 20), 1);
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(userMapper.mapToDto(any(User.class))).thenReturn(userDto);

        PageResponse<UserDto> result = userService.findAllUsers(PageRequest.of(0, 20));

        assertNotNull(result);
        assertNotNull(result.content());
        assertEquals(1, result.content().size());
        assertEquals(user.getEmail(), result.content().get(0).email());
        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    void findById_WithExistingUser_ShouldReturnUserDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.mapToDto(user)).thenReturn(userDto);

        UserDto result = userService.findById(1L);

        assertNotNull(result);
        assertEquals(user.getEmail(), result.email());
        verify(userRepository).findById(1L);
    }

    @Test
    void findById_WithNonExistingUser_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> userService.findById(1L));
    }

    @Test
    void updateProfile_WithValidData_ShouldReturnUpdatedUserDto() {
        RegisterRequest updateRequest = new RegisterRequest(user.getEmail(), "newpassword", "newusername");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.mapToDto(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return new UserDto(u.getId(), u.getEmail(), u.getUsername());
        });

        UserDto result = userService.updateProfile(1L, updateRequest);

        assertNotNull(result);
        assertEquals("newusername", result.username());
        verify(userRepository).save(any(User.class));
    }
}
