package org.example.backend.service;

import org.example.backend.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    int updateLastLogin(String username);

    User login(String username, String password);

    int register(User user);

    String updateAvatar(MultipartFile avatar,String userId);

    User getInfo(String uid);

    int updateInfo(User user);
}
