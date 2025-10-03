// src/main/java/imbuy/backend/controller/LotController.java
package imbuy.backend.controller;

import imbuy.backend.dto.*;
import imbuy.backend.service.LotService;
import imbuy.backend.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lots")
@RequiredArgsConstructor
@Tag(name = "Lots", description = "Lot management APIs")
public class LotController {

    private final LotService lotService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @Operation(summary = "Get all lots with pagination and filtering")
    public ResponseEntity<PageResponse<LotDto>> getLots(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) Boolean activeOnly) {

        LotFilterDto filter = new LotFilterDto();
        filter.setTitle(title);
        if (status != null) {
            try {
                filter.setStatus(imbuy.backend.enums.LotStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                // ignore invalid status
            }
        }
        filter.setCategoryId(categoryId);
        filter.setOwnerId(ownerId);
        filter.setActiveOnly(activeOnly);

        Pageable pageable = PageRequest.of(0, 20); // default pagination
        Long currentUserId = securityUtils.isAuthenticated() ? securityUtils.getCurrentUserId() : null;
        PageResponse<LotDto> lots = lotService.getLots(filter, pageable, currentUserId);

        return ResponseEntity.ok(lots);
    }

    @GetMapping("/with-total")
    @Operation(summary = "Get all lots with total count in header")
    public ResponseEntity<PageResponse<LotDto>> getLotsWithTotalCount(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        LotFilterDto filter = new LotFilterDto();
        filter.setTitle(title);
        if (status != null) {
            try {
                filter.setStatus(imbuy.backend.enums.LotStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                // ignore invalid status
            }
        }
        filter.setCategoryId(categoryId);
        filter.setOwnerId(ownerId);

        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        Long currentUserId = securityUtils.isAuthenticated() ? securityUtils.getCurrentUserId() : null;
        PageResponse<LotDto> lots = lotService.getLotsWithTotalCount(filter, pageable, currentUserId);

        HttpHeaders headers = new HttpHeaders();
        // В реальном приложении здесь нужно добавить общее количество в заголовок
        // headers.add("X-Total-Count", String.valueOf(lots.getTotalElements()));

        return new ResponseEntity<>(lots, headers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get lot by ID")
    public ResponseEntity<LotDto> getLotById(@PathVariable Long id) {
        Long currentUserId = securityUtils.isAuthenticated() ? securityUtils.getCurrentUserId() : null;
        LotDto lot = lotService.getLotById(id, currentUserId);
        return ResponseEntity.ok(lot);
    }

    @PostMapping
    @Operation(summary = "Create a new lot")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LotDto> createLot(@Valid @RequestBody CreateLotDto createLotDto) {
        Long currentUserId = securityUtils.getCurrentUserId();
        LotDto lot = lotService.createLot(createLotDto, currentUserId);
        return new ResponseEntity<>(lot, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update lot")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LotDto> updateLot(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLotDto updateLotDto) {
        Long currentUserId = securityUtils.getCurrentUserId();
        LotDto lot = lotService.updateLot(id, updateLotDto, currentUserId);
        return ResponseEntity.ok(lot);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete lot")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteLot(@PathVariable Long id) {
        Long currentUserId = securityUtils.getCurrentUserId();
        lotService.deleteLot(id, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/favorite")
    @Operation(summary = "Toggle favorite status for lot")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> toggleFavorite(@PathVariable Long id) {
        Long currentUserId = securityUtils.getCurrentUserId();
        lotService.toggleFavorite(id, currentUserId);
        return ResponseEntity.ok().build();
    }
}