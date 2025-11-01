package com.carmaintenance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.carmaintenance.entity.MemberLevel;

import java.util.List;
import java.util.Map;

/**
 * 会员等级Service接口
 */
public interface MemberLevelService extends IService<MemberLevel> {

    /**
     * 根据经验值获取会员等级
     */
    MemberLevel getLevelByExperience(Integer experience);

    /**
     * 获取所有会员等级
     */
    List<MemberLevel> getAllLevels();

    /**
     * 创建或更新会员等级
     */
    String saveMemberLevel(MemberLevel memberLevel);

    /**
     * 删除会员等级
     */
    boolean deleteMemberLevel(Integer levelId);

    /**
     * 获取等级升级进度
     */
    Map<String, Object> getLevelProgress(Integer currentExperience);

    /**
     * 检查并更新用户等级
     */
    Integer checkAndUpdateUserLevel(Long userId);

    /**
     * 获取会员等级统计信息
     */
    Map<String, Object> getMemberStats();
}