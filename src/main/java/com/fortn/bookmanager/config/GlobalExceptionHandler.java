package com.fortn.bookmanager.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Object handleException(HttpServletRequest req, Exception e) {
        // 记录堆栈（开发时可打开）
        System.err.println("[GlobalExceptionHandler] error on " + req.getRequestURI() + " : " + e.getMessage());
        e.printStackTrace();

        if (isApiRequest(req)) {
            // 返回 JSON，避免 HTML 被前端当作 JSON 解析
            Map<String, Object> body = Map.of(
                    "error", "internal_error",
                    "message", "服务内部错误，请查看后端日志");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(body, headers, HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            // 原来的页面错误处理
            ModelAndView mv = new ModelAndView("error");
            mv.addObject("message", e.getMessage());
            return mv;
        }
    }

    private boolean isApiRequest(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String accept = req.getHeader("Accept");
        String xRequested = req.getHeader("X-Requested-With");

        if (uri != null && uri.startsWith("/api/"))
            return true;
        if (xRequested != null && "XMLHttpRequest".equalsIgnoreCase(xRequested))
            return true;
        if (accept != null && accept.contains("application/json"))
            return true;
        return false;
    }
}