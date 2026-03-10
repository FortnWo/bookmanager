package com.fortn.bookmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fortn.bookmanager.pojo.Announcement;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
    // 如有复杂自定义SQL再补充
}