package com.fortn.bookmanager.service;

import com.fortn.bookmanager.model.SessionSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

@Service
public class SessionSettingsService {

    private static final String SESSION_SETTINGS_KEY = "sessionSettings";

    @Value("${baidu.ai.model:ernie-speed-128k}")
    private String defaultModel;

    @Value("${baidu.ai.temperature:0.8}")
    private Double defaultTemperature;

    @Value("${baidu.ai.top_p:0.9}")
    private Double defaultTopP;

    @Value("${baidu.ai.max-tokens:1024}")
    private Integer defaultMaxTokens;

    public SessionSettings getOrCreate(HttpSession session) {
        SessionSettings s = (SessionSettings) session.getAttribute(SESSION_SETTINGS_KEY);
        if (s == null) {
            s = new SessionSettings(defaultModel, defaultTemperature, defaultTopP, defaultMaxTokens);
            session.setAttribute(SESSION_SETTINGS_KEY, s);
        }
        return s;
    }

    public void save(HttpSession session, SessionSettings settings) {
        if (settings == null)
            return;
        session.setAttribute(SESSION_SETTINGS_KEY, settings);
    }

    public SessionSettings toEffective(SessionSettings sessionSettings, SessionSettings override) {
        if (sessionSettings == null)
            return override;
        if (override == null)
            return sessionSettings;
        String model = override.getModel() != null ? override.getModel() : sessionSettings.getModel();
        Double temperature = override.getTemperature() != null ? override.getTemperature()
                : sessionSettings.getTemperature();
        Double topP = override.getTopP() != null ? override.getTopP() : sessionSettings.getTopP();
        Integer maxTokens = override.getMaxTokens() != null ? override.getMaxTokens() : sessionSettings.getMaxTokens();
        return new SessionSettings(model, temperature, topP, maxTokens);
    }
}