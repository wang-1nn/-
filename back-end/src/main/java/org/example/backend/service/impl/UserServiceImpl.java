package org.example.backend.service.impl;

import jakarta.annotation.Resource;
import org.example.backend.entity.User;
import org.example.backend.mapper.UserMapper;
import org.example.backend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    UserMapper userMapper;

    @Override
    public int updateLastLogin(String username ) {
        Date lastLogin = new Date();
        return userMapper.updateLastLogin(username,lastLogin);
    }

    @Override
    public User login(String username, String password) {
        User user = userMapper.login(username, password);
        if (user != null && user.getStatus() == 1) {
            return user;
        }
        else {
            return null;
        }
    }

    @Override
    public int register(User user) {
        user.setCreatedAt(new Date());
        return userMapper.register(user);
    }

    @Override
    public String updateAvatar(MultipartFile avatar,String userId) {
        return userMapper.updateAvatar(avatar,userId);

    }

    @Override
    public User getInfo(String uid) {
        return userMapper.getInfo(uid);
    }

    @Override
    public int updateInfo(User user) {
        return userMapper.updateInfo(user);
    }

}
