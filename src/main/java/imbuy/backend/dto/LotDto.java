package imbuy.backend.dto;

import imbuy.backend.enums.LotStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public record LotDto(
        Long id,

        @NotBlank(message = "Title is required")
        String title,

        String description,

        @NotNull(message = "Start price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Start price must be greater than 0")
        BigDecimal startPrice,

        BigDecimal currentPrice,

        @NotNull(message = "Bid step is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Bid step must be greater than 0")
        BigDecimal bidStep,

        Long ownerId,
        String ownerUsername,
        Long categoryId,
        String categoryName,
        LotStatus status,
        LocalDateTime startDate,
        LocalDateTime endDate,
        LocalDateTime createdAt,
        Integer bidCount,
        Boolean isFavorite,
        String rejectionReason,
        Long winnerId,
        String winnerUsername
) {
}

