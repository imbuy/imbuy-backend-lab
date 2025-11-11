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

        testUser = User.builder()
                .email("test@example.com")
                .password("password")
                .username("testuser")
                .build();
        testUser = userRepository.save(testUser);

        testCategory = Category.builder()
                .name("Electronics")
                .build();
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
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1)
        );

        LotDto result = lotService.createLot(createLotDto, testUser.getId());

        assertNotNull(result);
        assertEquals("Test Lot", result.title());
        assertEquals(LotStatus.PENDING_APPROVAL, result.status());
        assertEquals(testUser.getId(), result.owner_id());

        Optional<Lot> savedLot = lotRepository.findById(result.id());
        assertTrue(savedLot.isPresent());
        assertEquals("Test Lot", savedLot.get().getTitle());
    }

    @Test
    void updateLot_AsOwner_ShouldUpdateLot() {
        Lot lot = Lot.builder()
                .title("Original Title")
                .description("Original Description")
                .startPrice(BigDecimal.valueOf(100))
                .currentPrice(BigDecimal.valueOf(100))
                .bidStep(BigDecimal.valueOf(10))
                .owner(testUser)
                .status(LotStatus.DRAFT)
                .build();
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