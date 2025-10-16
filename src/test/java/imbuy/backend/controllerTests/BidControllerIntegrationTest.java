package imbuy.backend.controllerTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import imbuy.backend.AbstractIntegrationTest;
import imbuy.backend.domain.Lot;
import imbuy.backend.domain.User;
import imbuy.backend.enums.LotStatus;
import imbuy.backend.repository.LotRepository;
import imbuy.backend.repository.UserRepository;
import imbuy.backend.utils.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class BidControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LotRepository lotRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private Lot testLot;
    private String userToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        lotRepository.deleteAll();

        testUser = new User("test@example.com", passwordEncoder.encode("password"), "testuser");
        testUser = userRepository.save(testUser);

        testLot = new Lot();
        testLot.setTitle("Test Lot");
        testLot.setDescription("Test Description");
        testLot.setStartPrice(BigDecimal.valueOf(100));
        testLot.setCurrentPrice(BigDecimal.valueOf(100));
        testLot.setBidStep(BigDecimal.valueOf(10));
        testLot.setOwner(testUser);
        testLot.setStatus(LotStatus.ACTIVE);
        testLot.setStartDate(LocalDateTime.now().minusHours(1));
        testLot.setEndDate(LocalDateTime.now().plusHours(1));
        testLot = lotRepository.save(testLot);

        userToken = jwtTokenProvider.generateToken(testUser.getEmail(), "USER");
    }

    @Test
    void getBidsByLotId_WithExistingLot_ShouldReturnBids() throws Exception {
        mockMvc.perform(get("/api/lots/{lotId}/bids", testLot.getId())
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getWinningBid_WithNoBids_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/lots/{lotId}/bids/winning", testLot.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}