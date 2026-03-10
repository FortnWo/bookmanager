package com.fortn.bookmanager.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fortn.bookmanager.mapper.ReaderMapper;
import com.fortn.bookmanager.pojo.Reader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class UserSessionInterceptor implements HandlerInterceptor {

    @Autowired
    private ReaderMapper readerMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            if (session.getAttribute("name") == null) {
                String name = "管理员";
                if (!username.equals("admin")) {
                    Reader reader = readerMapper.selectOne(
                            new QueryWrapper<Reader>().eq("reader_id", username));
                    name = (reader != null) ? reader.getName() : "";
                }
                session.setAttribute("name", name);
                session.setAttribute("username", username);
            }
        }
        return true;
    }
}