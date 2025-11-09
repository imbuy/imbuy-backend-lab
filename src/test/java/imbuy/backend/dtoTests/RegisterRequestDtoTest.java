package imbuy.backend.dtoTests;

import imbuy.backend.dto.RegisterRequest;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterRequestDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void registerRequest_ValidData_ShouldPassValidation() {
        RegisterRequest registerRequest = new RegisterRequest("test@example.com", "password123", "testuser");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(registerRequest);

        assertThat(violations).isEmpty();
    }

    @Test
    void registerRequest_NullUsername_ShouldPassValidation() {
        RegisterRequest registerRequest = new RegisterRequest("test@example.com", "password123", null);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(registerRequest);

        assertThat(violations).isEmpty();
    }

    @Test
    void registerRequest_AllFields_ShouldHaveCorrectValues() {
        RegisterRequest registerRequest = new RegisterRequest("user@example.com", "securepassword", "newuser");

        assertThat(registerRequest.email()).isEqualTo("user@example.com");
        assertThat(registerRequest.password()).isEqualTo("securepassword");
        assertThat(registerRequest.username()).isEqualTo("newuser");
    }

    @Test
    void registerRequest_EqualsAndHashCode_ShouldWorkCorrectly() {
        RegisterRequest request1 = new RegisterRequest("test@example.com", "password123", "testuser");
        RegisterRequest request2 = new RegisterRequest("test@example.com", "password123", "testuser");
        RegisterRequest differentRequest = new RegisterRequest("different@example.com", "password123", "testuser");

        assertThat(request1).isEqualTo(request2);
        assertThat(request1).isNotEqualTo(differentRequest);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        assertThat(request1.hashCode()).isNotEqualTo(differentRequest.hashCode());
    }
}
