package imbuy.backend.mapper;

import imbuy.backend.domain.Lot;
import imbuy.backend.dto.LotDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class, CategoryMapper.class})
public interface LotMapper {

    @Mapping(target = "owner_id", source = "owner.id")
    @Mapping(target = "owner_username", source = "owner.username")
    @Mapping(target = "category_id", source = "category.id")
    @Mapping(target = "category_name", source = "category.name")
    @Mapping(target = "winner_id", source = "lot.winner.id")
    @Mapping(target = "winner_username", source = "lot.winner.username")
    @Mapping(target = "start_date", source = "startDate")
    @Mapping(target = "end_date", source = "endDate")
    @Mapping(target = "start_price", source = "startPrice")
    @Mapping(target = "current_price", source = "currentPrice")
    @Mapping(target = "bid_step", source = "bidStep")
    LotDto mapToDto(Lot lot);
}