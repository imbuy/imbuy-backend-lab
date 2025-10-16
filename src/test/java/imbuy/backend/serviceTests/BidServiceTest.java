package imbuy.backend.serviceTests;

import imbuy.backend.domain.Bid;
import imbuy.backend.domain.Lot;
import imbuy.backend.domain.User;
import imbuy.backend.dto.BidDto;
import imbuy.backend.dto.CreateBidDto;
import imbuy.backend.dto.PageResponse;
import imbuy.backend.enums.LotStatus;
import imbuy.backend.repository.BidRepository;
import imbuy.backend.repository.LotRepository;
import imbuy.backend.repository.UserRepository;
import imbuy.backend.service.BidService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BidServiceTest {

    @Mock
    private BidRepository bidRepository;

    @Mock
    private LotRepository lotRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BidService bidService;

    private Lot activeLot;
    private User bidder;
    private User owner;
    private CreateBidDto createBidDto;

    @BeforeEach
    void setUp() {
        owner = new User("owner@example.com", "password", "owner");
        owner.setId(1L);

        bidder = new User("bidder@example.com", "password", "bidder");
        bidder.setId(2L);

        activeLot = new Lot();
        activeLot.setId(1L);
        activeLot.setTitle("Test Lot");
        activeLot.setStartPrice(BigDecimal.valueOf(100));
        activeLot.setCurrentPrice(BigDecimal.valueOf(100));
        activeLot.setBidStep(BigDecimal.valueOf(10));
        activeLot.setStatus(LotStatus.ACTIVE);
        activeLot.setOwner(owner);
        activeLot.setStartDate(LocalDateTime.now().minusHours(1));
        activeLot.setEndDate(LocalDateTime.now().plusHours(1));

        createBidDto = new CreateBidDto();
        createBidDto.setAmount(BigDecimal.valueOf(110));
    }

    @Test
    void getBidsByLotId_WithExistingLot_ShouldReturnBids() {
        Long lotId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        Bid bid = new Bid(activeLot, bidder, BigDecimal.valueOf(110));
        bid.setId(1L);
        Page<Bid> bidPage = new PageImpl<>(List.of(bid));

        when(lotRepository.existsById(lotId)).thenReturn(true);
        when(bidRepository.findByLotIdOrderByCreatedAtDesc(lotId, pageable)).thenReturn(bidPage);

        PageResponse<BidDto> result = bidService.getBidsByLotId(lotId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(BigDecimal.valueOf(110), result.getContent().get(0).getAmount());
    }

    @Test
    void getBidsByLotId_WithNonExistingLot_ShouldThrowException() {
        Long lotId = 999L;
        Pageable pageable = PageRequest.of(0, 10);

        when(lotRepository.existsById(lotId)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> bidService.getBidsByLotId(lotId, pageable));
    }

    @Test
    void placeBid_WithValidBid_ShouldCreateBid() {
        when(lotRepository.findById(1L)).thenReturn(Optional.of(activeLot));
        when(userRepository.findById(2L)).thenReturn(Optional.of(bidder));
        when(bidRepository.save(any(Bid.class))).thenAnswer(invocation -> {
            Bid bid = invocation.getArgument(0);
            bid.setId(1L);
            return bid;
        });
        when(lotRepository.save(any(Lot.class))).thenReturn(activeLot);

        BidDto result = bidService.placeBid(1L, createBidDto, 2L);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(110), result.getAmount());
        verify(bidRepository).save(any(Bid.class));
        verify(lotRepository).save(activeLot);
    }

    @Test
    void placeBid_OnOwnLot_ShouldThrowException() {
        when(lotRepository.findById(1L)).thenReturn(Optional.of(activeLot));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        assertThrows(RuntimeException.class, () -> bidService.placeBid(1L, createBidDto, 1L));
    }

    @Test
    void placeBid_WithInsufficientAmount_ShouldThrowException() {
        createBidDto.setAmount(BigDecimal.valueOf(105));

        when(lotRepository.findById(1L)).thenReturn(Optional.of(activeLot));
        when(userRepository.findById(2L)).thenReturn(Optional.of(bidder));

        assertThrows(RuntimeException.class, () -> bidService.placeBid(1L, createBidDto, 2L));
    }

    @Test
    void placeBid_OnInactiveLot_ShouldThrowException() {
        activeLot.setStatus(LotStatus.COMPLETED);

        when(lotRepository.findById(1L)).thenReturn(Optional.of(activeLot));
        when(userRepository.findById(2L)).thenReturn(Optional.of(bidder));

        assertThrows(RuntimeException.class, () -> bidService.placeBid(1L, createBidDto, 2L));
    }

    @Test
    void getWinningBid_WithExistingBids_ShouldReturnHighestBid() {
        Bid winningBid = new Bid(activeLot, bidder, BigDecimal.valueOf(150));
        winningBid.setId(1L);

        when(bidRepository.findTopByLotIdOrderByAmountDesc(1L)).thenReturn(Optional.of(winningBid));

        BidDto result = bidService.getWinningBid(1L);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(150), result.getAmount());
    }

    @Test
    void getWinningBid_WithNoBids_ShouldReturnNull() {
        when(bidRepository.findTopByLotIdOrderByAmountDesc(1L)).thenReturn(Optional.empty());

        BidDto result = bidService.getWinningBid(1L);

        assertNull(result);
    }
}