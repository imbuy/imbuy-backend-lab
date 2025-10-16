package imbuy.backend.dtoTests;

import imbuy.backend.dto.LotDto;
import imbuy.backend.enums.LotStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LotDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void lotDto_ValidData_ShouldPassValidation() {
        LotDto lotDto = new LotDto();
        lotDto.setId(1L);
        lotDto.setTitle("Test Lot");
        lotDto.setDescription("Test Description");
        lotDto.setStartPrice(new BigDecimal("100.00"));
        lotDto.setCurrentPrice(new BigDecimal("150.00"));
        lotDto.setBidStep(new BigDecimal("10.00"));
        lotDto.setOwnerId(1L);
        lotDto.setOwnerUsername("owner123");
        lotDto.setCategoryId(2L);
        lotDto.setCategoryName("Electronics");
        lotDto.setStatus(LotStatus.ACTIVE);
        lotDto.setStartDate(LocalDateTime.now().minusHours(1));
        lotDto.setEndDate(LocalDateTime.now().plusHours(1));
        lotDto.setCreatedAt(LocalDateTime.now());
        lotDto.setBidCount(5);
        lotDto.setIsFavorite(true);
        lotDto.setWinnerId(3L);
        lotDto.setWinnerUsername("winner123");

        Set<ConstraintViolation<LotDto>> violations = validator.validate(lotDto);

        assertThat(violations).isEmpty();
    }

    @Test
    void lotDto_BlankTitle_ShouldFailValidation() {
        LotDto lotDto = new LotDto();
        lotDto.setTitle("");
        lotDto.setStartPrice(new BigDecimal("100.00"));
        lotDto.setBidStep(new BigDecimal("10.00"));

        Set<ConstraintViolation<LotDto>> violations = validator.validate(lotDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Title is required");
    }

    @Test
    void lotDto_AllFields_ShouldHaveCorrectValues() {
        LocalDateTime startDate = LocalDateTime.now().minusHours(1);
        LocalDateTime endDate = LocalDateTime.now().plusHours(1);
        LocalDateTime createdAt = LocalDateTime.now();

        LotDto lotDto = new LotDto();
        lotDto.setId(1L);
        lotDto.setTitle("Test Lot");
        lotDto.setDescription("Test Description");
        lotDto.setStartPrice(new BigDecimal("200.00"));
        lotDto.setCurrentPrice(new BigDecimal("250.00"));
        lotDto.setBidStep(new BigDecimal("20.00"));
        lotDto.setOwnerId(10L);
        lotDto.setOwnerUsername("testowner");
        lotDto.setCategoryId(5L);
        lotDto.setCategoryName("Books");
        lotDto.setStatus(LotStatus.COMPLETED);
        lotDto.setStartDate(startDate);
        lotDto.setEndDate(endDate);
        lotDto.setCreatedAt(createdAt);
        lotDto.setBidCount(15);
        lotDto.setIsFavorite(false);
        lotDto.setRejectionReason("Invalid description");
        lotDto.setWinnerId(20L);
        lotDto.setWinnerUsername("winneruser");

        assertThat(lotDto.getId()).isEqualTo(1L);
        assertThat(lotDto.getTitle()).isEqualTo("Test Lot");
        assertThat(lotDto.getDescription()).isEqualTo("Test Description");
        assertThat(lotDto.getStartPrice()).isEqualByComparingTo("200.00");
        assertThat(lotDto.getCurrentPrice()).isEqualByComparingTo("250.00");
        assertThat(lotDto.getBidStep()).isEqualByComparingTo("20.00");
        assertThat(lotDto.getOwnerId()).isEqualTo(10L);
        assertThat(lotDto.getOwnerUsername()).isEqualTo("testowner");
        assertThat(lotDto.getCategoryId()).isEqualTo(5L);
        assertThat(lotDto.getCategoryName()).isEqualTo("Books");
        assertThat(lotDto.getStatus()).isEqualTo(LotStatus.COMPLETED);
        assertThat(lotDto.getStartDate()).isEqualTo(startDate);
        assertThat(lotDto.getEndDate()).isEqualTo(endDate);
        assertThat(lotDto.getCreatedAt()).isEqualTo(createdAt);
        assertThat(lotDto.getBidCount()).isEqualTo(15);
        assertThat(lotDto.getIsFavorite()).isFalse();
        assertThat(lotDto.getRejectionReason()).isEqualTo("Invalid description");
        assertThat(lotDto.getWinnerId()).isEqualTo(20L);
        assertThat(lotDto.getWinnerUsername()).isEqualTo("winneruser");
    }

    @Test
    void lotDto_EqualsAndHashCode_ShouldWorkCorrectly() {
        LocalDateTime startDate = LocalDateTime.now().minusHours(1);
        LocalDateTime endDate = LocalDateTime.now().plusHours(1);
        LocalDateTime createdAt = LocalDateTime.now();

        LotDto dto1 = new LotDto();
        dto1.setId(1L);
        dto1.setTitle("Test Lot");
        dto1.setStartPrice(new BigDecimal("100.00"));
        dto1.setBidStep(new BigDecimal("10.00"));
        dto1.setStatus(LotStatus.ACTIVE);
        dto1.setStartDate(startDate);
        dto1.setEndDate(endDate);
        dto1.setCreatedAt(createdAt);

        LotDto dto2 = new LotDto();
        dto2.setId(1L);
        dto2.setTitle("Test Lot");
        dto2.setStartPrice(new BigDecimal("100.00"));
        dto2.setBidStep(new BigDecimal("10.00"));
        dto2.setStatus(LotStatus.ACTIVE);
        dto2.setStartDate(startDate);
        dto2.setEndDate(endDate);
        dto2.setCreatedAt(createdAt);

        LotDto differentDto = new LotDto();
        differentDto.setId(2L);
        differentDto.setTitle("Different Lot");

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1).isNotEqualTo(differentDto);
        assertThat(dto1).isNotEqualTo(null);
        assertThat(dto1).isNotEqualTo("not a LotDto");

        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.hashCode()).isNotEqualTo(differentDto.hashCode());
    }

    @Test
    void lotDto_ToString_ShouldContainAllFields() {
        LocalDateTime startDate = LocalDateTime.now().minusHours(1);
        LocalDateTime endDate = LocalDateTime.now().plusHours(1);
        LocalDateTime createdAt = LocalDateTime.now();

        LotDto lotDto = new LotDto();
        lotDto.setId(1L);
        lotDto.setTitle("Test Lot");
        lotDto.setDescription("Test Description");
        lotDto.setStartPrice(new BigDecimal("200.00"));
        lotDto.setCurrentPrice(new BigDecimal("250.00"));
        lotDto.setBidStep(new BigDecimal("20.00"));
        lotDto.setOwnerId(10L);
        lotDto.setOwnerUsername("testowner");
        lotDto.setCategoryId(5L);
        lotDto.setCategoryName("Books");
        lotDto.setStatus(LotStatus.COMPLETED);
        lotDto.setStartDate(startDate);
        lotDto.setEndDate(endDate);
        lotDto.setCreatedAt(createdAt);
        lotDto.setBidCount(15);
        lotDto.setIsFavorite(false);
        lotDto.setRejectionReason("Invalid description");
        lotDto.setWinnerId(20L);
        lotDto.setWinnerUsername("winneruser");

        String toString = lotDto.toString();

        assertThat(toString).contains("id=1");
        assertThat(toString).contains("title=Test Lot");
        assertThat(toString).contains("description=Test Description");
        assertThat(toString).contains("startPrice=200.00");
        assertThat(toString).contains("currentPrice=250.00");
        assertThat(toString).contains("bidStep=20.00");
        assertThat(toString).contains("ownerId=10");
        assertThat(toString).contains("ownerUsername=testowner");
        assertThat(toString).contains("categoryId=5");
        assertThat(toString).contains("categoryName=Books");
        assertThat(toString).contains("status=COMPLETED");
        assertThat(toString).contains("startDate=" + startDate);
        assertThat(toString).contains("endDate=" + endDate);
        assertThat(toString).contains("createdAt=" + createdAt);
        assertThat(toString).contains("bidCount=15");
        assertThat(toString).contains("isFavorite=false");
        assertThat(toString).contains("rejectionReason=Invalid description");
        assertThat(toString).contains("winnerId=20");
        assertThat(toString).contains("winnerUsername=winneruser");
    }
}