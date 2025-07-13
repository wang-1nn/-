package org.example.backend.service.impl;

import jakarta.annotation.Resource;
import org.example.backend.entity.pojo.User;
import org.example.backend.mapper.UserMapper;
import org.example.backend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.UUID;

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
    public String updateAvatar(MultipartFile avatar, String userId) {
        try {
            // 确保上传目录存在
            String uploadDir = "uploads/avatars/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 生成唯一文件名
            String originalFilename = avatar.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + fileExtension;
            
            // 保存文件
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(avatar.getInputStream(), filePath);
            
            // 文件URL
            String avatarUrl = "/uploads/avatars/" + newFilename;
            
            // 更新数据库
            int result = userMapper.updateAvatar(avatarUrl, userId);
            
            if (result > 0) {
                return avatarUrl;
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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
