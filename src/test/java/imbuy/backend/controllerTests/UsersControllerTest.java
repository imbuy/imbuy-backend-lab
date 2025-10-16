package imbuy.backend.controllerTests;

import imbuy.backend.controller.UsersController;
import imbuy.backend.dto.PageResponse;
import imbuy.backend.dto.RegisterRequest;
import imbuy.backend.dto.UserDto;
import imbuy.backend.service.UserService;
import imbuy.backend.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsersControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private UsersController usersController;

    private UserDto userDto;
    private RegisterRequest registerRequest;
    private PageResponse<UserDto> userPageResponse;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setEmail("test@example.com");
        userDto.setUsername("testuser");

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password");

        userPageResponse = new PageResponse<>();
        userPageResponse.setContent(List.of(userDto));
        userPageResponse.setCurrentPage(0);
        userPageResponse.setPageSize(20);
        userPageResponse.setHasNext(false);
        userPageResponse.setHasPrevious(false);
    }

    @Test
    void getAllUsers_AsAdmin_ShouldReturnPaginatedUsers() {
        when(userService.getAllUsers(any(PageRequest.class))).thenReturn(userPageResponse);

        ResponseEntity<PageResponse<UserDto>> response = usersController.getAllUsers(0, 20);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals(userDto, response.getBody().getContent().get(0));
        verify(userService).getAllUsers(any(PageRequest.class));
    }

    @Test
    void getAllUsers_WithLargeSize_ShouldLimitTo50() {
        when(userService.getAllUsers(any(PageRequest.class))).thenReturn(userPageResponse);

        ResponseEntity<PageResponse<UserDto>> response = usersController.getAllUsers(0, 100);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).getAllUsers(PageRequest.of(0, 50));
    }

    @Test
    void getUserById_WithExistingUser_ShouldReturnUser() {
        when(userService.getUserById(1L)).thenReturn(userDto);

        ResponseEntity<UserDto> response = usersController.getUserById(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userDto, response.getBody());
        verify(userService).getUserById(1L);
    }

    @Test
    void updateProfile_WithValidData_ShouldUpdateProfile() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(userService.updateProfileById(1L, registerRequest)).thenReturn(userDto);

        ResponseEntity<?> response = usersController.updateProfile(registerRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userDto, response.getBody());
        verify(userService).updateProfileById(1L, registerRequest);
    }

    @Test
    void updateProfile_WithNonExistingUser_ShouldReturnNotFound() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(userService.updateProfileById(1L, registerRequest))
                .thenThrow(new RuntimeException("User not found"));

        ResponseEntity<?> response = usersController.updateProfile(registerRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertInstanceOf(Map.class, response.getBody());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertEquals("User not found", body.get("error"));
        verify(userService).updateProfileById(1L, registerRequest);
    }
}