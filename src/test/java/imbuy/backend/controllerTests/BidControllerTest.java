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

import static org.assertj.core.api.Assertions.assertThat;
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
        bidDto = new BidDto(
                1L,
                BigDecimal.valueOf(100),
                1L,
                "testuser",
                LocalDateTime.now()
        );

        createBidDto = new CreateBidDto(BigDecimal.valueOf(100),1L);
    }

    @Test
    void getBidsByLotId_WithValidParameters_ShouldReturnBids() {
        PageResponse<BidDto> pageResponse = new PageResponse<>(List.of(bidDto), 0, 20, false, false);

        when(bidService.getBidsByLotId(1L, PageRequest.of(0, 20))).thenReturn(pageResponse);

        ResponseEntity<PageResponse<BidDto>> response = bidController.getBidsByLotId(1L, 0, 20);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().content()).hasSize(1);
        verify(bidService).getBidsByLotId(1L, PageRequest.of(0, 20));
    }

    @Test
    void placeBid_WithValidData_ShouldCreateBid() {
        when(bidService.placeBid(1L, createBidDto)).thenReturn(bidDto);

        ResponseEntity<BidDto> response = bidController.placeBid(1L, createBidDto);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(bidDto);
        verify(bidService).placeBid(1L, createBidDto);
    }

    @Test
    void getWinningBid_WithExistingWinningBid_ShouldReturnBid() {
        when(bidService.getWinningBid(1L)).thenReturn(bidDto);

        ResponseEntity<BidDto> response = bidController.getWinningBid(1L);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(bidDto);
        verify(bidService).getWinningBid(1L);
    }

    @Test
    void getWinningBid_WithNoWinningBid_ShouldReturnNotFound() {
        when(bidService.getWinningBid(1L)).thenReturn(null);

        ResponseEntity<BidDto> response = bidController.getWinningBid(1L);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(bidService).getWinningBid(1L);
    }
}
