package com.dev.E_commerce.Mini.mapper;

import com.dev.E_commerce.Mini.dto.request.UserRegisterRequest;
import com.dev.E_commerce.Mini.dto.request.UserUpdateRequest;
import com.dev.E_commerce.Mini.dto.response.UserResponse;
import com.dev.E_commerce.Mini.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "roles", source = "roles")
    @Mapping(target = "fullName", source = "fullName")
    UserResponse toUserResponse(User user);
    @Mapping(target = "roles", ignore = true)
    User toUser(UserRegisterRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
