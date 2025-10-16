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
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setUsername("testuser");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(registerRequest);

        assertThat(violations).isEmpty();
    }

    @Test
    void registerRequest_NullUsername_ShouldPassValidation() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setUsername(null);

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(registerRequest);

        assertThat(violations).isEmpty();
    }

    @Test
    void registerRequest_AllFields_ShouldHaveCorrectValues() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("user@example.com");
        registerRequest.setPassword("securepassword");
        registerRequest.setUsername("newuser");

        assertThat(registerRequest.getEmail()).isEqualTo("user@example.com");
        assertThat(registerRequest.getPassword()).isEqualTo("securepassword");
        assertThat(registerRequest.getUsername()).isEqualTo("newuser");
    }

    @Test
    void registerRequest_EqualsAndHashCode_ShouldWorkCorrectly() {
        RegisterRequest request1 = new RegisterRequest();
        request1.setEmail("test@example.com");
        request1.setPassword("password123");
        request1.setUsername("testuser");

        RegisterRequest request2 = new RegisterRequest();
        request2.setEmail("test@example.com");
        request2.setPassword("password123");
        request2.setUsername("testuser");

        RegisterRequest differentRequest = new RegisterRequest();
        differentRequest.setEmail("different@example.com");

        assertThat(request1).isEqualTo(request2);
        assertThat(request1).isNotEqualTo(differentRequest);
        assertThat(request1).isNotEqualTo(null);
        assertThat(request1).isNotEqualTo("not a RegisterRequest");

        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        assertThat(request1.hashCode()).isNotEqualTo(differentRequest.hashCode());
    }
}
