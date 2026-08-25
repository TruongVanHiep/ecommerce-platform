package com.dev.E_commerce.Mini.service;

import com.dev.E_commerce.Mini.dto.request.PermissionRequest;
import com.dev.E_commerce.Mini.dto.response.PermissionResponse;
import com.dev.E_commerce.Mini.entity.Permission;
import com.dev.E_commerce.Mini.mapper.PermissionMapper;
import com.dev.E_commerce.Mini.repository.PermissionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionService {
    PermissionRepository permissionRepository;
    PermissionMapper mapper;

    public PermissionResponse createPermission(PermissionRequest request){
        Permission permission = mapper.toPermission(request);
        return mapper.toPermissionResponse(permissionRepository.save(permission));
    }

    public List<PermissionResponse> getAllPermissions(){
        var permissions = permissionRepository.findAll();
        return permissions.stream().map(mapper::toPermissionResponse).toList();
    }

    public void deletePermission(String permission){
        permissionRepository.deleteById(permission);
    }
}
