package org.example.backend.controller;


import jakarta.annotation.Resource;

import org.example.backend.entity.pojo.User;
import org.example.backend.service.UserService;
import org.example.backend.util.RestBean;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    UserService userService;

    @PostMapping("/update-avatar")
    public RestBean<String> updateAvatar(@RequestParam("avatar") MultipartFile avatar,@RequestParam String userId){
        String avatarUrl = userService.updateAvatar(avatar,userId);
        if (avatarUrl != null){
            return RestBean.success("cg",avatarUrl);
        }
        else return RestBean.failure(500,"头像上传失败请重试");

    }

    @GetMapping("/getInfo")
    public RestBean<User> getInfo(@RequestParam("uid")String uid){
        User user = userService.getInfo(uid);
        if (user!=null)
            return RestBean.success("cg",user);
        else
            return RestBean.failure(500,"获取用户信息失败");
    }

    @PostMapping("/updateInfo")
    public RestBean<String>updateInfo(@RequestBody User user){
        if(userService.updateInfo(user)>0)
            return RestBean.success("cg");
        else
            return RestBean.failure(500,"更新用户信息失败");
    }
}
