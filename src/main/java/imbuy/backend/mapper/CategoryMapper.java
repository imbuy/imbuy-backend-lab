package imbuy.backend.mapper;

import imbuy.backend.domain.Category;
import imbuy.backend.dto.CategoryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    @Mapping(target = "children", ignore = true) // игнорируем children для обычного маппинга
    CategoryDto mapToDto(Category category);

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    @Mapping(target = "children", source = "children", qualifiedByName = "mapChildren")
    CategoryDto toDtoWithChildren(Category category);

    @Named("mapChildren")
    default List<CategoryDto> mapChildren(Set<Category> children) {
        if (children == null || children.isEmpty()) {
            return null;
        }
        return children.stream()
                .map(this::toDtoWithChildren)
                .collect(Collectors.toList());
    }
}