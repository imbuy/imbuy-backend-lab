package imbuy.backend.mapper;

import imbuy.backend.domain.Lot;
import imbuy.backend.dto.LotDto;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class, CategoryMapper.class})
public interface LotMapper {

    @Mapping(target = "owner_id", source = "owner.id")
    @Mapping(target = "owner_username", source = "owner.username")
    @Mapping(target = "category_id", source = "category.id")
    @Mapping(target = "category_name", source = "category.name")
    @Mapping(target = "bid_count", ignore = true)
    @Mapping(target = "winner_id", ignore = true)
    @Mapping(target = "winner_username", ignore = true)
    @Mapping(target = "is_favorite", ignore = true)
    @Mapping(target = "rejection_reason", ignore = true)
    LotDto mapToDto(Lot lot, @Context Long currentUserId);
}