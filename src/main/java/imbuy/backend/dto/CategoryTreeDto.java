package imbuy.backend.dto;

import java.util.List;


public record CategoryTreeDto(
        List<CategoryDto> categories
) {
}
