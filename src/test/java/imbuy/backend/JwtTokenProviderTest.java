package imbuy.backend;

import imbuy.backend.utils.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        jwtTokenProvider.setJwtSecret("veryLongSecretKeyThatIsAtLeast64BytesLongForHS512Algorithm123");
        jwtTokenProvider.setJwtExpirationMs(3600000L);
    }

    @Test
    void generateToken_WithValidData_ShouldReturnToken() {
        String token = jwtTokenProvider.generateToken("test@example.com");

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void getEmailFromToken_WithValidToken_ShouldReturnEmail() {
        String token = jwtTokenProvider.generateToken("test@example.com");

        String email = jwtTokenProvider.getEmailFromToken(token);

        assertEquals("test@example.com", email);
    }


    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        String token = jwtTokenProvider.generateToken("test@example.com");

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        String invalidToken = "invalid.token.here";

        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        assertFalse(isValid);
    }
}