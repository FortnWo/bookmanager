package com.fortn.bookmanager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fortn.bookmanager.mapper.BookMapper;
import com.fortn.bookmanager.pojo.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {
    @Autowired
    private BookMapper bookMapper;

    @Cacheable("books")
    public List<Book> getAllBooks() {
        return bookMapper.selectList(null);
    }

    public Book getBookById(Long id) {
        return bookMapper.selectById(id);
    }

    public void updateBook(Book book) {
        bookMapper.updateById(book);
    }

    public void deleteBook(Long id) {
        bookMapper.deleteById(id);
    }

    public void addBook(Book book) {
        bookMapper.insert(book);
    }

    public List<Book> searchBooks(String keyword) {
        return bookMapper.selectList(
                new QueryWrapper<Book>()
                        .like("name", keyword)
                        .or().like("publish", keyword)
                        .or().like("author", keyword));
    }

    public List<Book> searchBooksByIsbn(String isbn) {
        return bookMapper.selectList(
                new QueryWrapper<Book>()
                        .like("isbn", isbn));
    }
}