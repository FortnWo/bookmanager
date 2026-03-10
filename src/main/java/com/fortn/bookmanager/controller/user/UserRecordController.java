package com.fortn.bookmanager.controller.user;

import com.fortn.bookmanager.pojo.Record;
import com.fortn.bookmanager.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/user/record")
public class UserRecordController {

    @Autowired
    private RecordService recordService;

    @RequestMapping("/getOwnRecord")
    public String ownRecord(Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        List<Record> records = recordService.getRecordsByReaderId(Long.valueOf(username));
        model.addAttribute("records", records);
        return "user/ownRecord";
    }
}
