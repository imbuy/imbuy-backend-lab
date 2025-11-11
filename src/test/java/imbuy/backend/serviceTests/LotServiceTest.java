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
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();

        testLot = Lot.builder()
                .id(1L)
                .title("Test Lot")
                .startPrice(BigDecimal.valueOf(100))
                .currentPrice(BigDecimal.valueOf(100))
                .bidStep(BigDecimal.valueOf(10))
                .owner(testUser)
                .status(LotStatus.PENDING_APPROVAL)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .build();
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
        Lot activeLot = testLot.toBuilder()
                .status(LotStatus.ACTIVE)
                .build();
        when(lotRepository.findById(1L)).thenReturn(Optional.of(activeLot));
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
        Lot activeLot = testLot.toBuilder()
                .status(LotStatus.ACTIVE)
                .build();
        when(lotRepository.findById(1L)).thenReturn(Optional.of(activeLot));

        assertThatThrownBy(() -> lotService.deleteLot(1L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
    }

    @Test
    void deleteLot_WithValidConditions_ShouldDeleteLot() {
        Lot draftLot = testLot.toBuilder()
                .status(LotStatus.DRAFT)
                .build();
        when(lotRepository.findById(1L)).thenReturn(Optional.of(draftLot));

        lotService.deleteLot(1L, 1L);

        verify(lotRepository).delete(draftLot);
    }
}