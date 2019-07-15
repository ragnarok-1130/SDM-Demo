package com.example.common.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserDto implements Serializable {

    private Long id;

    private Long userId;

    private String username;

    private String password;
}
