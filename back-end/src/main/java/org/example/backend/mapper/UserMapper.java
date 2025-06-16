package org.example.backend.mapper;

import org.apache.ibatis.annotations.*;
import org.example.backend.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Mapper
public interface UserMapper {


    @Select("select * from users where username = #{username} and password = #{password}")
    User login(String username,String password);


    @Options(useGeneratedKeys = true, keyProperty = "userId", keyColumn = "user_id")
    @Insert("insert into users(username,password,real_name,role_id,created_at) values(#{username},#{password},#{realName},#{roleId},#{createdAt})")
    int register(User user);

    @Update("update users set avatar = #{avatar} where user_id = #{userId}")
    String updateAvatar(MultipartFile avatar,String userId);

    @Select("select * from users where user_id = #{uid}")
    User getInfo(String uid);

    @Update("update users set password=#{password},email = #{email},phone = #{phone} where user_id = #{userId}")
    int updateInfo(User user);

    @Update("update users set last_login = #{lastLogin} where username = #{username}")
    int updateLastLogin(String username, Date lastLogin);
}
