package com.fortn.bookmanager.controller;

import com.fortn.bookmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("/toChPwdPage")
    public String toChangePasswordPage() {
        return "password";
    }

    @RequestMapping("/changePassword")
    public String changePassword(HttpSession session, String newPassword) {
        String username = (String) session.getAttribute("username");
        userService.changePassword(username, newPassword);
        return "redirect:/";
    }

    @RequestMapping("/checkPassword")
    @ResponseBody
    public String checkPassword(@RequestParam("password") String password, HttpSession session) {
        String username = (String) session.getAttribute("username");
        boolean ok = userService.checkPassword(username, password);
        return ok ? "1" : "0";
    }
}
