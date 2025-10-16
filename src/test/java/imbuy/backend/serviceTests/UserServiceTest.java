package imbuy.backend.serviceTests;

import imbuy.backend.domain.User;
import imbuy.backend.dto.PageResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    void getAllUsers_WithUsers_ShouldReturnPaginatedUsers() {
        List<User> users = List.of(testUser);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 20), 1);
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        PageResponse<UserDto> result = userService.getAllUsers(PageRequest.of(0, 20));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("test@example.com");
        assertThat(result.getCurrentPage()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(20);
        assertThat(result.isHasNext()).isFalse();
        assertThat(result.isHasPrevious()).isFalse();

        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    void getAllUsers_WithEmptyPage_ShouldReturnEmptyPage() {
        Page<User> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(userRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        PageResponse<UserDto> result = userService.getAllUsers(PageRequest.of(0, 20));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getCurrentPage()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(20);

        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    void getAllUsers_WithMultiplePages_ShouldReturnCorrectPaginationInfo() {
        List<User> users = List.of(testUser);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(1, 10), 25);
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        PageResponse<UserDto> result = userService.getAllUsers(PageRequest.of(1, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getCurrentPage()).isEqualTo(1);
        assertThat(result.getPageSize()).isEqualTo(10);
        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    void getUserById_WithExistingUser_ShouldReturnUserDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserDto result = userService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getUsername()).isEqualTo("testuser");

        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_WithNonExistingUser_ShouldThrowException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findById(999L);
    }

    @Test
    void updateProfileById_WithUsernameAndPassword_ShouldUpdateBoth() {
        RegisterRequest updateRequest = new RegisterRequest();
        updateRequest.setUsername("newusername");
        updateRequest.setPassword("newpassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateProfileById(1L, updateRequest);

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
        RegisterRequest updateRequest = new RegisterRequest();
        updateRequest.setUsername("newusername");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateProfileById(1L, updateRequest);

        assertThat(result.getUsername()).isEqualTo("newusername");

        verify(userRepository).findById(1L);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("newusername") &&
                        user.getPassword().equals("encodedPassword")
        ));
    }

    @Test
    void updateProfileById_WithOnlyPassword_ShouldUpdateOnlyPassword() {
        RegisterRequest updateRequest = new RegisterRequest();
        updateRequest.setPassword("newpassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateProfileById(1L, updateRequest);

        assertThat(result.getUsername()).isEqualTo("testuser");

        verify(userRepository).findById(1L);
        verify(passwordEncoder).encode("newpassword");
        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("testuser") &&
                        user.getPassword().equals("encodedNewPassword")
        ));
    }

    @Test
    void updateProfileById_WithEmptyRequest_ShouldNotChangeAnything() {
        RegisterRequest updateRequest = new RegisterRequest();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateProfileById(1L, updateRequest);

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
        RegisterRequest updateRequest = new RegisterRequest();
        updateRequest.setUsername("newusername");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfileById(999L, updateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");

        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateProfileById_ShouldEncodePassword() {
        RegisterRequest updateRequest = new RegisterRequest();
        updateRequest.setPassword("plainPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.updateProfileById(1L, updateRequest);

        verify(passwordEncoder).encode("plainPassword");
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("encodedPassword123")
        ));
    }
}