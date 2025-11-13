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
        BigDecimal start_price,
        BigDecimal current_price,
        @NotNull(message = "Bid step is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Bid step must be greater than 0")
        BigDecimal bid_step,
        Long owner_id,
        String owner_username,
        Long category_id,
        String category_name,
        LotStatus status,
        LocalDateTime start_date,
        LocalDateTime end_date,
        Long winner_id,
        String winner_username
) {}

