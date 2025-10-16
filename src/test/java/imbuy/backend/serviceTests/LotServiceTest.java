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
    void getLots_WithNoFilters_ShouldReturnAllLots() {
        // Given
        Page<Lot> lotPage = new PageImpl<>(List.of(testLot));
        when(lotRepository.findAll(any(Pageable.class))).thenReturn(lotPage);
        when(bidRepository.countByLotId(anyLong())).thenReturn(0L);
        when(favoriteRepository.existsByUserIdAndLotId(anyLong(), anyLong())).thenReturn(false);

        LotFilterDto filter = new LotFilterDto();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        PageResponse<LotDto> result = lotService.getLots(filter, pageable, 1L);

        // Then
        assertThat(result.getContent()).hasSize(1);
        verify(lotRepository).findAll(pageable);
    }

    @Test
    void getLots_WithActiveOnlyFilter_ShouldReturnActiveLots() {
        // Given
        Page<Lot> lotPage = new PageImpl<>(List.of(testLot));
        when(lotRepository.findByStatus(eq(LotStatus.ACTIVE), any(Pageable.class))).thenReturn(lotPage);
        when(bidRepository.countByLotId(anyLong())).thenReturn(0L);
        when(favoriteRepository.existsByUserIdAndLotId(anyLong(), anyLong())).thenReturn(false);

        LotFilterDto filter = new LotFilterDto();
        filter.setActiveOnly(true);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        PageResponse<LotDto> result = lotService.getLots(filter, pageable, 1L);

        // Then
        assertThat(result.getContent()).hasSize(1);
        verify(lotRepository).findByStatus(LotStatus.ACTIVE, pageable);
    }

    @Test
    void getLots_WithTitleFilter_ShouldReturnFilteredLots() {
        // Given
        Page<Lot> lotPage = new PageImpl<>(List.of(testLot));
        when(lotRepository.findByFilters(eq("Test"), eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(lotPage);
        when(bidRepository.countByLotId(anyLong())).thenReturn(0L);
        when(favoriteRepository.existsByUserIdAndLotId(anyLong(), anyLong())).thenReturn(false);

        LotFilterDto filter = new LotFilterDto();
        filter.setTitle("Test");
        Pageable pageable = PageRequest.of(0, 10);

        // When
        PageResponse<LotDto> result = lotService.getLots(filter, pageable, 1L);

        // Then
        assertThat(result.getContent()).hasSize(1);
        verify(lotRepository).findByFilters("Test", null, null, null, pageable);
    }

    @Test
    void getLotsWithTotalCount_ShouldUseFilters() {
        // Given
        Page<Lot> lotPage = new PageImpl<>(List.of(testLot));
        when(lotRepository.findByFilters(eq("Test"), eq(LotStatus.ACTIVE), eq(1L), eq(null), any(Pageable.class)))
                .thenReturn(lotPage);
        when(bidRepository.countByLotId(anyLong())).thenReturn(0L);
        when(favoriteRepository.existsByUserIdAndLotId(anyLong(), anyLong())).thenReturn(false);

        LotFilterDto filter = new LotFilterDto();
        filter.setTitle("Test");
        filter.setStatus(LotStatus.ACTIVE);
        filter.setCategoryId(1L);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        PageResponse<LotDto> result = lotService.getLotsWithTotalCount(filter, pageable, 1L);

        // Then
        assertThat(result.getContent()).hasSize(1);
        verify(lotRepository).findByFilters("Test", LotStatus.ACTIVE, 1L, null, pageable);
    }

    @Test
    void getLotById_WithNonExistingLot_ShouldThrowException() {
        // Given
        when(lotRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> lotService.getLotById(999L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void createLot_WithNonExistingUser_ShouldThrowException() {
        // Given
        CreateLotDto createLotDto = new CreateLotDto();
        createLotDto.setTitle("Test Lot");
        createLotDto.setStartPrice(BigDecimal.valueOf(100));
        createLotDto.setBidStep(BigDecimal.valueOf(10));

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> lotService.createLot(createLotDto, 999L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void createLot_WithNonExistingCategory_ShouldThrowException() {
        // Given
        CreateLotDto createLotDto = new CreateLotDto();
        createLotDto.setTitle("Test Lot");
        createLotDto.setStartPrice(BigDecimal.valueOf(100));
        createLotDto.setBidStep(BigDecimal.valueOf(10));
        createLotDto.setCategoryId(999L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> lotService.createLot(createLotDto, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void createLot_WithCategory_ShouldSetCategory() {
        // Given
        CreateLotDto createLotDto = new CreateLotDto();
        createLotDto.setTitle("Test Lot");
        createLotDto.setStartPrice(BigDecimal.valueOf(100));
        createLotDto.setBidStep(BigDecimal.valueOf(10));
        createLotDto.setCategoryId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(lotRepository.save(any(Lot.class))).thenAnswer(invocation -> {
            Lot savedLot = invocation.getArgument(0);
            savedLot.setId(1L);
            return savedLot;
        });

        // When
        LotDto result = lotService.createLot(createLotDto, 1L);

        // Then
        assertThat(result.getTitle()).isEqualTo("Test Lot");
        assertThat(result.getStatus()).isEqualTo(LotStatus.PENDING_APPROVAL);
        verify(lotRepository).save(any(Lot.class));
    }

    @Test
    void approveLot_AsAdmin_ShouldApproveLot() {
        // Given
        testLot.setStatus(LotStatus.PENDING_APPROVAL);
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));
        when(lotRepository.findById(1L)).thenReturn(Optional.of(testLot));
        when(lotRepository.save(any(Lot.class))).thenReturn(testLot);

        // When
        LotDto result = lotService.approveLot(1L, 2L);

        // Then
        assertThat(result.getStatus()).isEqualTo(LotStatus.ACTIVE);
        verify(lotRepository).save(testLot);
    }

    @Test
    void approveLot_AsNonAdmin_ShouldThrowException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> lotService.approveLot(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Only admin can approve lots");
    }

    @Test
    void approveLot_WithNonPendingLot_ShouldThrowException() {
        // Given
        testLot.setStatus(LotStatus.ACTIVE);
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));
        when(lotRepository.findById(1L)).thenReturn(Optional.of(testLot));

        // When & Then
        assertThatThrownBy(() -> lotService.approveLot(1L, 2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Lot is not awaiting approval");
    }

    @Test
    void cancelLot_AsAdmin_ShouldCancelLot() {
        // Given
        testLot.setStatus(LotStatus.PENDING_APPROVAL);
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));
        when(lotRepository.findById(1L)).thenReturn(Optional.of(testLot));
        when(lotRepository.save(any(Lot.class))).thenReturn(testLot);

        // When
        LotDto result = lotService.cancelLot(1L, 2L, "Invalid description");

        // Then
        assertThat(result.getStatus()).isEqualTo(LotStatus.CANCELLED);
        assertThat(result.getRejectionReason()).isEqualTo("Invalid description");
        verify(lotRepository).save(testLot);
    }

    @Test
    void cancelLot_AsNonAdmin_ShouldThrowException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> lotService.cancelLot(1L, 1L, "Reason"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Only admin can cancel lots");
    }

    @Test
    void cancelLot_WithNonPendingLot_ShouldThrowException() {
        // Given
        testLot.setStatus(LotStatus.ACTIVE);
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));
        when(lotRepository.findById(1L)).thenReturn(Optional.of(testLot));

        // When & Then
        assertThatThrownBy(() -> lotService.cancelLot(1L, 2L, "Reason"))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
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
    void updateLot_WithPartialUpdates_ShouldUpdateOnlyProvidedFields() {
        // Given
        testLot.setStatus(LotStatus.DRAFT);
        when(lotRepository.findById(1L)).thenReturn(Optional.of(testLot));
        when(lotRepository.save(any(Lot.class))).thenReturn(testLot);

        UpdateLotDto updateDto = new UpdateLotDto();
        updateDto.setTitle("Updated Title");
        updateDto.setBidStep(BigDecimal.valueOf(20));

        // When
        LotDto result = lotService.updateLot(1L, updateDto, 1L);

        // Then
        assertThat(result.getTitle()).isEqualTo("Updated Title");
        verify(lotRepository).save(testLot);
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

    @Test
    void toggleFavorite_AddFavorite_ShouldAddToFavorites() {
        // Given
        when(lotRepository.findById(1L)).thenReturn(Optional.of(testLot));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(favoriteRepository.findByUserAndLot(testUser, testLot)).thenReturn(Optional.empty());

        Favorite favorite = new Favorite(testUser, testLot);
        when(favoriteRepository.save(any(Favorite.class))).thenReturn(favorite);

        // When
        lotService.toggleFavorite(1L, 1L);

        // Then
        verify(favoriteRepository).save(any(Favorite.class));
        verify(favoriteRepository, never()).delete(any(Favorite.class));
    }

    @Test
    void toggleFavorite_RemoveFavorite_ShouldRemoveFromFavorites() {
        // Given
        Favorite existingFavorite = new Favorite(testUser, testLot);
        when(lotRepository.findById(1L)).thenReturn(Optional.of(testLot));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(favoriteRepository.findByUserAndLot(testUser, testLot)).thenReturn(Optional.of(existingFavorite));
        doNothing().when(favoriteRepository).delete(existingFavorite);

        // When
        lotService.toggleFavorite(1L, 1L);

        // Then
        verify(favoriteRepository).delete(existingFavorite);
        verify(favoriteRepository, never()).save(any(Favorite.class));
    }
}
