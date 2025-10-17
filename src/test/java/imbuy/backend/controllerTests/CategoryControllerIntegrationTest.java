package imbuy.backend.controllerTests;

import imbuy.backend.AbstractIntegrationTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@AutoConfigureMockMvc
class CategoryControllerIntegrationTest extends AbstractIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private CategoryRepository categoryRepository;
//
//    @Autowired
//    private JwtTokenProvider jwtTokenProvider;
//
//    private Category testCategory;
//    private String adminToken;
//
//    @BeforeEach
//    void setUp() {
//        categoryRepository.deleteAll();
//
//        testCategory = new Category("Electronics");
//        testCategory = categoryRepository.save(testCategory);
//
//        adminToken = jwtTokenProvider.generateToken("admin@imbuy.com");
//    }
//
//    @Test
//    void getCategoryTree_ShouldReturnTree() throws Exception {
//        mockMvc.perform(get("/api/categories/tree")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.categories").isArray());
//    }
//
//    @Test
//    void getCategoryById_WithExistingCategory_ShouldReturnCategory() throws Exception {
//        mockMvc.perform(get("/api/categories/{id}", testCategory.getId())
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.name").value("Electronics"));
//    }
}