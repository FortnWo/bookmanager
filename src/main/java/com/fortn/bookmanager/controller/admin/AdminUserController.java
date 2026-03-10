package com.fortn.bookmanager.controller.admin;

import com.fortn.bookmanager.service.UserService;
import com.fortn.bookmanager.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/user")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @RequestMapping("/toAddPage")
    public String toAddPage() {
        return "admin/user_add";
    }

    @RequestMapping("/add")
    public String add(User user, Model model) {
        // 检查用户名是否已存在
        User exist = userService.getUserByUsername(user.getUsername());
        if (exist != null) {
            model.addAttribute("error", "该卡号已存在，请勿重复添加！");
            return "admin/user_add";
        }
        if (user.getRole() == null)
            user.setRole("READER");
        userService.addUser(user);
        return "redirect:/admin/reader/toAddPage?readerId=" + user.getUsername();
    }
}
