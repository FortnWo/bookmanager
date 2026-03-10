package com.fortn.bookmanager.controller.admin;

import com.fortn.bookmanager.model.SessionSettings;
import com.fortn.bookmanager.service.SessionSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
public class AdminAiController {

    private final SessionSettingsService settingsService;

    public AdminAiController(SessionSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    private boolean isAdmin() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return a != null && a.getAuthorities().stream().anyMatch(g -> g.getAuthority().equals("ROLE_ADMIN"));
    }

    @GetMapping("/admin/ai/settings")
    public String adminSettingsPage(HttpSession session) {
        if (!isAdmin())
            return "error/403"; // 或重定向到登录/无权限页
        return "admin/ai_settings";
    }

    @GetMapping("/api/admin/ai/settings")
    @ResponseBody
    public ResponseEntity<?> getSettings(HttpSession session) {
        if (!isAdmin())
            return ResponseEntity.status(403).body(Map.of("error", "forbidden"));
        return ResponseEntity.ok(settingsService.getOrCreate(session));
    }

    @PostMapping("/api/admin/ai/settings")
    @ResponseBody
    public ResponseEntity<?> updateSettings(@RequestBody SessionSettings s, HttpSession session) {
        if (!isAdmin())
            return ResponseEntity.status(403).body(Map.of("error", "forbidden"));
        settingsService.save(session, s);
        // 记录日志（审计）
        System.out.println("[AdminAiController] admin updated AI settings: " + s);
        return ResponseEntity.ok(Map.of("ok", true, "settings", s));
    }
}