package com.carmaintenance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carmaintenance.entity.ExperienceRecord;
import com.carmaintenance.entity.MemberLevel;
import com.carmaintenance.entity.UserMember;
import com.carmaintenance.exception.BusinessException;
import com.carmaintenance.repository.ExperienceRecordRepository;
import com.carmaintenance.repository.UserMemberRepository;
import com.carmaintenance.service.MemberLevelService;
import com.carmaintenance.service.UserMemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户会员Service实现类
 */
@Service
public class UserMemberServiceImpl extends ServiceImpl<UserMemberRepository, UserMember> implements UserMemberService {

    private static final Logger logger = LoggerFactory.getLogger(UserMemberServiceImpl.class);

    @Autowired
    private MemberLevelService memberLevelService;

    @Autowired
    private ExperienceRecordRepository experienceRecordRepository;

    @Autowired
    private UserMemberRepository userMemberRepository;

    private static final int EXPERIENCE_PER_YUAN = 1; // 每消费1元获得1经验值

    @Override
    @Transactional
    public String initUserMember(Long userId) {
        // 检查用户是否已有会员信息
        if (userMemberRepository.countByUserId(userId) > 0) {
            return "用户会员信息已存在";
        }

        // 创建初始会员信息
        UserMember userMember = new UserMember();
        userMember.setUserId(userId);
        userMember.setLevelId(1); // 默认青铜会员
        userMember.setTotalExperience(0);
        userMember.setAvailableExperience(0);

        if (save(userMember)) {
            logger.info("用户会员信息初始化成功: userId={}", userId);
            return "会员信息初始化成功";
        } else {
            logger.error("用户会员信息初始化失败: userId={}", userId);
            throw new BusinessException(500, "会员信息初始化失败");
        }
    }

    @Override
    @Transactional
    public String addExperience(Long userId, Integer experience, Integer experienceType, Long orderId, String reason) {
        UserMember userMember = getUserMemberByUserId(userId);
        if (userMember == null) {
            initUserMember(userId);
            userMember = getUserMemberByUserId(userId);
        }

        // 更新经验值
        userMember.setTotalExperience(userMember.getTotalExperience() + experience);
        userMember.setAvailableExperience(userMember.getAvailableExperience() + experience);
        updateById(userMember);

        // 记录经验变化
        ExperienceRecord record = new ExperienceRecord();
        record.setUserId(userId);
        record.setOrderId(orderId);
        record.setExperienceChange(experience);
        record.setExperienceType(experienceType);
        record.setReason(reason);
        experienceRecordRepository.insert(record);

        // 检查并更新用户等级
        memberLevelService.checkAndUpdateUserLevel(userId);

        logger.info("用户经验值增加成功: userId={}, experience={}, totalExp={}", 
                   userId, experience, userMember.getTotalExperience());
        return "经验值增加成功";
    }

    @Override
    @Transactional
    public String deductExperience(Long userId, Integer experience, String reason) {
        UserMember userMember = getUserMemberByUserId(userId);
        if (userMember == null) {
            throw new BusinessException(404, "用户会员信息不存在");
        }

        if (userMember.getAvailableExperience() < experience) {
            logger.warn("用户经验值扣减失败: 可用经验值不足, userId={}, available={}, deduct={}", 
                       userId, userMember.getAvailableExperience(), experience);
            throw new BusinessException(400, "可用经验值不足");
        }

        // 扣减经验值
        userMember.setAvailableExperience(userMember.getAvailableExperience() - experience);
        updateById(userMember);

        // 记录经验变化
        ExperienceRecord record = new ExperienceRecord();
        record.setUserId(userId);
        record.setExperienceChange(-experience);
        record.setExperienceType(ExperienceRecord.ExperienceType.COMPLAINT.getCode());
        record.setReason(reason);
        experienceRecordRepository.insert(record);

        return "经验值扣减成功";
    }

    @Override
    @Transactional
    public String adjustExperience(Long userId, Integer experience, String reason) {
        UserMember userMember = getUserMemberByUserId(userId);
        if (userMember == null) {
            initUserMember(userId);
            userMember = getUserMemberByUserId(userId);
        }

        // 调整经验值
        userMember.setTotalExperience(userMember.getTotalExperience() + experience);
        userMember.setAvailableExperience(userMember.getAvailableExperience() + experience);
        updateById(userMember);

        // 记录经验变化
        ExperienceRecord record = new ExperienceRecord();
        record.setUserId(userId);
        record.setExperienceChange(experience);
        record.setExperienceType(ExperienceRecord.ExperienceType.MANUAL.getCode());
        record.setReason(reason);
        experienceRecordRepository.insert(record);

        // 检查并更新用户等级
        memberLevelService.checkAndUpdateUserLevel(userId);

        return "经验值调整成功";
    }

    @Override
    public Map<String, Object> getUserMemberInfo(Long userId) {
        UserMember userMember = getUserMemberByUserId(userId);
        if (userMember == null) {
            initUserMember(userId);
            userMember = getUserMemberByUserId(userId);
        }

        // 获取等级信息
        MemberLevel currentLevel = memberLevelService.getById(userMember.getLevelId());

        // 获取等级进度
        Map<String, Object> levelProgress = memberLevelService.getLevelProgress(userMember.getTotalExperience());

        Map<String, Object> result = new HashMap<>();
        result.put("userMember", userMember);
        result.put("currentLevel", currentLevel);
        result.put("levelProgress", levelProgress);

        return result;
    }

    @Override
    @Transactional
    public boolean updateUserLevel(Long userId, Integer newLevelId) {
        UserMember userMember = getUserMemberByUserId(userId);
        if (userMember == null) {
            throw new BusinessException(404, "用户会员信息不存在");
        }

        MemberLevel newLevel = memberLevelService.getById(newLevelId);
        if (newLevel == null) {
            throw new BusinessException(404, "目标等级不存在");
        }

        userMember.setLevelId(newLevelId);
        return updateById(userMember);
    }

    @Override
    public BigDecimal calculateUserDiscount(Long userId, BigDecimal originalAmount) {
        UserMember userMember = getUserMemberByUserId(userId);
        if (userMember == null) {
            return originalAmount; // 非会员无折扣
        }

        MemberLevel level = memberLevelService.getById(userMember.getLevelId());
        if (level == null || level.getDiscountRate() == null) {
            return originalAmount;
        }

        // 应用折扣
        BigDecimal discountedAmount = originalAmount.multiply(level.getDiscountRate())
                .setScale(2, RoundingMode.HALF_UP);

        return discountedAmount;
    }

    @Override
    public Map<String, Object> getUserExperienceRecords(Long userId, Integer page, Integer size) {
        // 获取经验记录
        Page<ExperienceRecord> recordPage = new Page<>(page != null ? page : 1, size != null ? size : 10);
        QueryWrapper<ExperienceRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");
        Page<ExperienceRecord> records = experienceRecordRepository.selectPage(recordPage, wrapper);

        // 获取用户会员信息
        UserMember userMember = getUserMemberByUserId(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("userMember", userMember);

        return result;
    }

    @Override
    public Map<String, Object> checkUserUpgrade(Long userId) {
        UserMember userMember = getUserMemberByUserId(userId);
        if (userMember == null) {
            throw new BusinessException(404, "用户会员信息不存在");
        }

        MemberLevel currentLevel = memberLevelService.getById(userMember.getLevelId());
        List<MemberLevel> allLevels = memberLevelService.getAllLevels();

        Map<String, Object> result = new HashMap<>();
        result.put("currentLevel", currentLevel);
        result.put("totalExperience", userMember.getTotalExperience());
        result.put("availableExperience", userMember.getAvailableExperience());

        // 检查是否可以升级
        MemberLevel shouldLevel = memberLevelService.getLevelByExperience(userMember.getTotalExperience());
        if (shouldLevel != null && !shouldLevel.getId().equals(currentLevel.getId())) {
            result.put("canUpgrade", true);
            result.put("nextLevel", shouldLevel);
        } else {
            result.put("canUpgrade", false);
        }

        return result;
    }

    /**
     * 根据用户ID获取用户会员信息
     */
    private UserMember getUserMemberByUserId(Long userId) {
        return userMemberRepository.findByUserId(userId);
    }

    /**
     * 订单完成后自动添加经验值
     */
    @Transactional
    public void addExperienceForOrder(Long userId, BigDecimal orderAmount, Long orderId) {
        int experience = orderAmount.intValue() * EXPERIENCE_PER_YUAN;
        if (experience > 0) {
            addExperience(userId, experience,
                    ExperienceRecord.ExperienceType.CONSUME.getCode(),
                    orderId,
                    "完成订单获得经验值");
        }
    }
}