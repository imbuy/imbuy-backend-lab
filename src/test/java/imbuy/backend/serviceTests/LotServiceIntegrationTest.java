package imbuy.backend.serviceTests;

import imbuy.backend.AbstractIntegrationTest;
import imbuy.backend.domain.Category;
import imbuy.backend.domain.Lot;
import imbuy.backend.domain.User;
import imbuy.backend.dto.*;
import imbuy.backend.enums.LotStatus;
import imbuy.backend.repository.CategoryRepository;
import imbuy.backend.repository.LotRepository;
import imbuy.backend.repository.UserRepository;
import imbuy.backend.service.LotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LotServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private LotService lotService;

    @Autowired
    private LotRepository lotRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        lotRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User("test@example.com", "password", "testuser");
        testUser = userRepository.save(testUser);

        testCategory = new Category("Electronics");
        testCategory = categoryRepository.save(testCategory);
    }

    @Test
    void createLot_WithValidData_ShouldCreateLot() {
        // Arrange
        CreateLotDto createLotDto = new CreateLotDto();
        createLotDto.setTitle("Test Lot");
        createLotDto.setDescription("Test Description");
        createLotDto.setStartPrice(BigDecimal.valueOf(100));
        createLotDto.setBidStep(BigDecimal.valueOf(10));
        createLotDto.setCategoryId(testCategory.getId());
        createLotDto.setStartDate(LocalDateTime.now());
        createLotDto.setEndDate(LocalDateTime.now().plusDays(1));

        // Act
        LotDto result = lotService.createLot(createLotDto, testUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals("Test Lot", result.getTitle());
        assertEquals(LotStatus.PENDING_APPROVAL, result.getStatus());
        assertEquals(testUser.getId(), result.getOwnerId());

        // Verify lot was saved in database
        Optional<Lot> savedLot = lotRepository.findById(result.getId());
        assertTrue(savedLot.isPresent());
        assertEquals("Test Lot", savedLot.get().getTitle());
    }

    @Test
    void getLots_WithFilters_ShouldReturnFilteredLots() {
        // Arrange - create test lots
        Lot lot1 = new Lot();
        lot1.setTitle("iPhone 15");
        lot1.setDescription("New iPhone");
        lot1.setStartPrice(BigDecimal.valueOf(1000));
        lot1.setCurrentPrice(BigDecimal.valueOf(1000));
        lot1.setBidStep(BigDecimal.valueOf(50));
        lot1.setOwner(testUser);
        lot1.setCategory(testCategory);
        lot1.setStatus(LotStatus.ACTIVE);
        lot1.setStartDate(LocalDateTime.now().minusHours(1));
        lot1.setEndDate(LocalDateTime.now().plusHours(1));
        lotRepository.save(lot1);

        Lot lot2 = new Lot();
        lot2.setTitle("Samsung Galaxy");
        lot2.setDescription("Android phone");
        lot2.setStartPrice(BigDecimal.valueOf(800));
        lot2.setCurrentPrice(BigDecimal.valueOf(800));
        lot2.setBidStep(BigDecimal.valueOf(40));
        lot2.setOwner(testUser);
        lot2.setStatus(LotStatus.ACTIVE);
        lot2.setStartDate(LocalDateTime.now().minusHours(1));
        lot2.setEndDate(LocalDateTime.now().plusHours(1));
        lotRepository.save(lot2);

        LotFilterDto filter = new LotFilterDto();
        filter.setTitle("iPhone");
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        PageResponse<LotDto> result = lotService.getLots(filter, pageable, testUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("iPhone 15", result.getContent().get(0).getTitle());
    }

    @Test
    void getLotById_WithExistingLot_ShouldReturnLot() {
        // Arrange
        Lot lot = new Lot();
        lot.setTitle("Test Lot");
        lot.setDescription("Test Description");
        lot.setStartPrice(BigDecimal.valueOf(100));
        lot.setCurrentPrice(BigDecimal.valueOf(100));
        lot.setBidStep(BigDecimal.valueOf(10));
        lot.setOwner(testUser);
        lot.setStatus(LotStatus.ACTIVE);
        lot = lotRepository.save(lot);

        // Act
        LotDto result = lotService.getLotById(lot.getId());

        // Assert
        assertNotNull(result);
        assertEquals(lot.getId(), result.getId());
        assertEquals("Test Lot", result.getTitle());
    }

    @Test
    void updateLot_AsOwner_ShouldUpdateLot() {
        // Arrange
        Lot lot = new Lot();
        lot.setTitle("Original Title");
        lot.setDescription("Original Description");
        lot.setStartPrice(BigDecimal.valueOf(100));
        lot.setCurrentPrice(BigDecimal.valueOf(100));
        lot.setBidStep(BigDecimal.valueOf(10));
        lot.setOwner(testUser);
        lot.setStatus(LotStatus.DRAFT);
        lot = lotRepository.save(lot);

        UpdateLotDto updateDto = new UpdateLotDto();
        updateDto.setTitle("Updated Title");
        updateDto.setDescription("Updated Description");

        // Act
        LotDto result = lotService.updateLot(lot.getId(), updateDto, testUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Description", result.getDescription());

        // Verify lot was updated in database
        Optional<Lot> updatedLot = lotRepository.findById(lot.getId());
        assertTrue(updatedLot.isPresent());
        assertEquals("Updated Title", updatedLot.get().getTitle());
    }
}