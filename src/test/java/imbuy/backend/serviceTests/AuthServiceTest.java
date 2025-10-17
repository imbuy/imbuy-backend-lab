package imbuy.backend.serviceTests;

import imbuy.backend.domain.User;
import imbuy.backend.dto.LoginRequest;
import imbuy.backend.dto.PageResponse;
import imbuy.backend.dto.RegisterRequest;
import imbuy.backend.dto.UserDto;
import imbuy.backend.exception.UserNotFoundException;
import imbuy.backend.repository.UserRepository;
import imbuy.backend.service.AuthService;
import imbuy.backend.service.TokenBlacklistService;
import imbuy.backend.utils.JwtTokenProvider;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setUsername("testuser");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        user = new User("test@example.com", "password123", "testuser"); // пароль без шифрования
        user.setId(1L);
    }

    @Test
    void register_WithNewUser_ShouldReturnUserDto() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = authService.register(registerRequest);

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getUsername(), result.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_WithValidCredentials_ShouldReturnToken() {
        String expectedToken = "jwt.token.here";

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(user.getEmail())).thenReturn(expectedToken);

        String result = authService.login(loginRequest);

        assertEquals(expectedToken, result);
        verify(jwtTokenProvider).generateToken(user.getEmail());
    }

    @Test
    void login_WithInvalidEmail_ShouldThrowException() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        verify(jwtTokenProvider, never()).generateToken(anyString());
    }

    @Test
    void login_WithInvalidPassword_ShouldThrowException() {
        user.setPassword("otherpassword"); // неверный пароль
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        verify(jwtTokenProvider, never()).generateToken(anyString());
    }

    @Test
    void logout_ShouldBlacklistToken() {
        String token = "token.to.blacklist";

        authService.logout(token);

        verify(tokenBlacklistService).blacklistToken(token);
    }

    @Test
    void findAllUsers_ShouldReturnPaginatedUserList() {
        List<User> users = List.of(user);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 20), 1);
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        PageResponse<UserDto> result = authService.findAllUsers(PageRequest.of(0, 20));

        assertNotNull(result);
        assertNotNull(result.getContent());
        assertEquals(1, result.getContent().size());
        assertEquals(user.getEmail(), result.getContent().get(0).getEmail());
        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    void findById_WithExistingUser_ShouldReturnUserDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = authService.findById(1L);

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    void findById_WithNonExistingUser_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.findById(1L));
    }

    @Test
    void updateProfile_WithValidData_ShouldReturnUpdatedUser() {
        RegisterRequest updateRequest = new RegisterRequest();
        updateRequest.setUsername("newusername");
        updateRequest.setPassword("newpassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserDto result = authService.updateProfile(1L, updateRequest);

        assertNotNull(result);
        assertEquals("newusername", user.getUsername());
        assertEquals("newpassword", user.getPassword());
        verify(userRepository).save(user);
    }
}
