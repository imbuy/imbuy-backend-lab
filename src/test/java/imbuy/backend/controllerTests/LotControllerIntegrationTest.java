package imbuy.backend.controllerTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import imbuy.backend.AbstractIntegrationTest;
import imbuy.backend.domain.User;
import imbuy.backend.dto.CreateLotDto;
import imbuy.backend.dto.LotDto;
import imbuy.backend.dto.UpdateLotDto;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class LotControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private String userToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new User("test@example.com", passwordEncoder.encode("password"), "testuser");
        testUser = userRepository.save(testUser);

        userToken = jwtTokenProvider.generateToken(testUser.getEmail(), "USER");
    }

    @Test
    void getLots_WithoutAuthentication_ShouldReturnLots() throws Exception {
        mockMvc.perform(get("/api/lots")
                        .param("title", "test")
                        .param("status", "ACTIVE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void createLot_WithValidDataAndAuthentication_ShouldCreateLot() throws Exception {
        CreateLotDto createLotDto = new CreateLotDto();
        createLotDto.setTitle("New Test Lot");
        createLotDto.setDescription("Test Description");
        createLotDto.setStartPrice(BigDecimal.valueOf(100));
        createLotDto.setBidStep(BigDecimal.valueOf(10));
        createLotDto.setStartDate(LocalDateTime.now().plusHours(1));
        createLotDto.setEndDate(LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/api/lots")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createLotDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Test Lot"))
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"));
    }

    @Test
    void createLot_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        CreateLotDto createLotDto = new CreateLotDto();
        createLotDto.setTitle("New Test Lot");
        createLotDto.setStartPrice(BigDecimal.valueOf(100));
        createLotDto.setBidStep(BigDecimal.valueOf(10));

        mockMvc.perform(post("/api/lots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createLotDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getLotById_WithExistingLot_ShouldReturnLot() throws Exception {
        CreateLotDto createLotDto = new CreateLotDto();
        createLotDto.setTitle("Test Lot for Get");
        createLotDto.setStartPrice(BigDecimal.valueOf(100));
        createLotDto.setBidStep(BigDecimal.valueOf(10));
        createLotDto.setStartDate(LocalDateTime.now().plusHours(1));
        createLotDto.setEndDate(LocalDateTime.now().plusDays(1));

        String response = mockMvc.perform(post("/api/lots")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createLotDto)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        LotDto createdLot = objectMapper.readValue(response, LotDto.class);

        mockMvc.perform(get("/api/lots/{id}", createdLot.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdLot.getId()))
                .andExpect(jsonPath("$.title").value("Test Lot for Get"));
    }

    @Test
    void updateLot_AsOwner_ShouldUpdateLot() throws Exception {
        CreateLotDto createLotDto = new CreateLotDto();
        createLotDto.setTitle("Original Lot");
        createLotDto.setStartPrice(BigDecimal.valueOf(100));
        createLotDto.setBidStep(BigDecimal.valueOf(10));
        createLotDto.setStartDate(LocalDateTime.now().plusHours(1));
        createLotDto.setEndDate(LocalDateTime.now().plusDays(1));

        String response = mockMvc.perform(post("/api/lots")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createLotDto)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        LotDto createdLot = objectMapper.readValue(response, LotDto.class);

        UpdateLotDto updateLotDto = new UpdateLotDto();
        updateLotDto.setTitle("Updated Lot Title");
        updateLotDto.setDescription("Updated Description");

        mockMvc.perform(put("/api/lots/{id}", createdLot.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateLotDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Lot Title"));
    }

    @Test
    void toggleFavorite_WithAuthentication_ShouldToggleFavorite() throws Exception {
        CreateLotDto createLotDto = new CreateLotDto();
        createLotDto.setTitle("Lot for Favorite");
        createLotDto.setStartPrice(BigDecimal.valueOf(100));
        createLotDto.setBidStep(BigDecimal.valueOf(10));
        createLotDto.setStartDate(LocalDateTime.now().plusHours(1));
        createLotDto.setEndDate(LocalDateTime.now().plusDays(1));

        String response = mockMvc.perform(post("/api/lots")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createLotDto)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        LotDto createdLot = objectMapper.readValue(response, LotDto.class);

        mockMvc.perform(post("/api/lots/{id}/favorite", createdLot.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }
}