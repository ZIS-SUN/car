package com.carmaintenance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carmaintenance.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公告Mapper
 */
@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
}
