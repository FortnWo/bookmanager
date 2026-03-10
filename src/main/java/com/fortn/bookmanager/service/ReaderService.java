package com.fortn.bookmanager.service;

import com.fortn.bookmanager.mapper.ReaderMapper;
import com.fortn.bookmanager.pojo.Reader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReaderService {
    @Autowired
    private ReaderMapper readerMapper;

    @Cacheable("readers")
    public List<Reader> getAllReaders() {
        return readerMapper.selectList(null);
    }

    @Cacheable(value = "reader", key = "#readerId")
    public Reader getReaderById(Long readerId) {
        return readerMapper.selectById(readerId);
    }

    public void addReader(Reader reader) {
        readerMapper.insert(reader);
    }

    public void updateReader(Reader reader) {
        readerMapper.updateById(reader);
    }

    public void deleteReader(Long id) {
        readerMapper.deleteById(id);
    }

    public List<Reader> searchReadersByName(String name) {
        return readerMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Reader>()
                .like("name", name));
    }
}