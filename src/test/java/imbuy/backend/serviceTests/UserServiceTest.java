package imbuy.backend.serviceTests;

import imbuy.backend.domain.User;
import imbuy.backend.dto.RegisterRequest;
import imbuy.backend.dto.UserDto;
import imbuy.backend.exception.UserNotFoundException;
import imbuy.backend.repository.UserRepository;
import imbuy.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private LocalDateTime testCreatedAt;

    @BeforeEach
    void setUp() {
        testCreatedAt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setUsername("testuser");
        testUser.setCreatedAt(testCreatedAt);
    }


    @Test
    void getAllUsers_WithEmptyList_ShouldReturnEmptyList() {
        // Given
        when(userRepository.findAll()).thenReturn(List.of());

        // When
        List<UserDto> result = userService.getAllUsers();

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_WithNonExistingUser_ShouldThrowException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findById(999L);
    }

    @Test
    void updateProfileById_WithUsernameAndPassword_ShouldUpdateBoth() {
        // Given
        RegisterRequest updateRequest = new RegisterRequest();
        updateRequest.setUsername("newusername");
        updateRequest.setPassword("newpassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserDto result = userService.updateProfileById(1L, updateRequest);

        // Then
        assertThat(result.getUsername()).isEqualTo("newusername");

        verify(userRepository).findById(1L);
        verify(passwordEncoder).encode("newpassword");
        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("newusername") &&
                        user.getPassword().equals("encodedNewPassword")
        ));
    }

    @Test
    void updateProfileById_WithOnlyUsername_ShouldUpdateOnlyUsername() {
        // Given
        RegisterRequest updateRequest = new RegisterRequest();
        updateRequest.setUsername("newusername");
        // Password is null

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserDto result = userService.updateProfileById(1L, updateRequest);

        // Then
        assertThat(result.getUsername()).isEqualTo("newusername");

        verify(userRepository).findById(1L);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("newusername") &&
                        user.getPassword().equals("encodedPassword") // Password remains unchanged
        ));
    }

    @Test
    void updateProfileById_WithOnlyPassword_ShouldUpdateOnlyPassword() {
        // Given
        RegisterRequest updateRequest = new RegisterRequest();
        updateRequest.setPassword("newpassword");
        // Username is null

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserDto result = userService.updateProfileById(1L, updateRequest);

        // Then
        assertThat(result.getUsername()).isEqualTo("testuser"); // Username remains unchanged

        verify(userRepository).findById(1L);
        verify(passwordEncoder).encode("newpassword");
        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("testuser") &&
                        user.getPassword().equals("encodedNewPassword")
        ));
    }

    @Test
    void updateProfileById_WithEmptyRequest_ShouldNotChangeAnything() {
        // Given
        RegisterRequest updateRequest = new RegisterRequest();
        // Both username and password are null

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserDto result = userService.updateProfileById(1L, updateRequest);

        // Then
        assertThat(result.getUsername()).isEqualTo("testuser");

        verify(userRepository).findById(1L);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("testuser") &&
                        user.getPassword().equals("encodedPassword")
        ));
    }

    @Test
    void updateProfileById_WithNonExistingUser_ShouldThrowException() {
        // Given
        RegisterRequest updateRequest = new RegisterRequest();
        updateRequest.setUsername("newusername");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateProfileById(999L, updateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");

        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateProfileById_ShouldEncodePassword() {
        // Given
        RegisterRequest updateRequest = new RegisterRequest();
        updateRequest.setPassword("plainPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        userService.updateProfileById(1L, updateRequest);

        // Then
        verify(passwordEncoder).encode("plainPassword");
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("encodedPassword123")
        ));
    }
}
