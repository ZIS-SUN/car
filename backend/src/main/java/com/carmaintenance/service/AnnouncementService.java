package com.carmaintenance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.carmaintenance.entity.Announcement;

import java.util.List;

/**
 * 公告服务接口
 */
public interface AnnouncementService extends IService<Announcement> {

    /**
     * 分页查询公告（管理后台）
     */
    Page<Announcement> getAnnouncementPage(Integer current, Integer size, String keyword, Integer status, Integer type);

    /**
     * 获取有效的公告列表（用户端）
     */
    List<Announcement> getActiveAnnouncements();

    /**
     * 创建公告
     */
    boolean createAnnouncement(Announcement announcement, Long userId);

    /**
     * 更新公告
     */
    boolean updateAnnouncement(Announcement announcement);

    /**
     * 删除公告
     */
    boolean deleteAnnouncement(Long id);

    /**
     * 发布公告
     */
    boolean publishAnnouncement(Long id);

    /**
     * 下线公告
     */
    boolean offlineAnnouncement(Long id);
}
