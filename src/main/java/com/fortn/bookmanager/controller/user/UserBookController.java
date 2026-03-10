package com.fortn.bookmanager.controller.user;

import com.fortn.bookmanager.pojo.Book;
import com.fortn.bookmanager.pojo.Record;
import com.fortn.bookmanager.service.BookService;
import com.fortn.bookmanager.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/user/book")
public class UserBookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private RecordService recordService;

    @RequestMapping("/getAll")
    public String getAll(Model model) {
        List<Book> books = bookService.getAllBooks();
        model.addAttribute("books", books);
        return "user/books";
    }

    @RequestMapping("/info/{id}")
    public String info(Model model, @PathVariable("id") Long id) {
        Book book = bookService.getBookById(id);
        model.addAttribute("book", book);
        return "user/book_info";
    }

    @RequestMapping("/search")
    public String search(String keyword, Model model) {
        List<Book> books = bookService.searchBooks(keyword);
        model.addAttribute("books", books);
        return "user/book_search_result";
    }

    @RequestMapping("/borrow/{bookId}")
    public String borrowBook(@PathVariable("bookId") long bookId, HttpSession session) {
        String readerId = (String) session.getAttribute("username");
        Record record = new Record();
        record.setBookId(bookId);
        record.setReaderId(Long.parseLong(readerId));
        record.setLendDate(new Date());
        recordService.addRecord(record);
        return "redirect:/user/book/getAll";
    }
}
