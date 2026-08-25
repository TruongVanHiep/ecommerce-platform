package com.dev.E_commerce.Mini.mapper;

import com.dev.E_commerce.Mini.dto.request.ProductRequest;
import com.dev.E_commerce.Mini.dto.request.RoleRequest;
import com.dev.E_commerce.Mini.dto.response.ProductResponse;
import com.dev.E_commerce.Mini.dto.response.RoleResponse;
import com.dev.E_commerce.Mini.entity.Product;
import com.dev.E_commerce.Mini.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toProduct(ProductRequest request);
    @Mapping(source = "category.id", target = "categoryId")
    ProductResponse toProductResponse(Product product);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cartItems", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    void updateProduct(@MappingTarget Product product, ProductRequest request);
}
