package com.fortn.bookmanager.controller.admin;

import com.fortn.bookmanager.mapper.DocumentMapper;
import com.fortn.bookmanager.model.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/document")
public class AdminDocumentController {

    @Autowired
    private DocumentMapper documentMapper;

    @RequestMapping("/toAddPage")
    public String toAddPage(Model model) {
        return "admin/document_add";
    }

    @RequestMapping("/add")
    public String add(Document doc) {
        if (doc.getCreatedAt() == null) {
            doc.setCreatedAt(LocalDateTime.now());
        }
        documentMapper.insert(doc);
        return "redirect:/index";
    }
}
