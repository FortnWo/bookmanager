package com.fortn.bookmanager.controller.admin;

import com.fortn.bookmanager.service.AnnouncementService;
import com.fortn.bookmanager.pojo.Announcement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @GetMapping("/edit")
    public String editAnnouncementPage(Model model) {
        Announcement latestAnnouncement = announcementService.getLatestAnnouncement();
        model.addAttribute("announcement", latestAnnouncement);
        return "announcement_edit";
    }

    @PostMapping("/publish")
    public String publishAnnouncement(@RequestParam String content) {
        announcementService.publishAnnouncement(content);
        return "redirect:/";
    }
}