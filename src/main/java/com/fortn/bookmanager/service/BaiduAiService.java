package com.fortn.bookmanager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BaiduAiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${baidu.ai.apiKey}")
    private String apiKey;

    @Value("${baidu.ernie.url:https://qianfan.baidubce.com/v2/chat/completions}")
    private String inferUrl;

    public BaiduAiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * messages发送请求，返回模型文本回复（choices[0].message.content）
     * 支持可选参数：temperature, top_p, max_tokens
     */
    public String chatWithMessages(String model, List<Map<String, String>> messages, double temperature, double topP,
            Integer maxTokens) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", messages);
            body.put("temperature", temperature);
            body.put("top_p", topP);
            if (maxTokens != null && maxTokens > 0) {
                body.put("max_tokens", maxTokens);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);
            ResponseEntity<String> resp = restTemplate.postForEntity(inferUrl, req, String.class);

            HttpStatus status = resp.getStatusCode();
            String respBody = resp.getBody() == null ? "" : resp.getBody();
            MediaType respType = resp.getHeaders().getContentType();

            if (!status.is2xxSuccessful()) {
                String snippet = respBody.length() > 1000 ? respBody.substring(0, 1000) : respBody;
                System.err.println("[BaiduAiService] Non-2xx response from inferUrl. status=" + status.value()
                        + " content-type=" + respType + " body-snippet=" + snippet.replaceAll("\\s+", " ").trim());
                throw new RuntimeException("AI 服务返回HTTP " + status.value());
            }

            if (respType == null || !respType.includes(MediaType.APPLICATION_JSON)) {
                String snippet = respBody.length() > 500 ? respBody.substring(0, 500) : respBody;
                System.err.println("[BaiduAiService] Unexpected content-type from AI service: " + respType
                        + " body-snippet=" + snippet.replaceAll("\\s+", " ").trim());
                throw new RuntimeException("AI 服务返回非 JSON 内容（可能被代理/重定向），请检查网络或 endpoint 配置");
            }

            JsonNode root = mapper.readTree(respBody);
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
            return root.toString();
        } catch (RuntimeException re) {
            System.err.println("[BaiduAiService] RuntimeException: " + re.getMessage());
            throw re;
        } catch (Exception e) {
            System.err.println("[BaiduAiService] Exception while calling AI: " + e.getMessage());
            throw new RuntimeException("调用 AI 服务失败，请查看后端日志", e);
        }
    }

    /**
     * 通用 chat 调用：可传 tools / tool_choice 等扩展参数，返回完整 JSON
     */
    public JsonNode chatRaw(Map<String, Object> body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);
            ResponseEntity<String> resp = restTemplate.postForEntity(inferUrl, req, String.class);

            HttpStatus status = resp.getStatusCode();
            String respBody = resp.getBody() == null ? "" : resp.getBody();
            MediaType respType = resp.getHeaders().getContentType();

            if (!status.is2xxSuccessful()) {
                String snippet = respBody.length() > 1000 ? respBody.substring(0, 1000) : respBody;
                System.err.println("[BaiduAiService] Non-2xx response from inferUrl. status=" + status.value()
                        + " content-type=" + respType + " body-snippet=" + snippet.replaceAll("\\s+", " ").trim());
                throw new RuntimeException("AI 服务返回HTTP " + status.value());
            }

            if (respType == null || !respType.includes(MediaType.APPLICATION_JSON)) {
                String snippet = respBody.length() > 500 ? respBody.substring(0, 500) : respBody;
                System.err.println("[BaiduAiService] Unexpected content-type from AI service: " + respType
                        + " body-snippet=" + snippet.replaceAll("\\s+", " ").trim());
                throw new RuntimeException("AI 服务返回非 JSON 内容（可能被代理/重定向），请检查网络或 endpoint 配置");
            }

            return mapper.readTree(respBody);
        } catch (RuntimeException re) {
            System.err.println("[BaiduAiService] RuntimeException: " + re.getMessage());
            throw re;
        } catch (Exception e) {
            System.err.println("[BaiduAiService] Exception while calling AI(raw): " + e.getMessage());
            throw new RuntimeException("调用 AI 服务失败，请查看后端日志", e);
        }
    }
}
