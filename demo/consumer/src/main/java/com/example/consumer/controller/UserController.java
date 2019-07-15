package com.example.consumer.controller;

import com.example.common.dto.UserDto;
import com.example.common.intf.UserRpcService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("/user")
@RestController
public class UserController {

    @Reference
    private UserRpcService userRpcService;

    @RequestMapping("/selectUser")
    public UserDto selectUser(@RequestParam Map<String,Object> params){
        try {
            long userId = Long.parseLong(params.get("userId").toString());
            return userRpcService.selectUser(userId);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @RequestMapping("/addUser")
    public String addUser(@RequestBody UserDto userDto){
        try {
            if(userRpcService.addUser(userDto)>0){
                return "新用户注册成功！";
            }else {
                return "注册失败！";
            }
        }catch (Exception e){
            e.printStackTrace();
            return "注册异常！";
        }
    }

    @RequestMapping("/updateUser")
    public String updateUser(@RequestBody UserDto userDto){
        try {
            if(userRpcService.updateUser(userDto)>0){
                return "账户信息修改成功！";
            }else {
                return "账户信息修改失败！";
            }
        }catch (Exception e){
            e.printStackTrace();
            return "账户信息修改异常！";
        }
    }

    @RequestMapping("/deleteUser")
    public String deleteUser(@RequestParam Map<String,Object> params){
        try {
            long userId = Long.parseLong(params.get("userId").toString());
            if(userRpcService.deleteUser(userId)>0){
                return "账户注销成功！";
            }else {
                return "账户注销失败！";
            }
        }catch (Exception e){
            e.printStackTrace();
            return "账户注销异常！";
        }
    }
}
