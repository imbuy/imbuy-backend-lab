package imbuy.backend.serviceTests;
import imbuy.backend.domain.*;
import imbuy.backend.dto.*;
import imbuy.backend.enums.LotStatus;
import imbuy.backend.repository.*;
import imbuy.backend.service.LotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LotServiceTest {

    @Mock
    private LotRepository lotRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private LotService lotService;

    private User testUser;
    private User adminUser;
    private Category testCategory;
    private Lot testLot;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@test.com");
        testUser.setUsername("testuser");
        testUser.setRoles(Set.of("USER"));

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@test.com");
        adminUser.setUsername("admin");
        adminUser.setRoles(Set.of("ADMIN"));

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Electronics");

        testLot = new Lot();
        testLot.setId(1L);
        testLot.setTitle("Test Lot");
        testLot.setDescription("Test Description");
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
        // Given
        when(lotRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> lotService.getLotById(999L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void updateLot_AsNonOwner_ShouldThrowException() {
        // Given
        when(lotRepository.findById(1L)).thenReturn(Optional.of(testLot));

        UpdateLotDto updateDto = new UpdateLotDto();
        updateDto.setTitle("Updated Title");

        // When & Then
        assertThatThrownBy(() -> lotService.updateLot(1L, updateDto, 999L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.FORBIDDEN);
    }

    @Test
    void updateLot_WithActiveStatus_ShouldThrowException() {
        // Given
        testLot.setStatus(LotStatus.ACTIVE);
        when(lotRepository.findById(1L)).thenReturn(Optional.of(testLot));

        UpdateLotDto updateDto = new UpdateLotDto();
        updateDto.setTitle("Updated Title");

        // When & Then
        assertThatThrownBy(() -> lotService.updateLot(1L, updateDto, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
    }

    @Test
    void deleteLot_AsNonOwner_ShouldThrowException() {
        // Given
        when(lotRepository.findById(1L)).thenReturn(Optional.of(testLot));

        // When & Then
        assertThatThrownBy(() -> lotService.deleteLot(1L, 999L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.FORBIDDEN);
    }

    @Test
    void deleteLot_WithActiveStatus_ShouldThrowException() {
        // Given
        testLot.setStatus(LotStatus.ACTIVE);
        when(lotRepository.findById(1L)).thenReturn(Optional.of(testLot));

        // When & Then
        assertThatThrownBy(() -> lotService.deleteLot(1L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
    }

    @Test
    void deleteLot_WithValidConditions_ShouldDeleteLot() {
        // Given
        testLot.setStatus(LotStatus.DRAFT);
        when(lotRepository.findById(1L)).thenReturn(Optional.of(testLot));
        doNothing().when(lotRepository).delete(testLot);

        // When
        lotService.deleteLot(1L, 1L);

        // Then
        verify(lotRepository).delete(testLot);
    }
}
