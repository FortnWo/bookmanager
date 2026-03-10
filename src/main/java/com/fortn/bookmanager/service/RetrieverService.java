package com.fortn.bookmanager.service;

import com.fortn.bookmanager.mapper.DocumentMapper;
import com.fortn.bookmanager.model.Document;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RetrieverService {

    private final DocumentMapper docMapper;
    // 每条检索结果在上下文中保留的字符数
    private static final int SNIPPET_LEN = 600;

    public RetrieverService(DocumentMapper docMapper) {
        this.docMapper = docMapper;
    }

    public List<Document> retrieve(String query, int topN) {
        if (query == null || query.trim().isEmpty())
            return List.of();
        return docMapper.searchByQuery(query, topN);
    }

    public String buildContextFrom(List<Document> docs) {
        if (docs == null || docs.isEmpty())
            return null;
        StringBuilder sb = new StringBuilder();
        sb.append("参考资料（来自馆内文档），仅供回答参考：\n");
        int idx = 1;
        for (Document d : docs) {
            sb.append("\n[").append(idx++).append("] ");
            if (d.getTitle() != null && !d.getTitle().isEmpty()) {
                sb.append(d.getTitle()).append(" ");
            }
            if (d.getSource() != null && !d.getSource().isEmpty()) {
                sb.append("(").append(d.getSource()).append(") ");
            }
            sb.append("\n");
            String content = d.getContent() == null ? "" : d.getContent().trim();
            if (content.length() > SNIPPET_LEN) {
                sb.append(content.substring(0, SNIPPET_LEN)).append("...");
            } else {
                sb.append(content);
            }
            sb.append("\n---\n");
        }
        sb.append("\n请基于以上参考资料回答用户问题，并在必要时标注来源编号（如[1]）。");
        return sb.toString();
    }

    public List<String> extractSources(List<Document> docs) {
        List<String> out = new ArrayList<>();
        if (docs == null)
            return out;
        for (Document d : docs) {
            String s = (d.getTitle() == null ? "" : d.getTitle())
                    + (d.getSource() == null ? "" : " (" + d.getSource() + ")");
            out.add(s.trim());
        }
        return out;
    }
}