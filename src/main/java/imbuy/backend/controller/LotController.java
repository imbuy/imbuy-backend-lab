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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @RequestHeader(value = "Authorization", required = false) String authHeader,
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
            }
        }
        filter.setCategoryId(categoryId);
        filter.setOwnerId(ownerId);
        filter.setActiveOnly(activeOnly);

        Pageable pageable = PageRequest.of(0, 20);
        Long currentUserId = null;
        if (authHeader != null) {
            String token = authHeader.replace("Bearer ", "");
            currentUserId = securityUtils.getCurrentUserId(token);
        }

        PageResponse<LotDto> lots = lotService.getLots(filter, pageable, currentUserId);
        return ResponseEntity.ok(lots);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get lot by ID")
    public ResponseEntity<LotDto> getLotById(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        LotDto lot = lotService.getLotById(id);
        return ResponseEntity.ok(lot);
    }

    @PostMapping
    @Operation(summary = "Create a new lot")
    public ResponseEntity<LotDto> createLot(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CreateLotDto createLotDto) {

        String token = authHeader.replace("Bearer ", "");
        Long currentUserId = securityUtils.getCurrentUserId(token);
        LotDto lot = lotService.createLot(createLotDto, currentUserId);
        return new ResponseEntity<>(lot, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve lot (Admin only)")
    public ResponseEntity<LotDto> approveLot(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {

        String token = authHeader.replace("Bearer ", "");
        Long adminId = securityUtils.getCurrentUserId(token);
        LotDto approvedLot = lotService.approveLot(id, adminId);
        return ResponseEntity.ok(approvedLot);
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel lot (Admin only)")
    public ResponseEntity<LotDto> cancelLot(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {

        String token = authHeader.replace("Bearer ", "");
        Long adminId = securityUtils.getCurrentUserId(token);
        LotDto cancelledLot = lotService.cancelLot(id, adminId, reason);
        return ResponseEntity.ok(cancelledLot);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update lot")
    public ResponseEntity<LotDto> updateLot(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody UpdateLotDto updateLotDto) {

        String token = authHeader.replace("Bearer ", "");
        Long currentUserId = securityUtils.getCurrentUserId(token);
        LotDto lot = lotService.updateLot(id, updateLotDto, currentUserId);
        return ResponseEntity.ok(lot);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete lot")
    public ResponseEntity<Void> deleteLot(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {

        String token = authHeader.replace("Bearer ", "");
        Long currentUserId = securityUtils.getCurrentUserId(token);
        lotService.deleteLot(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
