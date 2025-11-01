package com.carmaintenance.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carmaintenance.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 系统配置Repository接口
 */
@Mapper
public interface SystemConfigRepository extends BaseMapper<SystemConfig> {

    /**
     * 根据配置键获取配置值
     */
    @Select("SELECT config_value FROM system_configs WHERE config_key = #{configKey}")
    String getConfigValue(@Param("configKey") String configKey);

    /**
     * 根据配置键获取配置对象
     */
    @Select("SELECT * FROM system_configs WHERE config_key = #{configKey}")
    SystemConfig getByConfigKey(@Param("configKey") String configKey);

    /**
     * 更新配置值
     */
    @Update("UPDATE system_configs SET config_value = #{configValue}, update_time = NOW() WHERE config_key = #{configKey}")
    int updateConfigValue(@Param("configKey") String configKey, @Param("configValue") String configValue);
}