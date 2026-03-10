package com.fortn.bookmanager.controller;

import com.fortn.bookmanager.pojo.Announcement;
import com.fortn.bookmanager.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private AnnouncementService announcementService;

    @RequestMapping({ "/", "/index" })
    public String index(Model model) {
        Announcement latestAnnouncement = announcementService.getLatestAnnouncement();
        model.addAttribute("announcement", latestAnnouncement);
        return "index";
    }

    @RequestMapping("/toLoginPage")
    public String toLoginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("loginError", "用户名或密码错误！");
        }
        return "login";
    }

    @RequestMapping("/logoutSuccess")
    public String logoutSuccess() {
        return "login";
    }
}