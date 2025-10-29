
package imbuy.backend.mapper;

import imbuy.backend.domain.User;
import imbuy.backend.dto.UserDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto mapToDto(User user);
}
