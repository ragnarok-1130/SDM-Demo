package com.example.provider.service.impl;

import com.example.common.dto.UserDto;
import com.example.common.intf.UserRpcService;
import com.example.provider.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("userRpcService")
public class UserRpcServiceImpl implements UserRpcService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public int addUser(UserDto userDto) {
        return userMapper.insert(userDto);
    }

    @Override
    public UserDto selectUser(long userId) {
        return userMapper.select(userId);
    }

    @Override
    public int deleteUser(long userId) {
        return userMapper.delete(userId);
    }

    @Override
    public int updateUser(UserDto userDto) {
        return userMapper.update(userDto);
    }
}
