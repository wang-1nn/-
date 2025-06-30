package org.example.backend.controller;


import jakarta.annotation.Resource;

import org.example.backend.entity.pojo.User;
import org.example.backend.service.UserService;
import org.example.backend.util.JWTUtil;
import org.example.backend.util.RestBean;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    UserService userService;

    @PostMapping("/login")
    public RestBean<User>login(@RequestParam String username , @RequestParam String password){

        User user =  userService.login(username,password);
        userService.updateLastLogin(username);
        if (user != null){
            String token= JWTUtil.createToken(user);
            user.setToken(token);
            return RestBean.success("登录成功",user);
        }
        else
            return RestBean.failure(500,"登录失败请重试");
    }

    @PostMapping("/register")
    public RestBean<String>register(@RequestBody User user){
        if (userService.register(user)>0){
            return RestBean.success("注册成功");
        }
        else
            return RestBean.failure(500,"注册失败请重试");
    }
}
