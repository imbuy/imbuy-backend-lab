package imbuy.backend.dto;

import imbuy.backend.enums.LotStatus;

public record LotFilterDto(
        String title,
        LotStatus status,
        Long categoryId,
        Long ownerId,
        Boolean activeOnly
) {}