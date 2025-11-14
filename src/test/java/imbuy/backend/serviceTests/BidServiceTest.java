package imbuy.backend.serviceTests;

import imbuy.backend.domain.Bid;
import imbuy.backend.domain.Lot;
import imbuy.backend.domain.User;
import imbuy.backend.dto.BidDto;
import imbuy.backend.enums.LotStatus;
import imbuy.backend.mapper.BidMapper;
import imbuy.backend.repository.BidRepository;
import imbuy.backend.service.BidService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidServiceTest {

    @Mock
    private BidRepository bidRepository;

    @Mock
    private BidMapper bidMapper;

    @InjectMocks
    private BidService bidService;

    private Lot activeLot;
    private User bidder;
    private User owner;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .email("owner@example.com")
                .password("password")
                .username("owner")
                .id(1L)
                .build();

        bidder = User.builder()
                .email("bidder@example.com")
                .password("password")
                .username("bidder")
                .id(2L)
                .build();

        activeLot = Lot.builder()
                .id(1L)
                .title("Test Lot")
                .startPrice(BigDecimal.valueOf(100))
                .currentPrice(BigDecimal.valueOf(100))
                .bidStep(BigDecimal.valueOf(10))
                .status(LotStatus.ACTIVE)
                .owner(owner)
                .startDate(LocalDateTime.now().minusHours(1))
                .endDate(LocalDateTime.now().plusHours(1))
                .build();
    }

    @Test
    void getWinningBid_WithExistingBids_ShouldReturnHighestBid() {
        Bid winningBid = Bid.builder()
                .id(1L)
                .lot(activeLot)
                .bidder(bidder)
                .amount(BigDecimal.valueOf(150))
                .build();

        BidDto winningBidDto = new BidDto(
                1L,
                BigDecimal.valueOf(150),
                2L,
                "bidder"
        );

        when(bidRepository.findTopByLotIdOrderByAmountDesc(1L)).thenReturn(Optional.of(winningBid));
        when(bidMapper.mapToDto(winningBid)).thenReturn(winningBidDto);

        BidDto result = bidService.getWinningBid(1L);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(150), result.amount());
        assertEquals(2L, result.bidder_id());

        verify(bidRepository).findTopByLotIdOrderByAmountDesc(1L);
        verify(bidMapper).mapToDto(winningBid);
    }

    @Test
    void getWinningBid_WithNoBids_ShouldThrowNotFound() {
        when(bidRepository.findTopByLotIdOrderByAmountDesc(1L))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> bidService.getWinningBid(1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(bidRepository).findTopByLotIdOrderByAmountDesc(1L);
        verifyNoInteractions(bidMapper);
    }
}