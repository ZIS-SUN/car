package com.carmaintenance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.carmaintenance.entity.UserMember;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 用户会员Service接口
 */
public interface UserMemberService extends IService<UserMember> {

    /**
     * 初始化用户会员信息
     */
    String initUserMember(Long userId);

    /**
     * 增加用户经验值
     */
    String addExperience(Long userId, Integer experience, Integer experienceType, Long orderId, String reason);

    /**
     * 扣减用户经验值
     */
    String deductExperience(Long userId, Integer experience, String reason);

    /**
     * 手动调整经验值
     */
    String adjustExperience(Long userId, Integer experience, String reason);

    /**
     * 获取用户会员信息
     */
    Map<String, Object> getUserMemberInfo(Long userId);

    /**
     * 更新用户等级
     */
    boolean updateUserLevel(Long userId, Integer newLevelId);

    /**
     * 计算用户折扣
     */
    BigDecimal calculateUserDiscount(Long userId, BigDecimal originalAmount);

    /**
     * 获取用户经验记录
     */
    Map<String, Object> getUserExperienceRecords(Long userId, Integer page, Integer size);

    /**
     * 检查用户是否可以升级
     */
    Map<String, Object> checkUserUpgrade(Long userId);

    /**
     * 订单完成后自动添加经验值
     */
    void addExperienceForOrder(Long userId, BigDecimal orderAmount, Long orderId);
}