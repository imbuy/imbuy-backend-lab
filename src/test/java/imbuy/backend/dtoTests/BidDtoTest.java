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

class BidDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void bidDto_EqualsAndHashCode_ShouldWorkCorrectly() {
        LocalDateTime now = LocalDateTime.now();

        BidDto bidDto1 = new BidDto();
        bidDto1.setId(1L);
        bidDto1.setAmount(new BigDecimal("100.50"));
        bidDto1.setBidderId(1L);
        bidDto1.setBidderUsername("testuser");
        bidDto1.setCreatedAt(now);

        BidDto bidDto2 = new BidDto();
        bidDto2.setId(1L);
        bidDto2.setAmount(new BigDecimal("100.50"));
        bidDto2.setBidderId(1L);
        bidDto2.setBidderUsername("testuser");
        bidDto2.setCreatedAt(now);

        BidDto differentBidDto = new BidDto();
        differentBidDto.setId(2L);
        differentBidDto.setAmount(new BigDecimal("200.00"));

        assertThat(bidDto1).isEqualTo(bidDto2);
        assertThat(bidDto1).isNotEqualTo(differentBidDto);
        assertThat(bidDto1).isNotEqualTo(null);
        assertThat(bidDto1).isNotEqualTo("not a BidDto");

        assertThat(bidDto1.hashCode()).isEqualTo(bidDto2.hashCode());
        assertThat(bidDto1.hashCode()).isNotEqualTo(differentBidDto.hashCode());
    }

    @Test
    void bidDto_ToString_ShouldContainAllFields() {
        LocalDateTime now = LocalDateTime.now();
        BidDto bidDto = new BidDto();
        bidDto.setId(1L);
        bidDto.setAmount(new BigDecimal("150.75"));
        bidDto.setBidderId(2L);
        bidDto.setBidderUsername("bidder123");
        bidDto.setCreatedAt(now);

        String toString = bidDto.toString();

        assertThat(toString).contains("id=1");
        assertThat(toString).contains("amount=150.75");
        assertThat(toString).contains("bidderId=2");
        assertThat(toString).contains("bidderUsername=bidder123");
        assertThat(toString).contains("createdAt=" + now);
    }

    @Test
    void bidDto_ValidData_ShouldPassValidation() {
        BidDto bidDto = new BidDto();
        bidDto.setId(1L);
        bidDto.setAmount(new BigDecimal("100.50"));
        bidDto.setBidderId(1L);
        bidDto.setBidderUsername("testuser");
        bidDto.setCreatedAt(LocalDateTime.now());

        Set<ConstraintViolation<BidDto>> violations = validator.validate(bidDto);

        assertThat(violations).isEmpty();
    }

    @Test
    void bidDto_NullAmount_ShouldFailValidation() {
        BidDto bidDto = new BidDto();
        bidDto.setAmount(null);

        Set<ConstraintViolation<BidDto>> violations = validator.validate(bidDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Amount is required");
    }

    @Test
    void bidDto_ZeroAmount_ShouldFailValidation() {
        BidDto bidDto = new BidDto();
        bidDto.setAmount(BigDecimal.ZERO);

        Set<ConstraintViolation<BidDto>> violations = validator.validate(bidDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Amount must be greater than 0");
    }

    @Test
    void bidDto_NegativeAmount_ShouldFailValidation() {
        BidDto bidDto = new BidDto();
        bidDto.setAmount(new BigDecimal("-10.00"));

        Set<ConstraintViolation<BidDto>> violations = validator.validate(bidDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Amount must be greater than 0");
    }

    @Test
    void bidDto_AllFields_ShouldHaveCorrectValues() {
        LocalDateTime now = LocalDateTime.now();
        BidDto bidDto = new BidDto();
        bidDto.setId(1L);
        bidDto.setAmount(new BigDecimal("150.75"));
        bidDto.setBidderId(2L);
        bidDto.setBidderUsername("bidder123");
        bidDto.setCreatedAt(now);

        assertThat(bidDto.getId()).isEqualTo(1L);
        assertThat(bidDto.getAmount()).isEqualByComparingTo("150.75");
        assertThat(bidDto.getBidderId()).isEqualTo(2L);
        assertThat(bidDto.getBidderUsername()).isEqualTo("bidder123");
        assertThat(bidDto.getCreatedAt()).isEqualTo(now);
    }
}