package com.fortn.bookmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fortn.bookmanager.pojo.Reader;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReaderMapper extends BaseMapper<Reader> {
    // 如有复杂自定义SQL再补充
}
