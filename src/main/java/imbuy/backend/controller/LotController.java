package imbuy.backend.controller;

import imbuy.backend.dto.*;
import imbuy.backend.service.LotService;
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

    @GetMapping
    @Operation(summary = "Get all lots with pagination and filtering")
    public ResponseEntity<PageResponse<LotDto>> getLots(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Boolean activeOnly) {

        LotFilterDto filter = new LotFilterDto(
                title,
                activeOnly != null ? activeOnly : false
        );

        Pageable pageable = PageRequest.of(0, 20);
        PageResponse<LotDto> lots = lotService.getLots(filter, pageable);
        return ResponseEntity.ok(lots);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get lot by ID")
    public ResponseEntity<LotDto> getLotById(@PathVariable Long id) {
        LotDto lot = lotService.getLotById(id);
        return ResponseEntity.ok(lot);
    }

    @PostMapping
    @Operation(summary = "Create a new lot")
    public ResponseEntity<LotDto> createLot(@Valid @RequestBody CreateLotDto createLotDto,
                                            @RequestParam Long currentUserId) {
        LotDto lot = lotService.createLot(createLotDto, currentUserId);
        return new ResponseEntity<>(lot, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve lot")
    public ResponseEntity<LotDto> approveLot(@PathVariable Long id,
                                             @RequestParam Long currentUserId) {
        LotDto approvedLot = lotService.approveLot(id, currentUserId);
        return ResponseEntity.ok(approvedLot);
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel lot")
    public ResponseEntity<LotDto> cancelLot(@PathVariable Long id,
                                            @RequestParam Long currentUserId) {
        LotDto cancelledLot = lotService.cancelLot(id, currentUserId);
        return ResponseEntity.ok(cancelledLot);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update lot")
    public ResponseEntity<LotDto> updateLot(@PathVariable Long id,
                                            @RequestParam Long currentUserId,
                                            @Valid @RequestBody UpdateLotDto updateLotDto) {
        LotDto lot = lotService.updateLot(id, updateLotDto, currentUserId);
        return ResponseEntity.ok(lot);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete lot")
    public ResponseEntity<Void> deleteLot(@PathVariable Long id,
                                          @RequestParam Long currentUserId) {
        lotService.deleteLot(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
