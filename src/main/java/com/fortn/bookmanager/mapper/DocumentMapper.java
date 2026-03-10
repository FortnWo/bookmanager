package com.fortn.bookmanager.mapper;

import com.fortn.bookmanager.model.Document;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DocumentMapper {

    @Insert("INSERT INTO documents (title, content, source, created_at) VALUES (#{title}, #{content}, #{source}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Document doc);

    @Select("SELECT id, title, content, source, created_at FROM documents " +
            "WHERE title LIKE CONCAT('%', #{q}, '%') OR content LIKE CONCAT('%', #{q}, '%') " +
            "ORDER BY created_at DESC LIMIT #{limit}")
    List<Document> searchByQuery(@Param("q") String q, @Param("limit") int limit);

    @Select("SELECT * FROM documents WHERE id = #{id}")
    Document findById(Long id);
}