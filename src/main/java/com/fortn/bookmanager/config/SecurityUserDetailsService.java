package com.fortn.bookmanager.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fortn.bookmanager.mapper.UserMapper;
import com.fortn.bookmanager.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SecurityUserDetailsService implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<SimpleGrantedAuthority> list = new ArrayList<>();
        User user = userMapper.selectOne(
                new QueryWrapper<User>().eq("username", username));
        if (user == null)
            throw new UsernameNotFoundException("用户不存在");
        String role = "ROLE_" + user.getRole().toUpperCase();
        list.add(new SimpleGrantedAuthority(role));
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), list);
    }
}
