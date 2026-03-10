package com.fortn.bookmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fortn.bookmanager.pojo.Record;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RecordMapper extends BaseMapper<Record> {
    // 如有复杂自定义SQL再补充
}
