package com.dev.E_commerce.Mini.mapper;

import com.dev.E_commerce.Mini.dto.request.PermissionRequest;
import com.dev.E_commerce.Mini.dto.response.PermissionResponse;
import com.dev.E_commerce.Mini.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);
}
