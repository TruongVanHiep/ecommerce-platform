package com.dev.E_commerce.Mini.mapper;

import com.dev.E_commerce.Mini.dto.request.CategoryRequest;
import com.dev.E_commerce.Mini.dto.response.CategoryResponse;
import com.dev.E_commerce.Mini.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toCategory(CategoryRequest request);
    CategoryResponse toCategoryResponse(Category category);

    @Mapping(target = "id", ignore = true)
    void updateCategory(@MappingTarget Category category, CategoryRequest request);
}
