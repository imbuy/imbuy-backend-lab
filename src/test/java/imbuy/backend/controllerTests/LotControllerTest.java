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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    void getLots_WithAllParameters_ShouldReturnLots() {
        PageResponse<LotDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(List.of(lotDto));

        when(securityUtils.isAuthenticated()).thenReturn(true);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(lotService.getLots(any(LotFilterDto.class), any(Pageable.class), eq(1L)))
                .thenReturn(pageResponse);

        ResponseEntity<PageResponse<LotDto>> response = lotController.getLots(
                "test", "ACTIVE", 1L, 1L, true);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        verify(lotService).getLots(any(LotFilterDto.class), any(Pageable.class), eq(1L));
    }

    @Test
    void getLots_WithInvalidStatus_ShouldIgnoreStatus() {
        PageResponse<LotDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(List.of(lotDto));

        when(securityUtils.isAuthenticated()).thenReturn(false);
        when(lotService.getLots(any(LotFilterDto.class), any(Pageable.class), eq(null)))
                .thenReturn(pageResponse);

        ResponseEntity<PageResponse<LotDto>> response = lotController.getLots(
                "test", "INVALID_STATUS", 1L, 1L, null);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(lotService).getLots(any(LotFilterDto.class), any(Pageable.class), eq(null));
    }

    @Test
    void getLotsWithTotalCount_WithValidParameters_ShouldReturnLots() {
        PageResponse<LotDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(List.of(lotDto));

        when(securityUtils.isAuthenticated()).thenReturn(true);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(lotService.getLotsWithTotalCount(any(LotFilterDto.class), any(Pageable.class), eq(1L)))
                .thenReturn(pageResponse);

        ResponseEntity<PageResponse<LotDto>> response = lotController.getLotsWithTotalCount(
                "test", "ACTIVE", 1L, 1L, 0, 20);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        verify(lotService).getLotsWithTotalCount(any(LotFilterDto.class), any(Pageable.class), eq(1L));
    }

    @Test
    void getLotsWithTotalCount_WithLargeSize_ShouldLimitTo50() {
        PageResponse<LotDto> pageResponse = new PageResponse<>();

        when(securityUtils.isAuthenticated()).thenReturn(false);
        when(lotService.getLotsWithTotalCount(any(LotFilterDto.class), any(Pageable.class), eq(null)))
                .thenReturn(pageResponse);

        ResponseEntity<PageResponse<LotDto>> response = lotController.getLotsWithTotalCount(
                null, null, null, null, 0, 100);

        assertNotNull(response);
        verify(lotService).getLotsWithTotalCount(any(LotFilterDto.class),
                argThat(pageable -> pageable.getPageSize() == 50), eq(null));
    }

    @Test
    void getLotById_WithExistingLot_ShouldReturnLot() {
        when(securityUtils.isAuthenticated()).thenReturn(true);
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(lotService.getLotById(1L, 1L)).thenReturn(lotDto);

        ResponseEntity<LotDto> response = lotController.getLotById(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(lotDto, response.getBody());
        verify(lotService).getLotById(1L, 1L);
    }

    @Test
    void getLotById_WithUnauthenticatedUser_ShouldReturnLot() {
        when(securityUtils.isAuthenticated()).thenReturn(false);
        when(lotService.getLotById(1L, null)).thenReturn(lotDto);

        ResponseEntity<LotDto> response = lotController.getLotById(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(lotService).getLotById(1L, null);
    }

    @Test
    void createLot_WithValidData_ShouldCreateLot() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(lotService.createLot(createLotDto, 1L)).thenReturn(lotDto);

        ResponseEntity<LotDto> response = lotController.createLot(createLotDto);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(lotDto, response.getBody());
        verify(lotService).createLot(createLotDto, 1L);
    }

    @Test
    void approveLot_AsAdmin_ShouldApproveLot() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(lotService.approveLot(1L, 1L)).thenReturn(lotDto);

        ResponseEntity<LotDto> response = lotController.approveLot(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(lotDto, response.getBody());
        verify(lotService).approveLot(1L, 1L);
    }

    @Test
    void cancelLot_AsAdmin_ShouldCancelLot() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(lotService.cancelLot(1L, 1L, "Invalid data")).thenReturn(lotDto);

        ResponseEntity<LotDto> response = lotController.cancelLot(1L, "Invalid data");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(lotDto, response.getBody());
        verify(lotService).cancelLot(1L, 1L, "Invalid data");
    }

    @Test
    void cancelLot_WithoutReason_ShouldCancelLot() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(lotService.cancelLot(1L, 1L, null)).thenReturn(lotDto);

        ResponseEntity<LotDto> response = lotController.cancelLot(1L, null);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(lotService).cancelLot(1L, 1L, null);
    }

    @Test
    void updateLot_AsOwner_ShouldUpdateLot() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(lotService.updateLot(1L, updateLotDto, 1L)).thenReturn(lotDto);

        ResponseEntity<LotDto> response = lotController.updateLot(1L, updateLotDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(lotDto, response.getBody());
        verify(lotService).updateLot(1L, updateLotDto, 1L);
    }

    @Test
    void deleteLot_AsOwner_ShouldDeleteLot() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        doNothing().when(lotService).deleteLot(1L, 1L);

        ResponseEntity<Void> response = lotController.deleteLot(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(lotService).deleteLot(1L, 1L);
    }

    @Test
    void toggleFavorite_AsUser_ShouldToggleFavorite() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        doNothing().when(lotService).toggleFavorite(1L, 1L);

        ResponseEntity<Void> response = lotController.toggleFavorite(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(lotService).toggleFavorite(1L, 1L);
    }
}