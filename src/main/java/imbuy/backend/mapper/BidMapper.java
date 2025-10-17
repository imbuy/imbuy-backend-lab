package imbuy.backend.mapper;

import imbuy.backend.domain.Bid;
import imbuy.backend.dto.BidDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface BidMapper {
    BidMapper INSTANCE = Mappers.getMapper(BidMapper.class);
    @Mapping(target = "bidderId", source = "bidder.id")
    @Mapping(target = "bidderUsername", source = "bidder.username")
    @Mapping(target = "createdAt", ignore = true)
    BidDto toDto(Bid bid);
}