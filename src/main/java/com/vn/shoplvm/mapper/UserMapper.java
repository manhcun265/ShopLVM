package com.vn.shoplvm.mapper;


import com.vn.shoplvm.dto.response.UserResponse;
import com.vn.shoplvm.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserResponse toUserResponse(User user);
}

