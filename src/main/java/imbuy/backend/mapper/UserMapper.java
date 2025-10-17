package imbuy.backend.mapper;

import imbuy.backend.domain.User;
import imbuy.backend.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "createdAt", ignore = true)
    UserDto toDto(User user);
}

