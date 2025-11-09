package imbuy.backend.dtoTests;

import imbuy.backend.dto.BidDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BidDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void bidDto_EqualsAndHashCode_ShouldWorkCorrectly() {
        LocalDateTime now = LocalDateTime.now();

        BidDto bidDto1 = new BidDto(1L, new BigDecimal("100.50"), 1L, "testuser");
        BidDto bidDto2 = new BidDto(1L, new BigDecimal("100.50"), 1L, "testuser");
        BidDto differentBidDto = new BidDto(2L, new BigDecimal("200.00"), null, null);

        assertThat(bidDto1).isEqualTo(bidDto2);
        assertThat(bidDto1).isNotEqualTo(differentBidDto);
        assertThat(bidDto1).isNotEqualTo(null);
        assertThat(bidDto1).isNotEqualTo("not a BidDto");

        assertThat(bidDto1.hashCode()).isEqualTo(bidDto2.hashCode());
        assertThat(bidDto1.hashCode()).isNotEqualTo(differentBidDto.hashCode());
    }

    @Test
    void bidDto_ValidData_ShouldPassValidation() {
        BidDto bidDto = new BidDto(1L, new BigDecimal("100.50"), 1L, "testuser");

        Set<ConstraintViolation<BidDto>> violations = validator.validate(bidDto);

        assertThat(violations).isEmpty();
    }

    @Test
    void bidDto_NullAmount_ShouldFailValidation() {
        BidDto bidDto = new BidDto(1L, null, 1L, "testuser");

        Set<ConstraintViolation<BidDto>> violations = validator.validate(bidDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Amount is required");
    }

    @Test
    void bidDto_ZeroOrNegativeAmount_ShouldFailValidation() {
        BidDto zeroBid = new BidDto(1L, BigDecimal.ZERO, 1L, "user");
        BidDto negativeBid = new BidDto(1L, new BigDecimal("-10.00"), 1L, "user");

        Set<ConstraintViolation<BidDto>> zeroViolations = validator.validate(zeroBid);
        Set<ConstraintViolation<BidDto>> negativeViolations = validator.validate(negativeBid);

        assertThat(zeroViolations).anyMatch(v -> v.getMessage().equals("Amount must be greater than 0"));
        assertThat(negativeViolations).anyMatch(v -> v.getMessage().equals("Amount must be greater than 0"));
    }

    @Test
    void bidDto_AllFields_ShouldHaveCorrectValues() {
        LocalDateTime now = LocalDateTime.now();
        BidDto bidDto = new BidDto(1L, new BigDecimal("150.75"), 2L, "bidder123");

        assertThat(bidDto.id()).isEqualTo(1L);
        assertThat(bidDto.amount()).isEqualByComparingTo("150.75");
        assertThat(bidDto.bidder_id()).isEqualTo(2L);
        assertThat(bidDto.bidder_username()).isEqualTo("bidder123");
    }
}
