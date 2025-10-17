package imbuy.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UpdateLotDto(
        String title,
        String description,
        BigDecimal bidStep,
        Long categoryId,
        LocalDateTime endDate
) {
}
