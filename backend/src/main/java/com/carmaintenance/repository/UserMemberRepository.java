package com.carmaintenance.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carmaintenance.entity.UserMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户会员Repository接口
 */
@Mapper
public interface UserMemberRepository extends BaseMapper<UserMember> {

    /**
     * 根据用户ID查询会员信息
     */
    @Select("SELECT um.*, ml.level_name, ml.discount_rate, ml.benefits " +
            "FROM user_members um " +
            "LEFT JOIN member_levels ml ON um.level_id = ml.id " +
            "WHERE um.user_id = #{userId}")
    UserMember findByUserId(@Param("userId") Long userId);

    /**
     * 检查用户是否已有会员信息
     */
    @Select("SELECT COUNT(*) FROM user_members WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);
}