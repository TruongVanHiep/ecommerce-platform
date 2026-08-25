package com.dev.E_commerce.Mini.service;

import com.dev.E_commerce.Mini.dto.request.RoleRequest;
import com.dev.E_commerce.Mini.dto.response.RoleResponse;
import com.dev.E_commerce.Mini.mapper.RoleMapper;
import com.dev.E_commerce.Mini.repository.PermissionRepository;
import com.dev.E_commerce.Mini.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    RoleRepository roleRepository;
    RoleMapper roleMapper;
    PermissionRepository permissionRepository;

    public RoleResponse create(RoleRequest request) {
        var role = roleMapper.toRole(request);

        var permissions = permissionRepository.findAllById(request.getPermissions());
        role.setPermissions(new HashSet<>(permissions));
        return  roleMapper.toRoleResponse(roleRepository.save(role));
    }

    public List<RoleResponse> getAllRoles(){
        return roleRepository.findAll().
                stream().map(roleMapper::toRoleResponse).toList();
    }

    public void deleteRole(String roleName) {
        roleRepository.deleteById(roleName);
    }
}
