package imbuy.backend.controllerTests;

import imbuy.backend.domain.Bid;
import imbuy.backend.domain.Category;
import imbuy.backend.domain.Lot;
import imbuy.backend.domain.User;
import imbuy.backend.dto.CreateBidDto;
import imbuy.backend.enums.LotStatus;
import imbuy.backend.repository.BidRepository;
import imbuy.backend.repository.CategoryRepository;
import imbuy.backend.repository.LotRepository;
import imbuy.backend.repository.UserRepository;
import imbuy.backend.service.BidService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class BidControllerIntegrationTest {

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
    private BidService bidService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private LotRepository lotRepository;

    @Autowired
    private BidRepository bidRepository;

    private Long lotId;
    private Long ownerId;
    private Long bidderId;
    private User owner;
    private User bidder;
    private Category category;
    private Lot activeLot;

    @BeforeEach
    void setUp() {
        bidRepository.deleteAll();
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
        ownerId = owner.getId();

        bidder = User.builder()
                .email("bidder@test.com")
                .password("password")
                .username("bidder_user")
                .balance(BigDecimal.valueOf(1000))
                .build();
        bidder = userRepository.save(bidder);
        bidderId = bidder.getId();

        category = Category.builder()
                .name("Test Category")
                .build();
        category = categoryRepository.save(category);

        activeLot = Lot.builder()
                .title("Test Lot")
                .description("Test Description")
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
        lotId = activeLot.getId();
    }

    @Test
    @WithMockUser
    void getBidsByLotId_ShouldReturnEmptyList_WhenNoBidsExist() throws Exception {
        mockMvc.perform(get("/api/lots/{lotId}/bids", lotId)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.current_page", is(0)))
                .andExpect(jsonPath("$.page_size", is(20)))
                .andExpect(jsonPath("$.has_next", is(false)))
                .andExpect(jsonPath("$.has_previous", is(false)));
    }

    @Test
    @WithMockUser
    void getBidsByLotId_ShouldRespectPagination() throws Exception {
        for (int i = 1; i <= 5; i++) {
            Bid bid = Bid.builder()
                    .lot(activeLot)
                    .bidder(bidder)
                    .amount(BigDecimal.valueOf(100 + i * 10))
                    .build();
            bidRepository.save(bid);
        }

        mockMvc.perform(get("/api/lots/{lotId}/bids", lotId)
                        .param("page", "0")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.page_size", is(3)))
                .andExpect(jsonPath("$.has_next", is(true)))
                .andExpect(jsonPath("$.has_previous", is(false)));
    }

    @Test
    @WithMockUser
    void placeBid_ShouldCreateBid_WhenValidData() throws Exception {
        CreateBidDto createBidDto = new CreateBidDto(BigDecimal.valueOf(110));

        mockMvc.perform(post("/api/lots/{lotId}/bids", lotId)
                        .param("currentUserId", bidderId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": 110.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount", is(110.0)))
                .andExpect(jsonPath("$.bidder_id", is(bidderId.intValue())))
                .andExpect(jsonPath("$.bidder_username", is("bidder_user")));

        mockMvc.perform(get("/api/lots/{lotId}/bids", lotId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].amount", is(110.0)));
    }

    @Test
    @WithMockUser
    void placeBid_ShouldReturnBadRequest_WhenInvalidAmount() throws Exception {
        mockMvc.perform(post("/api/lots/{lotId}/bids", lotId)
                        .param("currentUserId", bidderId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": -50.00
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getWinningBid_ShouldReturnNotFound_WhenNoBids() throws Exception {
        mockMvc.perform(get("/api/lots/{lotId}/bids/winning", lotId))
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser
    void getBidsByLotId_ShouldReturnNotFound_WhenLotDoesNotExist() throws Exception {
        Long nonExistentLotId = 999L;
        mockMvc.perform(get("/api/lots/{lotId}/bids", nonExistentLotId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void placeBid_ShouldReturnNotFound_WhenLotDoesNotExist() throws Exception {
        Long nonExistentLotId = 999L;
        mockMvc.perform(post("/api/lots/{lotId}/bids", nonExistentLotId)
                        .param("currentUserId", bidderId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": 110.00
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void placeBid_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        Long nonExistentUserId = 999L;
        mockMvc.perform(post("/api/lots/{lotId}/bids", lotId)
                        .param("currentUserId", nonExistentUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "amount": 110.00
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getBidsByLotId_WithSizeGreaterThan50_ShouldLimitTo50() throws Exception {
        mockMvc.perform(get("/api/lots/{lotId}/bids", lotId)
                        .param("page", "0")
                        .param("size", "100"))
                .andExpect(status().isOk());
    }
}