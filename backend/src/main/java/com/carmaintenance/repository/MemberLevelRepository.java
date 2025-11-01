package com.carmaintenance.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carmaintenance.entity.MemberLevel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 会员等级Repository接口
 */
@Mapper
public interface MemberLevelRepository extends BaseMapper<MemberLevel> {

    /**
     * 根据经验值获取对应的会员等级
     */
    @Select("SELECT * FROM member_levels WHERE #{experience} >= min_experience ORDER BY min_experience DESC LIMIT 1")
    MemberLevel getLevelByExperience(Integer experience);

    /**
     * 获取所有等级（按经验值排序）
     */
    @Select("SELECT * FROM member_levels ORDER BY min_experience ASC")
    List<MemberLevel> getAllLevels();

    /**
     * 获取最高等级
     */
    @Select("SELECT * FROM member_levels ORDER BY min_experience DESC LIMIT 1")
    MemberLevel getHighestLevel();
}