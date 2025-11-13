package imbuy.backend.controllerTests;

import imbuy.backend.controller.LotController;
import imbuy.backend.dto.CreateLotDto;
import imbuy.backend.dto.LotDto;
import imbuy.backend.dto.PageResponse;
import imbuy.backend.dto.UpdateLotDto;
import imbuy.backend.enums.LotStatus;
import imbuy.backend.service.LotService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LotControllerTest {

    @Mock
    private LotService lotService;

    @InjectMocks
    private LotController lotController;

    private LotDto lotDto;
    private PageResponse<LotDto> lotPageResponse;

    @BeforeEach
    void setUp() {
        lotDto = new LotDto(
                1L,                      // id
                "Test Lot",              // title
                "Description",           // description
                BigDecimal.valueOf(100), // start_price
                BigDecimal.valueOf(110), // current_price
                BigDecimal.valueOf(10),  // bid_step
                1L,                      // owner_id
                "testuser",              // owner_username
                2L,                      // category_id
                "Electronics",           // category_name
                LotStatus.ACTIVE,        // status
                LocalDateTime.now(),     // start_date
                LocalDateTime.now().plusDays(1),
                null,                     // winner_id
                null                      // winner_username
        );

        lotPageResponse = new PageResponse<>(List.of(lotDto), 0, 20, false, false);
    }

    @Test
    void getLots_ShouldReturnPaginatedLots() {
        when(lotService.getLots(any(), any(PageRequest.class))).thenReturn(lotPageResponse);

        ResponseEntity<PageResponse<LotDto>> response = lotController.getLots(null, null);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(lotPageResponse, response.getBody());
        verify(lotService).getLots(any(), any(PageRequest.class));
    }

    @Test
    void getLotById_ShouldReturnLot() {
        when(lotService.getLotById(1L)).thenReturn(lotDto);

        ResponseEntity<LotDto> response = lotController.getLotById(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(lotDto, response.getBody());
        verify(lotService).getLotById(1L);
    }

    @Test
    void createLot_ShouldReturnCreatedLot() {
        CreateLotDto createLotDto = new CreateLotDto("New Lot", "Desc", BigDecimal.valueOf(100),
                BigDecimal.valueOf(100), 1L, LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        when(lotService.createLot(any(CreateLotDto.class), any(Long.class))).thenReturn(lotDto);

        ResponseEntity<LotDto> response = lotController.createLot(createLotDto, 1L);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(lotDto, response.getBody());
        verify(lotService).createLot(any(CreateLotDto.class), any(Long.class));
    }

    @Test
    void approveLot_ShouldReturnApprovedLot() {
        when(lotService.approveLot(1L, 1L)).thenReturn(lotDto);

        ResponseEntity<LotDto> response = lotController.approveLot(1L, 1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(lotDto, response.getBody());
        verify(lotService).approveLot(1L, 1L);
    }

    @Test
    void cancelLot_ShouldReturnCancelledLot() {
        when(lotService.cancelLot(1L, 1L)).thenReturn(lotDto);

        ResponseEntity<LotDto> response = lotController.cancelLot(1L, 1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(lotDto, response.getBody());
        verify(lotService).cancelLot(1L, 1L);
    }

    @Test
    void updateLot_ShouldReturnUpdatedLot() {
        UpdateLotDto updateLotDto = new UpdateLotDto("Updated Lot", "Updated Desc", null, null, null);
        when(lotService.updateLot(1L, updateLotDto, 1L)).thenReturn(lotDto);

        ResponseEntity<LotDto> response = lotController.updateLot(1L, 1L, updateLotDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(lotDto, response.getBody());
        verify(lotService).updateLot(1L, updateLotDto, 1L);
    }

    @Test
    void deleteLot_ShouldReturnNoContent() {
        doNothing().when(lotService).deleteLot(1L, 1L);

        ResponseEntity<Void> response = lotController.deleteLot(1L, 1L);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(lotService).deleteLot(1L, 1L);
    }
}
