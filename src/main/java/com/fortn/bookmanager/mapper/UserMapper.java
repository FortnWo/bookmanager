package com.fortn.bookmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fortn.bookmanager.pojo.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 如有复杂自定义SQL再补充
}
