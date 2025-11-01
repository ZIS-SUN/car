package com.carmaintenance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carmaintenance.entity.Announcement;
import com.carmaintenance.mapper.AnnouncementMapper;
import com.carmaintenance.service.AnnouncementService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告服务实现
 */
@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    @Override
    public Page<Announcement> getAnnouncementPage(Integer current, Integer size, String keyword, Integer status, Integer type) {
        Page<Announcement> page = new Page<>(current, size);

        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();

        // 关键字搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Announcement::getTitle, keyword)
                    .or()
                    .like(Announcement::getContent, keyword));
        }

        // 状态筛选
        if (status != null) {
            wrapper.eq(Announcement::getStatus, status);
        }

        // 类型筛选
        if (type != null) {
            wrapper.eq(Announcement::getType, type);
        }

        // 按优先级和创建时间排序
        wrapper.orderByDesc(Announcement::getPriority)
                .orderByDesc(Announcement::getCreateTime);

        return this.page(page, wrapper);
    }

    @Override
    public List<Announcement> getActiveAnnouncements() {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        LocalDateTime now = LocalDateTime.now();

        wrapper.eq(Announcement::getStatus, 1) // 状态为发布
                .and(w -> w.isNull(Announcement::getStartTime)
                        .or()
                        .le(Announcement::getStartTime, now)) // 开始时间为空或已到
                .and(w -> w.isNull(Announcement::getEndTime)
                        .or()
                        .ge(Announcement::getEndTime, now)) // 结束时间为空或未到
                .orderByDesc(Announcement::getPriority)
                .orderByDesc(Announcement::getCreateTime)
                .last("LIMIT 10"); // 最多返回10条

        return this.list(wrapper);
    }

    @Override
    public boolean createAnnouncement(Announcement announcement, Long userId) {
        announcement.setCreatedBy(userId);
        announcement.setCreateTime(LocalDateTime.now());
        return this.save(announcement);
    }

    @Override
    public boolean updateAnnouncement(Announcement announcement) {
        announcement.setUpdateTime(LocalDateTime.now());
        return this.updateById(announcement);
    }

    @Override
    public boolean deleteAnnouncement(Long id) {
        return this.removeById(id);
    }

    @Override
    public boolean publishAnnouncement(Long id) {
        Announcement announcement = new Announcement();
        announcement.setId(id);
        announcement.setStatus(1);
        announcement.setUpdateTime(LocalDateTime.now());
        return this.updateById(announcement);
    }

    @Override
    public boolean offlineAnnouncement(Long id) {
        Announcement announcement = new Announcement();
        announcement.setId(id);
        announcement.setStatus(2);
        announcement.setUpdateTime(LocalDateTime.now());
        return this.updateById(announcement);
    }
}
