package imbuy.backend.mapper;

import imbuy.backend.domain.Bid;
import imbuy.backend.dto.BidDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BidMapper {
    @Mapping(target = "bidder_id", source = "bidder.id")
    @Mapping(target = "bidder_username", source = "bidder.username")
    BidDto mapToDto(Bid bid);
}