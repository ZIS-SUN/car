package com.carmaintenance.controller;

import com.carmaintenance.dto.Result;
import com.carmaintenance.entity.MemberLevel;
import com.carmaintenance.security.UserPrincipal;
import com.carmaintenance.service.MemberLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 会员等级控制器
 */
@RestController
@RequestMapping("/member-level")
public class MemberLevelController {

    @Autowired
    private MemberLevelService memberLevelService;

    /**
     * 创建会员等级（管理员）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> createMemberLevel(@Valid @RequestBody MemberLevel memberLevel) {
        String message = memberLevelService.saveMemberLevel(memberLevel);
        return Result.success(message);
    }

    /**
     * 更新会员等级（管理员）
     */
    @PutMapping("/{levelId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> updateMemberLevel(@PathVariable Integer levelId,
                                           @Valid @RequestBody MemberLevel memberLevel) {
        memberLevel.setId(levelId);
        String message = memberLevelService.saveMemberLevel(memberLevel);
        return Result.success(message);
    }

    /**
     * 删除会员等级（管理员）
     */
    @DeleteMapping("/{levelId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> deleteMemberLevel(@PathVariable Integer levelId) {
        if (memberLevelService.deleteMemberLevel(levelId)) {
            return Result.success("会员等级删除成功");
        } else {
            return Result.error("会员等级删除失败");
        }
    }

    /**
     * 获取所有会员等级
     */
    @GetMapping("/all")
    public Result<List<MemberLevel>> getAllLevels() {
        List<MemberLevel> levels = memberLevelService.getAllLevels();
        return Result.success(levels);
    }

    /**
     * 根据经验值获取会员等级
     */
    @GetMapping("/by-experience/{experience}")
    public Result<MemberLevel> getLevelByExperience(@PathVariable Integer experience) {
        MemberLevel level = memberLevelService.getLevelByExperience(experience);
        return Result.success(level);
    }

    /**
     * 获取等级升级进度
     */
    @GetMapping("/progress/{experience}")
    public Result<Map<String, Object>> getLevelProgress(@PathVariable Integer experience) {
        Map<String, Object> progress = memberLevelService.getLevelProgress(experience);
        return Result.success(progress);
    }

    /**
     * 检查并更新用户等级
     */
    @PutMapping("/check-upgrade/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Integer> checkAndUpdateUserLevel(@PathVariable Long userId) {
        Integer newLevelId = memberLevelService.checkAndUpdateUserLevel(userId);
        if (newLevelId != null) {
            return Result.success("用户等级检查完成", newLevelId);
        } else {
            return Result.error("用户不存在");
        }
    }

    /**
     * 获取会员统计信息（管理员）
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> getMemberStats() {
        Map<String, Object> stats = memberLevelService.getMemberStats();
        return Result.success(stats);
    }
}