package imbuy.backend.controllerTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import imbuy.backend.AbstractIntegrationTest;
import imbuy.backend.domain.Category;
import imbuy.backend.repository.CategoryRepository;
import imbuy.backend.utils.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class CategoryControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Category testCategory;
    private String adminToken;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();

        testCategory = new Category("Electronics");
        testCategory = categoryRepository.save(testCategory);

        adminToken = jwtTokenProvider.generateToken("admin@imbuy.com", "ROLE_ADMIN");
    }

    @Test
    void getCategoryTree_ShouldReturnTree() throws Exception {
        mockMvc.perform(get("/api/categories/tree")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories").isArray());
    }

    @Test
    void getAllCategories_ShouldReturnAllCategories() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getCategoryById_WithExistingCategory_ShouldReturnCategory() throws Exception {
        mockMvc.perform(get("/api/categories/{id}", testCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Electronics"));
    }
}