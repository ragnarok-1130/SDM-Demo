package com.example.common.intf;

import com.example.common.dto.UserDto;

public interface UserRpcService {
    int addUser(UserDto userDto);
    UserDto selectUser(long userId);
    int deleteUser(long userId);
    int updateUser(UserDto userDto);
}
