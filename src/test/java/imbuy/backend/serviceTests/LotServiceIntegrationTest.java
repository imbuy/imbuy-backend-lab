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
        CreateLotDto createLotDto = new CreateLotDto(
                "Test Lot",
                "Test Description",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(10),
                testCategory.getId(),
                testUser.getId(),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1)
        );

        LotDto result = lotService.createLot(createLotDto, testUser.getId());

        assertNotNull(result);
        assertEquals("Test Lot", result.title());
        assertEquals(LotStatus.PENDING_APPROVAL, result.status());
        assertEquals(testUser.getId(), result.ownerId());

        Optional<Lot> savedLot = lotRepository.findById(result.id());
        assertTrue(savedLot.isPresent());
        assertEquals("Test Lot", savedLot.get().getTitle());
    }

    @Test
    void updateLot_AsOwner_ShouldUpdateLot() {
        Lot lot = new Lot();
        lot.setTitle("Original Title");
        lot.setDescription("Original Description");
        lot.setStartPrice(BigDecimal.valueOf(100));
        lot.setCurrentPrice(BigDecimal.valueOf(100));
        lot.setBidStep(BigDecimal.valueOf(10));
        lot.setOwner(testUser);
        lot.setStatus(LotStatus.DRAFT);
        lot = lotRepository.save(lot);

        UpdateLotDto updateDto = new UpdateLotDto(
                "Updated Title",
                "Updated Description",
                null,
                null,
                null
        );

        LotDto result = lotService.updateLot(lot.getId(), updateDto, testUser.getId());

        assertNotNull(result);
        assertEquals("Updated Title", result.title());
        assertEquals("Updated Description", result.description());

        Optional<Lot> updatedLot = lotRepository.findById(lot.getId());
        assertTrue(updatedLot.isPresent());
        assertEquals("Updated Title", updatedLot.get().getTitle());
    }
}
