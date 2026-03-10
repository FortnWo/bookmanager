package com.fortn.bookmanager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fortn.bookmanager.mapper.RecordMapper;
import com.fortn.bookmanager.pojo.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class RecordService {
    @Autowired
    private RecordMapper recordMapper;

    @Cacheable("records")
    public List<Record> getAllRecords() {
        return recordMapper.selectList(null);
    }

    @Cacheable(value = "recordsByCondition", key = "#readerId + '-' + #bookId")
    public List<Record> searchRecords(String readerId, String bookId) {
        QueryWrapper<Record> wrapper = new QueryWrapper<>();
        if (readerId != null && !readerId.isEmpty()) {
            wrapper.eq("reader_id", readerId);
        }
        if (bookId != null && !bookId.isEmpty()) {
            wrapper.eq("book_id", bookId);
        }
        return recordMapper.selectList(wrapper);
    }

    @Cacheable(value = "recordsByBookName", key = "#readerId + '-' + #bookName")
    public List<Record> searchByBookName(String readerId, String bookName) {
        QueryWrapper<Record> wrapper = new QueryWrapper<>();
        if (readerId != null && !readerId.isEmpty()) {
            wrapper.eq("reader_id", readerId);
        }
        if (bookName != null && !bookName.isEmpty()) {
            wrapper.like("book_name", bookName);
        }
        return recordMapper.selectList(wrapper);
    }

    @Cacheable(value = "recordsByReaderId", key = "#readerId")
    public List<Record> getRecordsByReaderId(Long readerId) {
        return recordMapper.selectList(
                new QueryWrapper<Record>().eq("reader_id", readerId));
    }

    public void returnBook(long sernum) {
        Record record = recordMapper.selectById(sernum);
        if (record != null && record.getBackDate() == null) {
            record.setBackDate(new Date());
            recordMapper.updateById(record);
        }
    }

    public void addRecord(Record record) {
        recordMapper.insert(record);
    }
}