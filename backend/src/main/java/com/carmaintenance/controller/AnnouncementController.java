package com.carmaintenance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carmaintenance.dto.Result;
import com.carmaintenance.entity.Announcement;
import com.carmaintenance.security.UserPrincipal;
import com.carmaintenance.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告控制器
 */
@RestController
@RequestMapping("/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    /**
     * 获取有效公告列表（用户端）
     */
    @GetMapping("/active")
    public Result<List<Announcement>> getActiveAnnouncements() {
        List<Announcement> announcements = announcementService.getActiveAnnouncements();
        return Result.success(announcements);
    }

    /**
     * 分页查询公告（管理后台）
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<Announcement>> getAnnouncementList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer type) {
        Page<Announcement> page = announcementService.getAnnouncementPage(current, size, keyword, status, type);
        return Result.success(page);
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    public Result<Announcement> getAnnouncement(@PathVariable Long id) {
        Announcement announcement = announcementService.getById(id);
        if (announcement == null) {
            return Result.error("公告不存在");
        }
        return Result.success(announcement);
    }

    /**
     * 创建公告
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> createAnnouncement(@RequestBody Announcement announcement,
                                            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        boolean success = announcementService.createAnnouncement(announcement, userPrincipal.getUserId());
        if (success) {
            return Result.success("创建成功");
        } else {
            return Result.error("创建失败");
        }
    }

    /**
     * 更新公告
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> updateAnnouncement(@PathVariable Long id,
                                            @RequestBody Announcement announcement) {
        announcement.setId(id);
        boolean success = announcementService.updateAnnouncement(announcement);
        if (success) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> deleteAnnouncement(@PathVariable Long id) {
        boolean success = announcementService.deleteAnnouncement(id);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }

    /**
     * 发布公告
     */
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> publishAnnouncement(@PathVariable Long id) {
        boolean success = announcementService.publishAnnouncement(id);
        if (success) {
            return Result.success("发布成功");
        } else {
            return Result.error("发布失败");
        }
    }

    /**
     * 下线公告
     */
    @PutMapping("/{id}/offline")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> offlineAnnouncement(@PathVariable Long id) {
        boolean success = announcementService.offlineAnnouncement(id);
        if (success) {
            return Result.success("下线成功");
        } else {
            return Result.error("下线失败");
        }
    }
}
