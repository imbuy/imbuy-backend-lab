package imbuy.backend.service;

import imbuy.backend.domain.Bid;
import imbuy.backend.domain.Lot;
import imbuy.backend.domain.User;
import imbuy.backend.dto.BidDto;
import imbuy.backend.dto.CreateBidDto;
import imbuy.backend.dto.PageResponse;
import imbuy.backend.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
@Slf4j
@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepository bidRepository;
    private final LotService lotService;
    private final AuthService userService;

    @Transactional(readOnly = true)
    public PageResponse<BidDto> getBidsByLotId(Long lotId, Pageable pageable) {
        Lot lot = lotService.getLotEntityById(lotId);
        Page<Bid> bids = bidRepository.findByLotIdOrderByCreatedAtDesc(lot.getId(), pageable);
        return PageResponse.of(bids.map(this::mapToDto));
    }

    @Transactional
    public BidDto placeBid(Long lotId, CreateBidDto createBidDto, Long bidderId) {
        Lot lot = lotService.getLotEntityById(lotId);
        User bidder = userService.getUserById(bidderId);

        validateBid(lot, createBidDto.getAmount(), bidderId);

        Bid bid = new Bid(lot, bidder, createBidDto.getAmount());
        bid = bidRepository.save(bid);

        lot.setCurrentPrice(createBidDto.getAmount());
        lotService.updateLotCurrentPrice(lot);

        log.info("User #{} ({}) placed bid {} on lot #{} ('{}')",
                bidder.getId(), bidder.getUsername(), createBidDto.getAmount(), lot.getId(), lot.getTitle());

        return mapToDto(bid);
    }

    private void validateBid(Lot lot, BigDecimal amount, Long bidderId) {
        LocalDateTime now = LocalDateTime.now();

        if (lot.getStatus() != imbuy.backend.enums.LotStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lot is not active");
        }

        if (lot.getEndDate() != null && lot.getEndDate().isBefore(now)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lot has already ended");
        }

        if (lot.getOwner().getId().equals(bidderId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot bid on your own lot");
        }

        BigDecimal minBid = lot.getCurrentPrice().add(lot.getBidStep());
        if (amount.compareTo(minBid) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Bid must be at least %.2f", minBid));
        }
    }

    @Transactional(readOnly = true)
    public BidDto getWinningBid(Long lotId) {
        return bidRepository.findTopByLotIdOrderByAmountDesc(lotId)
                .map(this::mapToDto)
                .orElse(null);
    }

    private BidDto mapToDto(Bid bid) {
        BidDto dto = new BidDto();
        dto.setId(bid.getId());
        dto.setAmount(bid.getAmount());
        dto.setBidderId(bid.getBidder().getId());
        dto.setBidderUsername(bid.getBidder().getUsername());
        dto.setCreatedAt(bid.getCreatedAt());
        return dto;
    }

    public int countBidsByLot(Long lotId) {
        return Math.toIntExact(bidRepository.countByLotId(lotId));
    }

    public Optional<Bid> getHighestBidByLot(Long lotId) {
        return bidRepository.findTopByLotIdOrderByAmountDesc(lotId);
    }
}

