package com.fortn.bookmanager.controller;

import com.fortn.bookmanager.model.Document;
import com.fortn.bookmanager.pojo.Book;
import com.fortn.bookmanager.pojo.Reader;
import com.fortn.bookmanager.pojo.Record;
import com.fortn.bookmanager.service.BaiduAiService;
import com.fortn.bookmanager.service.BookService;
import com.fortn.bookmanager.model.Persona;
import com.fortn.bookmanager.model.SessionSettings;
import com.fortn.bookmanager.service.PersonaService;
import com.fortn.bookmanager.service.ChatSessionService;
import com.fortn.bookmanager.service.SessionSettingsService;
import com.fortn.bookmanager.service.RetrieverService;
import com.fortn.bookmanager.service.ReaderService;
import com.fortn.bookmanager.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private BaiduAiService baiduAiService;

    @Autowired
    private PersonaService personaService;

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private SessionSettingsService sessionSettingsService;

    @Autowired
    private RetrieverService retrieverService;

    @Autowired
    private BookService bookService;

    @Autowired
    private ReaderService readerService;

    @Autowired
    private RecordService recordService;

    @Value("${baidu.ai.model:ernie-speed-128k}")
    private String defaultModel;

    private static final String SESSION_PERSONA_ID = "personaId";

    // chat 方法（只显示修改的关键片段）
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, Object> req, HttpSession session) {
        String msg = Objects.toString(req.getOrDefault("message", req.getOrDefault("content", "")), "").trim();
        if (msg.isEmpty())
            return Map.of("reply", "", "error", "empty message");

        String sessionId = (String) session.getAttribute("chatSessionId");
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
            session.setAttribute("chatSessionId", sessionId);
        }

        // 获取会话级设置（若前端通过 body 覆盖参数，会在下方合并）
        SessionSettings sessionSettings = sessionSettingsService.getOrCreate(session);

        // 若请求体中传了参数，构造覆盖对象并计算生效设置
        SessionSettings override = new SessionSettings();
        if (req.containsKey("model"))
            override.setModel(Objects.toString(req.get("model"), null));
        if (req.containsKey("temperature")) {
            try {
                override.setTemperature(Double.parseDouble(req.get("temperature").toString()));
            } catch (Exception ignored) {
            }
        }
        if (req.containsKey("top_p")) {
            try {
                override.setTopP(Double.parseDouble(req.get("top_p").toString()));
            } catch (Exception ignored) {
            }
        }
        if (req.containsKey("max_tokens")) {
            try {
                override.setMaxTokens(Integer.parseInt(req.get("max_tokens").toString()));
            } catch (Exception ignored) {
            }
        }

        SessionSettings effective = sessionSettingsService.toEffective(sessionSettings, override);

        // 1) 检索相关文档（RAG）：优先 top3
        List<Document> docs = retrieverService.retrieve(msg, 3);
        String retrievedContext = retrieverService.buildContextFrom(docs);
        List<String> sources = retrieverService.extractSources(docs);

        // 2) 加载最近 messages
        List<Map<String, String>> messages = chatSessionService.loadLastNMessages(sessionId, 10);
        if (messages.isEmpty()) {
            String personaId = (String) session.getAttribute(SESSION_PERSONA_ID);
            Persona persona = personaId != null ? personaService.get(personaId) : null;
            if (persona == null) {
                List<Persona> all = personaService.listAll();
                persona = all.isEmpty() ? null : all.get(0);
            }
            String systemContent = persona != null ? persona.getContent() : "你是一个友好的图书馆助理。";
            chatSessionService.saveMessage(sessionId, "system", systemContent);
            messages.add(Map.of("role", "system", "content", systemContent));
        }

        // (A) 注入权限与工具使用策略，减少模型由于隐私策略而拒绝调用工具
        String policy = buildPolicySystemPrompt();
        if (policy != null && !policy.isEmpty()) {
            if (!messages.isEmpty() && "system".equals(messages.get(0).get("role"))) {
                Map<String, String> first = messages.get(0);
                String merged = first.get("content") + "\n\n" + policy;
                messages.set(0, Map.of("role", "system", "content", merged));
            } else {
                messages.add(0, Map.of("role", "system", "content", policy));
            }
        }

        // (B) 如果检索到上下文：合并到现有的首个 system 提示，避免多个 system 被模型忽略
        if (retrievedContext != null && !retrievedContext.isEmpty()) {
            if (!messages.isEmpty() && "system".equals(messages.get(0).get("role"))) {
                Map<String, String> first = messages.get(0);
                String merged = first.get("content") + "\n\n" + retrievedContext;
                // 覆盖首个 system 内容
                messages.set(0, Map.of("role", "system", "content", merged));
            } else {
                messages.add(0, Map.of("role", "system", "content", retrievedContext));
            }
            // 仅在会话存档中记录一次上下文注入，避免历史无限膨胀
            chatSessionService.saveMessage(sessionId, "system", "[RAG] 上下文已注入");
        }

        // 保存用户消息
        chatSessionService.saveMessage(sessionId, "user", msg);
        messages.add(Map.of("role", "user", "content", msg));

        // 使用生效参数调用模型
        String model = effective.getModel() == null ? defaultModel : effective.getModel();
        double temperature = effective.getTemperature() == null ? 0.8 : effective.getTemperature();
        double topP = effective.getTopP() == null ? 0.9 : effective.getTopP();
        Integer maxTokens = effective.getMaxTokens();

        try {
            // 先尝试使用 tools 调用，让模型可直接请求后端数据
            List<Map<String, Object>> tools = buildToolsSchema();
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", messages);
            body.put("temperature", temperature);
            body.put("top_p", topP);
            if (maxTokens != null && maxTokens > 0)
                body.put("max_tokens", maxTokens);
            body.put("tools", tools);
            body.put("tool_choice", "auto");

            JsonNode firstResp = baiduAiService.chatRaw(body);
            JsonNode choice0 = firstResp.path("choices").isArray() && firstResp.path("choices").size() > 0
                    ? firstResp.path("choices").get(0)
                    : null;
            JsonNode messageNode = choice0 == null ? null : choice0.path("message");
            JsonNode toolCalls = messageNode == null ? null : messageNode.path("tool_calls");

            if (toolCalls != null && toolCalls.isArray() && toolCalls.size() > 0) {
                // 执行工具
                // 先把 assistant(含 tool_calls) 也加入到对话，符合工具调用协议
                List<Map<String, Object>> rawMessages = toRawMessages(messages);
                List<Map<String, Object>> assistantToolCalls = new ArrayList<>();
                for (JsonNode call : toolCalls) {
                    String callId = call.path("id").asText(UUID.randomUUID().toString());
                    JsonNode fn = call.path("function");
                    String name = fn.path("name").asText("");
                    String argsJson = fn.path("arguments").asText("{}");
                    String toolResult = executeTool(name, argsJson);
                    Map<String, String> toolMsg = new HashMap<>();
                    toolMsg.put("role", "tool");
                    toolMsg.put("content", toolResult);
                    toolMsg.put("tool_call_id", callId);
                    toolMsg.put("name", name);
                    messages.add(toolMsg);
                    // 记录 assistant 的 tool_call 条目
                    Map<String, Object> toolCallEntry = new HashMap<>();
                    toolCallEntry.put("id", callId);
                    toolCallEntry.put("type", "function");
                    toolCallEntry.put("function", Map.of("name", name, "arguments", argsJson));
                    assistantToolCalls.add(toolCallEntry);
                }
                // 把 assistant(tool_calls) 消息加入原始对话结构
                rawMessages.add(Map.of("role", "assistant", "content", "", "tool_calls", assistantToolCalls));
                // 再把每条 tool 结果加入原始结构
                for (Map<String, String> m : messages) {
                    if ("tool".equals(m.get("role"))) {
                        Map<String, Object> toolObj = new HashMap<>();
                        toolObj.put("role", "tool");
                        toolObj.put("content", m.get("content"));
                        toolObj.put("tool_call_id", m.get("tool_call_id"));
                        toolObj.put("name", m.get("name"));
                        rawMessages.add(toolObj);
                    }
                }
                // 带上工具结果再问一次模型，得到最终自然语言回答
                Map<String, Object> second = new HashMap<>();
                second.put("model", model);
                second.put("messages", rawMessages);
                second.put("temperature", temperature);
                second.put("top_p", topP);
                if (maxTokens != null && maxTokens > 0)
                    second.put("max_tokens", maxTokens);
                // 可继续附上 tools 以允许递归调用，这里为防复杂度，暂不再附
                JsonNode secondResp = baiduAiService.chatRaw(second);
                String reply = extractAssistantContent(secondResp);
                chatSessionService.saveMessage(sessionId, "assistant", reply);
                return Map.of("reply", reply, "sources", sources);
            } else {
                // 无工具调用，回退到直接对话
                String reply = extractAssistantContent(firstResp);
                if (reply == null || reply.isEmpty()) {
                    reply = baiduAiService.chatWithMessages(model, messages, temperature, topP, maxTokens);
                }
                chatSessionService.saveMessage(sessionId, "assistant", reply);
                return Map.of("reply", reply, "sources", sources);
            }
        } catch (RuntimeException e) {
            String msgShort = e.getMessage() == null ? "AI 服务错误" : e.getMessage();
            System.err.println("[AiController] AI call failed: " + msgShort);
            return Map.of("reply", "", "error", msgShort);
        }
    }

    private List<Map<String, Object>> buildToolsSchema() {
        List<Map<String, Object>> tools = new ArrayList<>();
        // 1) 书目查询：按书名模糊搜索
        tools.add(Map.of(
                "type", "function",
                "function", Map.of(
                        "name", "find_books_by_title",
                        "description", "按书名模糊搜索图书，返回匹配的图书列表",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "title", Map.of("type", "string", "description", "要查询的书名关键字")),
                                "required", List.of("title")))));
        // 1.1) 书目查询：按 ISBN 模糊搜索
        tools.add(Map.of(
                "type", "function",
                "function", Map.of(
                        "name", "find_books_by_isbn",
                        "description", "按ISBN模糊搜索图书",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "isbn", Map.of("type", "string", "description", "书籍ISBN关键字")),
                                "required", List.of("isbn")))));
        // 2) 查询读者信息
        tools.add(Map.of(
                "type", "function",
                "function", Map.of(
                        "name", "get_reader_info",
                        "description", "根据读者ID查询读者信息",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "reader_id", Map.of("type", "integer", "description", "读者ID")),
                                "required", List.of("reader_id")))));
        // 2.1) 按读者姓名模糊查找
        tools.add(Map.of(
                "type", "function",
                "function", Map.of(
                        "name", "find_reader_by_name",
                        "description", "按姓名模糊查找读者",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "name", Map.of("type", "string", "description", "读者姓名关键字")),
                                "required", List.of("name")))));
        // 3) 查询某读者借阅记录
        tools.add(Map.of(
                "type", "function",
                "function", Map.of(
                        "name", "get_reader_records",
                        "description", "根据读者ID查询借阅记录",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "reader_id", Map.of("type", "integer", "description", "读者ID")),
                                "required", List.of("reader_id")))));
        // 3.1) 统计读者当前借阅数量
        tools.add(Map.of(
                "type", "function",
                "function", Map.of(
                        "name", "count_borrowed_by_reader",
                        "description", "统计指定读者的借阅记录数",
                        "parameters", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "reader_id", Map.of("type", "integer", "description", "读者ID")),
                                "required", List.of("reader_id")))));
        return tools;
    }

    private String executeTool(String name, String argsJson) {
        try {
            // 简单解析参数
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> args = om.readValue(argsJson == null || argsJson.isBlank() ? "{}" : argsJson,
                    Map.class);
            switch (name) {
                case "find_books_by_title": {
                    String title = Objects.toString(args.get("title"), "");
                    List<Book> list = bookService.searchBooks(title);
                    return om.writeValueAsString(list);
                }
                case "find_books_by_isbn": {
                    String isbn = Objects.toString(args.get("isbn"), "");
                    List<Book> list = bookService.searchBooksByIsbn(isbn);
                    return om.writeValueAsString(list);
                }
                case "get_reader_info": {
                    Long readerId = args.get("reader_id") == null ? null
                            : Long.valueOf(args.get("reader_id").toString());
                    Reader reader = readerService.getReaderById(readerId);
                    return om.writeValueAsString(reader);
                }
                case "find_reader_by_name": {
                    String nameKey = Objects.toString(args.get("name"), "");
                    List<Reader> readers = readerService.searchReadersByName(nameKey);
                    return om.writeValueAsString(readers);
                }
                case "get_reader_records": {
                    Long readerId = args.get("reader_id") == null ? null
                            : Long.valueOf(args.get("reader_id").toString());
                    List<Record> records = recordService.getRecordsByReaderId(readerId);
                    return om.writeValueAsString(records);
                }
                case "count_borrowed_by_reader": {
                    Long readerId = args.get("reader_id") == null ? null
                            : Long.valueOf(args.get("reader_id").toString());
                    List<Record> records = recordService.getRecordsByReaderId(readerId);
                    return String.valueOf(records == null ? 0 : records.size());
                }
                default:
                    return "{}";
            }
        } catch (Exception e) {
            return "{\"error\":\"tool execution failed: " + e.getMessage().replace("\"", "'") + "\"}";
        }
    }

    private String extractAssistantContent(JsonNode root) {
        if (root == null)
            return null;
        if (root.has("choices") && root.get("choices").isArray() && root.get("choices").size() > 0) {
            JsonNode first = root.get("choices").get(0);
            JsonNode messageNode = first.path("message").path("content");
            if (messageNode.isTextual())
                return messageNode.asText();
            if (!messageNode.isMissingNode())
                return messageNode.toString();
        }
        if (root.has("result"))
            return root.path("result").asText("");
        return null;
    }

    private List<Map<String, Object>> toRawMessages(List<Map<String, String>> simple) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, String> m : simple) {
            Map<String, Object> o = new HashMap<>();
            o.put("role", m.get("role"));
            o.put("content", m.get("content"));
            out.add(o);
        }
        return out;
    }

    private String buildPolicySystemPrompt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = false;
        boolean isUser = false;
        if (auth != null && auth.getAuthorities() != null) {
            for (GrantedAuthority ga : auth.getAuthorities()) {
                String a = ga.getAuthority();
                if ("ROLE_ADMIN".equals(a))
                    isAdmin = true;
                if ("ROLE_USER".equals(a))
                    isUser = true;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("你是馆内系统的智能助理，可以使用工具访问后端受控数据以回答问题。\n");
        sb.append("当用户询问以下事项时，优先调用相应工具，而不是凭空回答：\n");
        sb.append("- 书目可用性与详情：使用 find_books_by_title\n");
        sb.append("- 读者信息与借阅记录：使用 get_reader_info / get_reader_records\n\n");

        if (isAdmin) {
            sb.append("当前用户角色：ADMIN。可调用所有工具（含读者信息、借阅记录）。\n");
        } else if (isUser) {
            sb.append("当前用户角色：USER。仅允许调用图书检索工具，禁止访问读者个人信息与借阅记录。\n");
        } else {
            sb.append("当前用户角色：访客。仅允许调用图书检索工具。\n");
        }

        sb.append("请严格遵守以上访问策略。若用户请求超出权限范围，请礼貌拒绝并给出可行替代方案。\n");
        sb.append("当需要实时数据时，一定要调用工具，不要臆测答案；拿到工具结果后再进行总结与自然语言表述。\n");
        return sb.toString();
    }

    // 参数面板：读取当前会话设置
    @GetMapping("/settings")
    public SessionSettings getSettings(HttpSession session) {
        return sessionSettingsService.getOrCreate(session);
    }

    // 参数面板：更新当前会话设置（body JSON 可包含 model/temperature/top_p/max_tokens）
    @PostMapping("/settings")
    public Map<String, Object> updateSettings(@RequestBody SessionSettings settings, HttpSession session) {
        SessionSettings current = sessionSettingsService.getOrCreate(session);
        SessionSettings merged = sessionSettingsService.toEffective(current, settings);
        sessionSettingsService.save(session, merged);
        return Map.of("ok", true, "settings", merged);
    }

    // 重置会话（清空历史并重建 sessionId）
    @PostMapping("/reset")
    public Map<String, Object> reset(HttpSession session) {
        String sessionId = (String) session.getAttribute("chatSessionId");
        if (sessionId != null) {
            chatSessionService.deleteSession(sessionId);
        }
        String newId = UUID.randomUUID().toString();
        session.setAttribute("chatSessionId", newId);
        return Map.of("ok", true, "sessionId", newId);
    }
}
