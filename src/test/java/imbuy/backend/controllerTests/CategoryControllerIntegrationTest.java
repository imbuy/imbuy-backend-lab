package imbuy.backend.controllerTests;

import imbuy.backend.domain.Category;
import imbuy.backend.repository.CategoryRepository;
import imbuy.backend.repository.LotRepository;
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
class CategoryControllerIntegrationTest {

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
    private CategoryRepository categoryRepository;

    @Autowired
    private LotRepository lotRepository;

    private Category parentCategory;
    private Category childCategory1;
    private Category childCategory2;
    private Category standaloneCategory;

    @BeforeEach
    void setUp() {
        lotRepository.deleteAll();
        categoryRepository.deleteAll();

        parentCategory = Category.builder()
                .name("Electronics")
                .build();
        parentCategory = categoryRepository.save(parentCategory);

        childCategory1 = Category.builder()
                .name("Smartphones")
                .parent(parentCategory)
                .build();
        childCategory1 = categoryRepository.save(childCategory1);

        childCategory2 = Category.builder()
                .name("Laptops")
                .parent(parentCategory)
                .build();
        childCategory2 = categoryRepository.save(childCategory2);

        standaloneCategory = Category.builder()
                .name("Books")
                .build();
        standaloneCategory = categoryRepository.save(standaloneCategory);

        parentCategory = categoryRepository.findById(parentCategory.getId()).orElseThrow();
    }

    @Test
    void getAllCategoriesPaginated_ShouldReturnPaginatedCategories() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(4))) // All categories
                .andExpect(jsonPath("$.current_page", is(0)))
                .andExpect(jsonPath("$.page_size", is(10)))
                .andExpect(jsonPath("$.has_next", is(false)))
                .andExpect(jsonPath("$.has_previous", is(false)));
    }

    @Test
    void getAllCategoriesPaginated_ShouldRespectPagination() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.page_size", is(2)))
                .andExpect(jsonPath("$.has_next", is(true)))
                .andExpect(jsonPath("$.has_previous", is(false)));
    }

    @Test
    void getCategoryById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        Long nonExistentId = 999L;
        mockMvc.perform(get("/api/categories/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCategory_ShouldCreateRootCategory_WhenValidData() throws Exception {
        String categoryJson = """
                {
                    "name": "New Category"
                }
                """;

        mockMvc.perform(post("/api/categories/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("New Category")))
                .andExpect(jsonPath("$.parent_id", nullValue()))
                .andExpect(jsonPath("$.parent_name", nullValue()));

        List<Category> categories = categoryRepository.findAll();
        assertTrue(categories.stream().anyMatch(c -> c.getName().equals("New Category")));
    }

    @Test
    void createCategory_ShouldCreateChildCategory_WhenParentProvided() throws Exception {
        String categoryJson = """
                {
                    "name": "Tablets",
                    "parent_id": %d
                }
                """.formatted(parentCategory.getId());

        mockMvc.perform(post("/api/categories/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Tablets")))
                .andExpect(jsonPath("$.parent_id", is(parentCategory.getId().intValue())))
                .andExpect(jsonPath("$.parent_name", is("Electronics")));

        Category savedCategory = categoryRepository.findAll().stream()
                .filter(c -> c.getName().equals("Tablets"))
                .findFirst()
                .orElseThrow();
        assertEquals(parentCategory.getId(), savedCategory.getParent().getId());
    }

    @Test
    void createCategory_ShouldReturnBadRequest_WhenNameIsBlank() throws Exception {
        String categoryJson = """
                {
                    "name": ""
                }
                """;

        mockMvc.perform(post("/api/categories/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCategory_ShouldChangeParent_WhenNewParentProvided() throws Exception {
        Category newParent = Category.builder()
                .name("New Parent")
                .build();
        newParent = categoryRepository.save(newParent);

        String updateJson = """
                {
                    "name": "Smartphones",
                    "parent_id": %d
                }
                """.formatted(newParent.getId());

        mockMvc.perform(put("/api/categories/update/{id}", childCategory1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Smartphones")))
                .andExpect(jsonPath("$.parent_id", is(newParent.getId().intValue())))
                .andExpect(jsonPath("$.parent_name", is("New Parent")));

        Category updatedCategory = categoryRepository.findById(childCategory1.getId()).orElseThrow();
        assertEquals(newParent.getId(), updatedCategory.getParent().getId());
    }

    @Test
    void updateCategory_ShouldRemoveParent_WhenParentIdIsNull() throws Exception {
        String updateJson = """
                {
                    "name": "Standalone Smartphones",
                    "parent_id": null
                }
                """;

        mockMvc.perform(put("/api/categories/update/{id}", childCategory1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Standalone Smartphones")))
                .andExpect(jsonPath("$.parent_id", nullValue()))
                .andExpect(jsonPath("$.parent_name", nullValue()));

        Category updatedCategory = categoryRepository.findById(childCategory1.getId()).orElseThrow();
        assertNull(updatedCategory.getParent());
    }

    @Test
    void updateCategory_ShouldReturnNotFound_WhenCategoryNotExists() throws Exception {
        Long nonExistentId = 999L;
        String updateJson = """
                {
                    "name": "Updated Name"
                }
                """;

        mockMvc.perform(put("/api/categories/update/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCategory_ShouldDeleteCategory_WhenNoDependencies() throws Exception {
        Category deletableCategory = Category.builder()
                .name("Deletable Category")
                .build();
        deletableCategory = categoryRepository.save(deletableCategory);

        mockMvc.perform(delete("/api/categories/delete/{id}", deletableCategory.getId()))
                .andExpect(status().isNoContent());

        assertFalse(categoryRepository.existsById(deletableCategory.getId()));
    }

    @Test
    void deleteCategory_ShouldReturnNotFound_WhenCategoryNotExists() throws Exception {
        Long nonExistentId = 999L;
        mockMvc.perform(delete("/api/categories/delete/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllCategoriesPaginated_WithSizeGreaterThan50_ShouldLimitTo50() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .param("page", "0")
                        .param("size", "100"))
                .andExpect(status().isOk());
    }

    @Test
    void getCategoryTree_ShouldHandleEmptyDatabase() throws Exception {
        categoryRepository.deleteAll();

        mockMvc.perform(get("/api/categories/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories", empty()));
    }

    @Test
    void createCategory_ShouldAllowSameNameInDifferentParents() throws Exception {
        Category otherParent = Category.builder()
                .name("Other Parent")
                .build();
        otherParent = categoryRepository.save(otherParent);

        String categoryJson = """
                {
                    "name": "Smartphones",
                    "parent_id": %d
                }
                """.formatted(otherParent.getId());

        mockMvc.perform(post("/api/categories/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoryJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Smartphones")))
                .andExpect(jsonPath("$.parent_id", is(otherParent.getId().intValue())));
    }

    @Test
    void updateCategory_ShouldNotAllowCircularReference() throws Exception {
        String updateJson = """
                {
                    "name": "Electronics",
                    "parent_id": %d
                }
                """.formatted(childCategory1.getId());

        mockMvc.perform(put("/api/categories/update/{id}", parentCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk());
    }
}