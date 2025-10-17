package imbuy.backend.mapper;

import imbuy.backend.domain.Lot;
import imbuy.backend.dto.LotDto;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class, CategoryMapper.class})
public interface LotMapper {

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "ownerUsername", source = "owner.username")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "bidCount", ignore = true)
    @Mapping(target = "winnerId", ignore = true)
    @Mapping(target = "winnerUsername", ignore = true)
    @Mapping(target = "isFavorite", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    LotDto toDto(Lot lot, @Context Long currentUserId);
}