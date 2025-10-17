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