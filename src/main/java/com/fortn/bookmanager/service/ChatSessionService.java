package com.fortn.bookmanager.service;

import com.fortn.bookmanager.mapper.ChatMessageMapper;
import com.fortn.bookmanager.model.ChatMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ChatSessionService {

    private final ChatMessageMapper chatMessageMapper;

    @Value("${chat.session.max-messages:50}")
    private int maxMessagesPerSession;

    @Value("${chat.session.max-tokens:4000}")
    private int maxTokensPerSession;

    public ChatSessionService(ChatMessageMapper chatMessageMapper) {
        this.chatMessageMapper = chatMessageMapper;
    }

    public void saveMessage(String sessionId, String role, String content) {
        ChatMessage msg = new ChatMessage();
        msg.setSessionId(sessionId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setCreatedAt(LocalDateTime.now());
        // 简单估算token数（实际应该用模型的tokenizer）
        msg.setTokenCount(estimateTokens(content));
        chatMessageMapper.insert(msg);
    }

    public List<Map<String, String>> loadSessionMessages(String sessionId) {
        List<ChatMessage> messages = chatMessageMapper.findBySessionId(sessionId);
        List<Map<String, String>> result = new ArrayList<>();
        for (ChatMessage msg : messages) {
            result.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
        }
        return result;
    }

    public List<Map<String, String>> loadLastNMessages(String sessionId, int n) {
        List<ChatMessage> messages = chatMessageMapper.findLastNBySessionId(sessionId, n);
        Collections.reverse(messages); // 恢复正序
        List<Map<String, String>> result = new ArrayList<>();
        for (ChatMessage msg : messages) {
            result.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
        }
        return result;
    }

    // 新增：删除会话所有消息
    public void deleteSession(String sessionId) {
        chatMessageMapper.deleteBySessionId(sessionId);
    }

    private int estimateTokens(String content) {
        // 粗略估算：中文字符=2token，英文单词=1token
        return content.length() + (int) (content.chars().filter(ch -> ch > 0x4E00).count());
    }
}