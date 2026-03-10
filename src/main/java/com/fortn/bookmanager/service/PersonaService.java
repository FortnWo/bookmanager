package com.fortn.bookmanager.service;

import com.fortn.bookmanager.model.Persona;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PersonaService {

    private final Map<String, Persona> store = new ConcurrentHashMap<>();
    private final AtomicInteger seq = new AtomicInteger(1);

    @PostConstruct
    public void initDefaults() {
        // 默认 persona：图书馆助手
        add(new Persona(null, "Library Assistant",
                "你是一个图书馆里的专业助理，友好、耐心且擅长推荐图书、查询馆藏与解答借阅规则。回答应简洁，必要时给出书名与索引建议。",
                "默认图书馆助手机器人"));
        // 轻松风格 persona
        add(new Persona(null, "Friendly Chatbot",
                "你以轻松、幽默的语气与读者对话，适合闲聊与推荐轻阅读。遇到专业问题，请提醒并尽力帮忙。",
                "适合非正式对话"));
    }

    public List<Persona> listAll() {
        return new ArrayList<>(store.values());
    }

    public Persona get(String id) {
        if (id == null)
            return null; // 防护：传入 null 时返回 null
        return store.get(id);
    }

    public Persona add(Persona p) {
        String id = String.valueOf(seq.getAndIncrement());
        p.setId(id);
        store.put(id, p);
        return p;
    }

    public Persona update(String id, Persona p) {
        if (!store.containsKey(id))
            return null;
        p.setId(id);
        store.put(id, p);
        return p;
    }

    public Persona remove(String id) {
        return store.remove(id);
    }
}