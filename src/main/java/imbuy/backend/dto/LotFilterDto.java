package imbuy.backend.dto;

public record LotFilterDto(
        String title,
        Boolean active_only
) {}