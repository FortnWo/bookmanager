package com.fortn.bookmanager.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fortn.bookmanager.mapper.AnnouncementMapper;
import com.fortn.bookmanager.pojo.Announcement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class AnnouncementService {
    @Autowired
    private AnnouncementMapper announcementMapper;

    @Cacheable("latestAnnouncement")
    public Announcement getLatestAnnouncement() {
        Announcement latest = announcementMapper.selectOne(
                new QueryWrapper<Announcement>().orderByDesc("id").last("LIMIT 1"));
        if (latest == null) {
            latest = new Announcement();
            latest.setContent("暂无公告");
        }
        return latest;
    }

    // 发布后清除缓存，这样下一次 getLatestAnnouncement() 会重新从 DB 读取
    @CacheEvict(value = "latestAnnouncement", allEntries = true)
    public void publishAnnouncement(String content) {
        Announcement announcement = new Announcement();
        announcement.setContent(content);
        announcementMapper.insert(announcement);
    }
}