package imbuy.backend.controller;

import imbuy.backend.dto.BidDto;
import imbuy.backend.dto.CreateBidDto;
import imbuy.backend.dto.PageResponse;
import imbuy.backend.service.BidService;
import imbuy.backend.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lots/{lotId}/bids")
@RequiredArgsConstructor
@Tag(name = "Bids", description = "Bid management APIs")
@SecurityRequirement(name = "bearerAuth")
public class BidController {

    private final BidService bidService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @Operation(summary = "Get bid history for a lot")
    public ResponseEntity<PageResponse<BidDto>> getBidsByLotId(
            @PathVariable Long lotId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        PageResponse<BidDto> bids = bidService.getBidsByLotId(lotId, pageable);
        return ResponseEntity.ok(bids);
    }

    @PostMapping
    @Operation(summary = "Place a bid on a lot")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BidDto> placeBid(
            @PathVariable Long lotId,
            @Valid @RequestBody CreateBidDto createBidDto) {

        Long bidderId = securityUtils.getCurrentUserId();
        BidDto bid = bidService.placeBid(lotId, createBidDto, bidderId);
        return new ResponseEntity<>(bid, HttpStatus.CREATED);
    }

    @GetMapping("/winning")
    @Operation(summary = "Get winning bid for a lot")
    public ResponseEntity<BidDto> getWinningBid(@PathVariable Long lotId) {
        BidDto winningBid = bidService.getWinningBid(lotId);
        return winningBid != null ? ResponseEntity.ok(winningBid) : ResponseEntity.notFound().build();
    }
}