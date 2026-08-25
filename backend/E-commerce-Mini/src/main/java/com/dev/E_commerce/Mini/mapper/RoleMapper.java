package com.dev.E_commerce.Mini.mapper;

import com.dev.E_commerce.Mini.dto.request.RoleRequest;
import com.dev.E_commerce.Mini.dto.response.RoleResponse;
import com.dev.E_commerce.Mini.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions",ignore = true)
    Role toRole(RoleRequest request);
    RoleResponse toRoleResponse(Role role);
}
