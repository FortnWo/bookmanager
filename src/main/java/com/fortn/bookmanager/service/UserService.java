package com.fortn.bookmanager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fortn.bookmanager.mapper.UserMapper;
import com.fortn.bookmanager.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Cacheable(value = "user", key = "#username")
    public User getUserByUsername(String username) {
        return userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                        .eq("username", username));
    }

    public void addUser(User user) {
        userMapper.insert(user);
    }

    public void changePassword(String username, String newPassword) {
        userMapper.update(
                null,
                new UpdateWrapper<User>()
                        .eq("username", username)
                        .set("password", newPassword));
    }

    public boolean checkPassword(String username, String password) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        return user != null && user.getPassword().equals(password);
    }
}