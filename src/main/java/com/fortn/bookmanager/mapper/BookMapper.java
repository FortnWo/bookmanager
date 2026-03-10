package com.fortn.bookmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fortn.bookmanager.pojo.Book;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookMapper extends BaseMapper<Book> {
     
}
