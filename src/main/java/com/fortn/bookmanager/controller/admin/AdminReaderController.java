package com.fortn.bookmanager.controller.admin;

import com.fortn.bookmanager.service.ReaderService;
import com.fortn.bookmanager.pojo.Reader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin/reader")
public class AdminReaderController {

    @Autowired
    private ReaderService readerService;

    @RequestMapping("/getAll")
    public String getAll(Model model) {
        List<Reader> readers = readerService.getAllReaders();
        model.addAttribute("readers", readers);
        return "admin/readers";
    }

    @RequestMapping("/toAddPage")
    public String toAddPage(Model model, @RequestParam(value = "readerId", required = false) String readerId) {
        model.addAttribute("readerId", readerId);
        return "admin/reader_add";
    }

    @RequestMapping("/add")
    public String add(Reader reader, Model model) {
        Reader exist = readerService.getReaderById(reader.getReaderId());
        if (exist != null) {
            model.addAttribute("error", "该用户个人信息已存在，请勿重复添加！");
            return "admin/reader_add";
        }
        readerService.addReader(reader);
        return "redirect:/admin/reader/getAll";
    }

    @RequestMapping("/delete/{id}")
    public String delete(@PathVariable("id") long id) {
        readerService.deleteReader(id);
        return "redirect:/admin/reader/getAll";
    }

    @RequestMapping("/toEditPage/{id}")
    public String toEditPage(Model model, @PathVariable("id") String id) {
        Reader reader = readerService.getReaderById(Long.valueOf(id));
        model.addAttribute("reader", reader);
        return "admin/reader_edit";
    }

    @RequestMapping("/update")
    public String update(Reader reader) {
        readerService.updateReader(reader);
        return "redirect:/admin/reader/getAll";
    }
}
