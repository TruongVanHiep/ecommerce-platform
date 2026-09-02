package com.dev.E_commerce.Mini.mapper;

import com.dev.E_commerce.Mini.dto.request.UserRegisterRequest;
import com.dev.E_commerce.Mini.dto.request.UserUpdateRequest;
import com.dev.E_commerce.Mini.dto.response.UserResponse;
import com.dev.E_commerce.Mini.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "roles", source = "roles")
    @Mapping(target = "fullName", source = "fullName")
    UserResponse toUserResponse(User user);
    @Mapping(target = "roles", ignore = true)
    User toUser(UserRegisterRequest request);

    // password KHÔNG được map tự động: mật khẩu phải được hash trong service
    // trước khi gán vào entity (trước đây map thẳng chuỗi thô vào DB).
    //
    // NullValuePropertyMappingStrategy.IGNORE: chỉ cập nhật những field client
    // thực sự gửi lên. Mặc định MapStruct ghi đè null lên field bị bỏ trống, nên
    // gửi mỗi fullName là email/username bị xoá thành null → vỡ ràng buộc NOT NULL
    // của DB (cập nhật một phần hồ sơ vốn không dùng được).
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
