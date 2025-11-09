package imbuy.backend.serviceTests;

import imbuy.backend.domain.*;
import imbuy.backend.dto.UpdateLotDto;
import imbuy.backend.enums.LotStatus;
import imbuy.backend.repository.*;
import imbuy.backend.service.LotService;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LotServiceTest {

    @Mock private LotRepository lotRepository;

    @InjectMocks private LotService lotService;

    private User testUser;
    private Lot testLot;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testLot = new Lot();
        testLot.setId(1L);
        testLot.setTitle("Test Lot");
        testLot.setStartPrice(BigDecimal.valueOf(100));
        testLot.setCurrentPrice(BigDecimal.valueOf(100));
        testLot.setBidStep(BigDecimal.valueOf(10));
        testLot.setOwner(testUser);
        testLot.setStatus(LotStatus.PENDING_APPROVAL);
        testLot.setStartDate(LocalDateTime.now());
        testLot.setEndDate(LocalDateTime.now().plusDays(1));
    }

    @Test
    void getLotById_WithNonExistingLot_ShouldThrowException() {
        when(lotRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lotService.getLotById(999L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void updateLot_AsNonOwner_ShouldThrowException() {
        when(lotRepository.findById(1L)).thenReturn(Optional.of(testLot));
        UpdateLotDto updateDto = new UpdateLotDto("Updated Title", null, null, null, null);

        assertThatThrownBy(() -> lotService.updateLot(1L, updateDto, 999L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.FORBIDDEN);
    }

    @Test
    void updateLot_WithActiveStatus_ShouldThrowException() {
        testLot.setStatus(LotStatus.ACTIVE);
        when(lotRepository.findById(1L)).thenReturn(Optional.of(testLot));
        UpdateLotDto updateDto = new UpdateLotDto("Updated Title", null, null, null, null);

        assertThatThrownBy(() -> lotService.updateLot(1L, updateDto, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
    }

    @Test
    void deleteLot_AsNonOwner_ShouldThrowException() {
        when(lotRepository.findById(1L)).thenReturn(Optional.of(testLot));

        assertThatThrownBy(() -> lotService.deleteLot(1L, 999L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.FORBIDDEN);
    }

    @Test
    void deleteLot_WithActiveStatus_ShouldThrowException() {
        testLot.setStatus(LotStatus.ACTIVE);
        when(lotRepository.findById(1L)).thenReturn(Optional.of(testLot));

        assertThatThrownBy(() -> lotService.deleteLot(1L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
    }

    @Test
    void deleteLot_WithValidConditions_ShouldDeleteLot() {
        testLot.setStatus(LotStatus.DRAFT);
        when(lotRepository.findById(1L)).thenReturn(Optional.of(testLot));

        lotService.deleteLot(1L, 1L);

        verify(lotRepository).delete(testLot);
    }
}
