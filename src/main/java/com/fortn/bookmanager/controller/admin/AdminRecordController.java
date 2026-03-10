package com.fortn.bookmanager.controller.admin;

import com.fortn.bookmanager.service.RecordService;
import com.fortn.bookmanager.pojo.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin/record")
public class AdminRecordController {

    @Autowired
    private RecordService recordService;

    @RequestMapping("/getAll")
    public String getAll(Model model) {
        List<Record> records = recordService.getAllRecords();
        model.addAttribute("records", records);
        return "admin/records";
    }

    @RequestMapping("/search")
    public String search(@RequestParam(value = "readerId", required = false) String readerId,
            @RequestParam(value = "bookId", required = false) String bookId,
            Model model) {
        List<Record> records = recordService.searchRecords(readerId, bookId);
        model.addAttribute("records", records);
        return "admin/records";
    }

    @RequestMapping("/return/{sernum}")
    public String returnBook(@PathVariable("sernum") long sernum) {
        recordService.returnBook(sernum);
        return "redirect:/admin/record/getAll";
    }

    @RequestMapping("/searchByBookName")
    public String searchByBookName(@RequestParam(value = "readerId", required = false) String readerId,
            @RequestParam(value = "bookName", required = false) String bookName,
            Model model) {
        List<Record> records = recordService.searchByBookName(readerId, bookName);
        model.addAttribute("records", records);
        return "admin/records";
    }
}
