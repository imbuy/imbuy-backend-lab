package imbuy.backend.controller;

import imbuy.backend.dto.PageResponse;
import imbuy.backend.dto.RegisterRequest;
import imbuy.backend.dto.UserDto;
import imbuy.backend.service.UserService;
import imbuy.backend.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {

    private final UserService userService;
    private final SecurityUtils securityUtils;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<PageResponse<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        PageResponse<UserDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @Valid @RequestBody RegisterRequest request) {
        try {
            Long userId = securityUtils.getCurrentUserId();
            UserDto updated = userService.updateProfileById(userId, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", ex.getMessage()));
        }
    }
}
