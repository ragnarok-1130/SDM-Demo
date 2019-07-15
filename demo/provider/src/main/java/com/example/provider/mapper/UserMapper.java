package com.example.provider.mapper;

import com.example.common.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface UserMapper {
    int insert(UserDto userDto);

    int update(UserDto userDto);

    int delete(long userId);

    UserDto select(long userId);
}
