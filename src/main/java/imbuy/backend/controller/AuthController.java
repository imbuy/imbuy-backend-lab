package imbuy.backend.controller;

import imbuy.backend.dto.LoginRequest;
import imbuy.backend.dto.PageResponse;
import imbuy.backend.dto.RegisterRequest;
import imbuy.backend.dto.UserDto;
import imbuy.backend.service.AuthService;
import imbuy.backend.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API для аутентификации и управления пользователями")
@SecurityRequirement(name = "bearerAuth")
public class AuthController {

    private final AuthService authService;
    private final SecurityUtils securityUtils;

    @PostMapping("/auth/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }


    @PostMapping("/auth/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/auth/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token.replace("Bearer ", ""));
        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/users/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        PageResponse<UserDto> users = authService.findAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(authService.findById(id));
    }

    @PutMapping("/users/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> updateProfile(@Valid @RequestBody RegisterRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(authService.updateProfile(userId, request));
    }
}