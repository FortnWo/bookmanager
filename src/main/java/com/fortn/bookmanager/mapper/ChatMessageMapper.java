package com.fortn.bookmanager.mapper;

import com.fortn.bookmanager.model.ChatMessage;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ChatMessageMapper {

    @Insert("INSERT INTO chat_messages (session_id, role, content, created_at, token_count) " +
            "VALUES (#{sessionId}, #{role}, #{content}, #{createdAt}, #{tokenCount})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ChatMessage message);

    @Select("SELECT * FROM chat_messages WHERE session_id = #{sessionId} ORDER BY created_at")
    List<ChatMessage> findBySessionId(String sessionId);

    @Delete("DELETE FROM chat_messages WHERE session_id = #{sessionId}")
    void deleteBySessionId(String sessionId);

    @Select("SELECT * FROM chat_messages WHERE session_id = #{sessionId} " +
            "ORDER BY created_at DESC LIMIT #{limit}")
    List<ChatMessage> findLastNBySessionId(@Param("sessionId") String sessionId, @Param("limit") int limit);
}