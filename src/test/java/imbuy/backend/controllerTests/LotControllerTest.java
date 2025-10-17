package imbuy.backend.controllerTests;

import imbuy.backend.controller.LotController;
import imbuy.backend.dto.*;
import imbuy.backend.enums.LotStatus;
import imbuy.backend.service.LotService;
import imbuy.backend.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LotControllerTest {

    @Mock
    private LotService lotService;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private LotController lotController;

    private LotDto lotDto;
    private CreateLotDto createLotDto;
    private UpdateLotDto updateLotDto;
    private final String token = "test-token";

    @BeforeEach
    void setUp() {
        lotDto = new LotDto();
        lotDto.setId(1L);
        lotDto.setTitle("Test Lot");
        lotDto.setDescription("Test Description");
        lotDto.setStartPrice(BigDecimal.valueOf(100));
        lotDto.setCurrentPrice(BigDecimal.valueOf(100));
        lotDto.setBidStep(BigDecimal.valueOf(10));
        lotDto.setStatus(LotStatus.ACTIVE);
        lotDto.setOwnerId(1L);

        createLotDto = new CreateLotDto();
        createLotDto.setTitle("New Lot");
        createLotDto.setDescription("New Description");
        createLotDto.setStartPrice(BigDecimal.valueOf(200));
        createLotDto.setBidStep(BigDecimal.valueOf(20));

        updateLotDto = new UpdateLotDto();
        updateLotDto.setTitle("Updated Lot");
        updateLotDto.setDescription("Updated Description");
    }

    @Test
    void getLots_WithToken_ShouldReturnLots() {
        PageResponse<LotDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(List.of(lotDto));

        when(securityUtils.getCurrentUserId(token)).thenReturn(1L);
        when(lotService.getLots(any(LotFilterDto.class), any(Pageable.class), eq(1L)))
                .thenReturn(pageResponse);

        ResponseEntity<PageResponse<LotDto>> response = lotController.getLots(
                token != null ? "Bearer " + token : null,
                "test", "ACTIVE", 1L, 1L, true
        );

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getContent().size());
        verify(lotService).getLots(any(LotFilterDto.class), any(Pageable.class), eq(1L));
    }

    @Test
    void getLotById_WithToken_ShouldReturnLot() {
        when(securityUtils.getCurrentUserId(token)).thenReturn(1L);
        when(lotService.getLotById(1L)).thenReturn(lotDto);

        ResponseEntity<LotDto> response = lotController.getLotById(1L, "Bearer " + token);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(lotDto, response.getBody());
        verify(lotService).getLotById(1L);
    }

    @Test
    void createLot_WithToken_ShouldCreateLot() {
        when(securityUtils.getCurrentUserId(token)).thenReturn(1L);
        when(lotService.createLot(createLotDto, 1L)).thenReturn(lotDto);

        ResponseEntity<LotDto> response = lotController.createLot("Bearer " + token, createLotDto);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(lotDto, response.getBody());
        verify(lotService).createLot(createLotDto, 1L);
    }

    @Test
    void approveLot_WithToken_ShouldApproveLot() {
        when(securityUtils.getCurrentUserId(token)).thenReturn(1L);
        when(lotService.approveLot(1L, 1L)).thenReturn(lotDto);

        ResponseEntity<LotDto> response = lotController.approveLot("Bearer " + token, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(lotDto, response.getBody());
        verify(lotService).approveLot(1L, 1L);
    }

    @Test
    void cancelLot_WithToken_ShouldCancelLot() {
        when(securityUtils.getCurrentUserId(token)).thenReturn(1L);
        when(lotService.cancelLot(1L, 1L, "Reason")).thenReturn(lotDto);

        ResponseEntity<LotDto> response = lotController.cancelLot("Bearer " + token, 1L, "Reason");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(lotDto, response.getBody());
        verify(lotService).cancelLot(1L, 1L, "Reason");
    }

    @Test
    void updateLot_WithToken_ShouldUpdateLot() {
        when(securityUtils.getCurrentUserId(token)).thenReturn(1L);
        when(lotService.updateLot(1L, updateLotDto, 1L)).thenReturn(lotDto);

        ResponseEntity<LotDto> response = lotController.updateLot("Bearer " + token, 1L, updateLotDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(lotDto, response.getBody());
        verify(lotService).updateLot(1L, updateLotDto, 1L);
    }

    @Test
    void deleteLot_WithToken_ShouldDeleteLot() {
        when(securityUtils.getCurrentUserId(token)).thenReturn(1L);
        doNothing().when(lotService).deleteLot(1L, 1L);

        ResponseEntity<Void> response = lotController.deleteLot("Bearer " + token, 1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(lotService).deleteLot(1L, 1L);
    }
}
