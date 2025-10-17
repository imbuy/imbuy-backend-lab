package imbuy.backend.controllerTests;

import imbuy.backend.controller.BidController;
import imbuy.backend.dto.BidDto;
import imbuy.backend.dto.CreateBidDto;
import imbuy.backend.dto.PageResponse;
import imbuy.backend.service.BidService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BidControllerTest {

    @Mock
    private BidService bidService;

    @InjectMocks
    private BidController bidController;

    private BidDto bidDto;
    private CreateBidDto createBidDto;

    @BeforeEach
    void setUp() {
        bidDto = new BidDto();
        bidDto.setId(1L);
        bidDto.setAmount(BigDecimal.valueOf(100));
        bidDto.setBidderId(1L);
        bidDto.setBidderUsername("testuser");
        bidDto.setCreatedAt(LocalDateTime.now());

        createBidDto = new CreateBidDto();
        createBidDto.setAmount(BigDecimal.valueOf(100));
    }

    @Test
    void getBidsByLotId_WithValidParameters_ShouldReturnBids() {
        PageResponse<BidDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(List.of(bidDto));

        when(bidService.getBidsByLotId(1L, PageRequest.of(0, 20))).thenReturn(pageResponse);

        ResponseEntity<PageResponse<BidDto>> response = bidController.getBidsByLotId(1L, 0, 20);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        verify(bidService).getBidsByLotId(1L, PageRequest.of(0, 20));
    }

    @Test
    void placeBid_WithValidData_ShouldCreateBid() {
        when(bidService.placeBid(1L, createBidDto, 1L)).thenReturn(bidDto);

        ResponseEntity<BidDto> response = bidController.placeBid(1L, String.valueOf(1L), createBidDto);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(bidDto, response.getBody());
        verify(bidService).placeBid(1L, createBidDto, 1L);
    }

    @Test
    void getWinningBid_WithExistingWinningBid_ShouldReturnBid() {
        when(bidService.getWinningBid(1L)).thenReturn(bidDto);

        ResponseEntity<BidDto> response = bidController.getWinningBid(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(bidDto, response.getBody());
        verify(bidService).getWinningBid(1L);
    }

    @Test
    void getWinningBid_WithNoWinningBid_ShouldReturnNotFound() {
        when(bidService.getWinningBid(1L)).thenReturn(null);

        ResponseEntity<BidDto> response = bidController.getWinningBid(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(bidService).getWinningBid(1L);
    }
}
