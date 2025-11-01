package com.carmaintenance.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carmaintenance.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Repository接口
 */
@Mapper
public interface UserRepository extends BaseMapper<User> {
}