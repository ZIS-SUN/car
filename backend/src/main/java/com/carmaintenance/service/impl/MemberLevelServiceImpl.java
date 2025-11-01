package com.carmaintenance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carmaintenance.entity.MemberLevel;
import com.carmaintenance.entity.UserMember;
import com.carmaintenance.exception.BusinessException;
import com.carmaintenance.repository.MemberLevelRepository;
import com.carmaintenance.repository.UserMemberRepository;
import com.carmaintenance.service.MemberLevelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会员等级Service实现类
 */
@Service
public class MemberLevelServiceImpl extends ServiceImpl<MemberLevelRepository, MemberLevel> implements MemberLevelService {

    private static final Logger logger = LoggerFactory.getLogger(MemberLevelServiceImpl.class);

    @Autowired
    private UserMemberRepository userMemberRepository;

    @Override
    public MemberLevel getLevelByExperience(Integer experience) {
        return baseMapper.getLevelByExperience(experience != null ? experience : 0);
    }

    @Override
    public List<MemberLevel> getAllLevels() {
        return baseMapper.getAllLevels();
    }

    @Override
    @Transactional
    public String saveMemberLevel(MemberLevel memberLevel) {
        if (memberLevel.getId() == null) {
            // 新增等级
            if (save(memberLevel)) {
                logger.info("会员等级创建成功: {}", memberLevel.getLevelName());
                return "会员等级创建成功";
            }
        } else {
            // 更新等级
            if (updateById(memberLevel)) {
                logger.info("会员等级更新成功: id={}, name={}", memberLevel.getId(), memberLevel.getLevelName());
                return "会员等级更新成功";
            }
        }
        logger.error("会员等级操作失败: {}", memberLevel);
        throw new BusinessException(500, "操作失败");
    }

    @Override
    @Transactional
    public boolean deleteMemberLevel(Integer levelId) {
        // 检查是否有用户使用该等级
        long userCount = userMemberRepository.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserMember>()
                        .eq("level_id", levelId));

        if (userCount > 0) {
            logger.warn("删除会员等级失败: 该等级下还有{}个用户，无法删除, levelId={}", userCount, levelId);
            throw new BusinessException(400, "该等级下还有用户，无法删除");
        }

        return removeById(levelId);
    }

    @Override
    public Map<String, Object> getLevelProgress(Integer currentExperience) {
        Map<String, Object> progress = new HashMap<>();

        MemberLevel currentLevel = getLevelByExperience(currentExperience);
        List<MemberLevel> allLevels = getAllLevels();

        progress.put("currentLevel", currentLevel);
        progress.put("currentExperience", currentExperience);

        // 计算到下一等级的进度
        if (currentLevel != null && allLevels.size() > 1) {
            MemberLevel nextLevel = null;
            for (int i = 0; i < allLevels.size() - 1; i++) {
                if (allLevels.get(i).getId().equals(currentLevel.getId())) {
                    nextLevel = allLevels.get(i + 1);
                    break;
                }
            }

            if (nextLevel != null) {
                int currentLevelMinExp = currentLevel.getMinExperience();
                int nextLevelMinExp = nextLevel.getMinExperience();
                int totalExpNeeded = nextLevelMinExp - currentLevelMinExp;
                int expGained = currentExperience - currentLevelMinExp;

                double progressPercent = totalExpNeeded > 0 ?
                    (double) expGained / totalExpNeeded * 100 : 0;

                progress.put("nextLevel", nextLevel);
                progress.put("expNeeded", Math.max(0, nextLevelMinExp - currentExperience));
                progress.put("progressPercent", Math.min(100, Math.max(0, progressPercent)));
            } else {
                progress.put("isHighestLevel", true);
            }
        } else {
            progress.put("isHighestLevel", true);
        }

        return progress;
    }

    @Override
    @Transactional
    public Integer checkAndUpdateUserLevel(Long userId) {
        UserMember userMember = userMemberRepository.findByUserId(userId);
        if (userMember == null) {
            return null;
        }

        // 根据总经验值计算应该的等级
        MemberLevel shouldLevel = getLevelByExperience(userMember.getTotalExperience());
        if (shouldLevel != null && !shouldLevel.getId().equals(userMember.getLevelId())) {
            // 更新用户等级
            userMember.setLevelId(shouldLevel.getId());
            userMemberRepository.updateById(userMember);
            return shouldLevel.getId();
        }

        return userMember.getLevelId();
    }

    @Override
    public Map<String, Object> getMemberStats() {
        Map<String, Object> stats = new HashMap<>();

        // 总会员数
        long totalMembers = userMemberRepository.selectCount(null);
        stats.put("totalMembers", totalMembers);

        // 各等级会员数
        List<MemberLevel> levels = getAllLevels();
        Map<String, Long> levelStats = new HashMap<>();
        for (MemberLevel level : levels) {
            long count = userMemberRepository.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserMember>()
                            .eq("level_id", level.getId()));
            levelStats.put(level.getLevelName(), count);
        }
        stats.put("levelStats", levelStats);

        return stats;
    }
}