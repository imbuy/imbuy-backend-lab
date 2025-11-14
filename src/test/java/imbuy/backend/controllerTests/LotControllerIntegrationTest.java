package imbuy.backend.controllerTests;

import imbuy.backend.domain.Category;
import imbuy.backend.domain.Lot;
import imbuy.backend.domain.User;
import imbuy.backend.enums.LotStatus;
import imbuy.backend.repository.CategoryRepository;
import imbuy.backend.repository.LotRepository;
import imbuy.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class LotControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LotRepository lotRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User owner;
    private User anotherUser;
    private Category category;
    private Lot activeLot;
    private Lot pendingLot;
    private Lot completedLot;

    @BeforeEach
    void setUp() {
        lotRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        owner = User.builder()
                .email("owner@test.com")
                .password("password")
                .username("owner_user")
                .balance(BigDecimal.valueOf(1000))
                .build();
        owner = userRepository.save(owner);

        anotherUser = User.builder()
                .email("another@test.com")
                .password("password")
                .username("another_user")
                .balance(BigDecimal.valueOf(1000))
                .build();
        anotherUser = userRepository.save(anotherUser);

        category = Category.builder()
                .name("Electronics")
                .build();
        category = categoryRepository.save(category);

        activeLot = Lot.builder()
                .title("Active Test Lot")
                .description("Active lot description")
                .startPrice(BigDecimal.valueOf(100))
                .currentPrice(BigDecimal.valueOf(100))
                .bidStep(BigDecimal.valueOf(10))
                .owner(owner)
                .category(category)
                .status(LotStatus.ACTIVE)
                .startDate(LocalDateTime.now().minusHours(1))
                .endDate(LocalDateTime.now().plusHours(24))
                .build();
        activeLot = lotRepository.save(activeLot);

        pendingLot = Lot.builder()
                .title("Pending Test Lot")
                .description("Pending lot description")
                .startPrice(BigDecimal.valueOf(200))
                .currentPrice(BigDecimal.valueOf(200))
                .bidStep(BigDecimal.valueOf(20))
                .owner(owner)
                .category(category)
                .status(LotStatus.PENDING_APPROVAL)
                .startDate(LocalDateTime.now().minusHours(1))
                .endDate(LocalDateTime.now().plusHours(24))
                .build();
        pendingLot = lotRepository.save(pendingLot);

        completedLot = Lot.builder()
                .title("Completed Test Lot")
                .description("Completed lot description")
                .startPrice(BigDecimal.valueOf(300))
                .currentPrice(BigDecimal.valueOf(350))
                .bidStep(BigDecimal.valueOf(30))
                .owner(owner)
                .category(category)
                .status(LotStatus.COMPLETED)
                .startDate(LocalDateTime.now().minusHours(48))
                .endDate(LocalDateTime.now().minusHours(24))
                .build();
        completedLot = lotRepository.save(completedLot);
    }

    @Test
    void getLots_ShouldReturnAllLots() throws Exception {
        mockMvc.perform(get("/api/lots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].title", is("Active Test Lot")))
                .andExpect(jsonPath("$.content[1].title", is("Pending Test Lot")))
                .andExpect(jsonPath("$.content[2].title", is("Completed Test Lot")));
    }

    @Test
    void getLots_ShouldFilterByTitle() throws Exception {
        mockMvc.perform(get("/api/lots")
                        .param("title", "Active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Active Test Lot")));
    }

    @Test
    void getLots_ShouldFilterActiveOnly() throws Exception {
        mockMvc.perform(get("/api/lots")
                        .param("activeOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Active Test Lot")))
                .andExpect(jsonPath("$.content[0].status", is("ACTIVE")));
    }

    @Test
    void getLotById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        Long nonExistentId = 999L;
        mockMvc.perform(get("/api/lots/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createLot_ShouldCreateLot_WhenValidData() throws Exception {
        String createLotJson = """
                {
                    "title": "New Test Lot",
                    "description": "New lot description",
                    "start_price": 500.00,
                    "bid_step": 25.00,
                    "category_id": %d,
                    "start_date": "%s",
                    "end_date": "%s"
                }
                """.formatted(
                category.getId(),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(25)
        );

        mockMvc.perform(post("/api/lots")
                        .param("currentUserId", owner.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createLotJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("New Test Lot")))
                .andExpect(jsonPath("$.description", is("New lot description")))
                .andExpect(jsonPath("$.start_price", is(500.0)))
                .andExpect(jsonPath("$.current_price", is(500.0)))
                .andExpect(jsonPath("$.bid_step", is(25.0)))
                .andExpect(jsonPath("$.owner_id", is(owner.getId().intValue())))
                .andExpect(jsonPath("$.owner_username", is("owner_user")))
                .andExpect(jsonPath("$.category_id", is(category.getId().intValue())))
                .andExpect(jsonPath("$.status", is("PENDING_APPROVAL")));

        List<Lot> lots = lotRepository.findAll();
        assertTrue(lots.stream().anyMatch(lot -> lot.getTitle().equals("New Test Lot")));
    }

    @Test
    void createLot_ShouldCreateLot_WhenNoCategory() throws Exception {
        String createLotJson = """
                {
                    "title": "Lot Without Category",
                    "description": "No category description",
                    "start_price": 400.00,
                    "bid_step": 20.00
                }
                """;

        mockMvc.perform(post("/api/lots")
                        .param("currentUserId", owner.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createLotJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Lot Without Category")))
                .andExpect(jsonPath("$.category_id", nullValue()))
                .andExpect(jsonPath("$.category_name", nullValue()))
                .andExpect(jsonPath("$.status", is("PENDING_APPROVAL")));
    }

    @Test
    void createLot_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        String invalidLotJson = """
                {
                    "title": "",
                    "start_price": -100.00,
                    "bid_step": 0.00
                }
                """;

        mockMvc.perform(post("/api/lots")
                        .param("currentUserId", owner.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidLotJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createLot_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
        String createLotJson = """
                {
                    "title": "New Lot",
                    "start_price": 100.00,
                    "bid_step": 10.00
                }
                """;

        Long nonExistentUserId = 999L;
        mockMvc.perform(post("/api/lots")
                        .param("currentUserId", nonExistentUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createLotJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void createLot_ShouldReturnNotFound_WhenCategoryNotExists() throws Exception {
        String createLotJson = """
                {
                    "title": "New Lot",
                    "start_price": 100.00,
                    "bid_step": 10.00,
                    "category_id": 999
                }
                """;

        mockMvc.perform(post("/api/lots")
                        .param("currentUserId", owner.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createLotJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void approveLot_ShouldApproveLot_WhenOwnerAndPending() throws Exception {
        mockMvc.perform(put("/api/lots/{id}/approve", pendingLot.getId())
                        .param("currentUserId", owner.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(pendingLot.getId().intValue())))
                .andExpect(jsonPath("$.status", is("ACTIVE")));

        Lot updatedLot = lotRepository.findById(pendingLot.getId()).orElseThrow();
        assertEquals(LotStatus.ACTIVE, updatedLot.getStatus());
    }

    @Test
    void cancelLot_ShouldCancelLot_WhenOwnerAndPending() throws Exception {
        mockMvc.perform(put("/api/lots/{id}/cancel", pendingLot.getId())
                        .param("currentUserId", owner.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(pendingLot.getId().intValue())))
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        Lot updatedLot = lotRepository.findById(pendingLot.getId()).orElseThrow();
        assertEquals(LotStatus.CANCELLED, updatedLot.getStatus());
    }

    @Test
    void updateLot_ShouldReturnForbidden_WhenNotOwner() throws Exception {
        String updateJson = """
                {
                    "title": "Updated Title"
                }
                """;

        mockMvc.perform(put("/api/lots/{id}", pendingLot.getId())
                        .param("currentUserId", anotherUser.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteLot_ShouldDeleteLot_WhenOwnerAndNotActive() throws Exception {
        mockMvc.perform(delete("/api/lots/{id}", pendingLot.getId())
                        .param("currentUserId", owner.getId().toString()))
                .andExpect(status().isNoContent());

        assertFalse(lotRepository.existsById(pendingLot.getId()));
    }

    @Test
    void deleteLot_ShouldReturnForbidden_WhenNotOwner() throws Exception {
        mockMvc.perform(delete("/api/lots/{id}", pendingLot.getId())
                        .param("currentUserId", anotherUser.getId().toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteLot_ShouldReturnNotFound_WhenLotNotExists() throws Exception {
        Long nonExistentId = 999L;
        mockMvc.perform(delete("/api/lots/{id}", nonExistentId)
                        .param("currentUserId", owner.getId().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createLot_ShouldSetCurrentPriceEqualToStartPrice() throws Exception {
        String createLotJson = """
                {
                    "title": "New Lot With Price",
                    "start_price": 750.00,
                    "bid_step": 25.00
                }
                """;

        mockMvc.perform(post("/api/lots")
                        .param("currentUserId", owner.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createLotJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.start_price", is(750.0)))
                .andExpect(jsonPath("$.current_price", is(750.0)));

        Lot savedLot = lotRepository.findAll().stream()
                .filter(lot -> lot.getTitle().equals("New Lot With Price"))
                .findFirst()
                .orElseThrow();
        assertEquals(savedLot.getStartPrice(), savedLot.getCurrentPrice());
    }

    @Test
    void getLots_ShouldHandleNoFilters() throws Exception {
        mockMvc.perform(get("/api/lots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)));
    }

    @Test
    void getLots_ShouldHandleEmptyResult() throws Exception {
        lotRepository.deleteAll();

        mockMvc.perform(get("/api/lots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", empty()));
    }

    @Test
    void createLot_ShouldUseCurrentTime_WhenNoStartDateProvided() throws Exception {
        String createLotJson = """
                {
                    "title": "Lot Without Start Date",
                    "start_price": 100.00,
                    "bid_step": 10.00
                }
                """;

        mockMvc.perform(post("/api/lots")
                        .param("currentUserId", owner.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createLotJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.start_date", notNullValue()));

        Lot savedLot = lotRepository.findAll().stream()
                .filter(lot -> lot.getTitle().equals("Lot Without Start Date"))
                .findFirst()
                .orElseThrow();
        assertNotNull(savedLot.getStartDate());
        assertTrue(savedLot.getStartDate().isBefore(LocalDateTime.now().plusMinutes(1)));
        assertTrue(savedLot.getStartDate().isAfter(LocalDateTime.now().minusMinutes(1)));
    }
}