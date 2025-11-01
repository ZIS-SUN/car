package com.carmaintenance.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carmaintenance.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评价Repository接口
 */
@Mapper
public interface ReviewRepository extends BaseMapper<Review> {

    /**
     * 根据用户ID查��评价
     */
    @Select("SELECT * FROM reviews WHERE user_id = #{userId} AND deleted = 0 ORDER BY create_time DESC")
    List<Review> findByUserId(@Param("userId") Long userId);

    /**
     * 根据门店ID查询评价
     */
    @Select("SELECT * FROM reviews WHERE shop_id = #{shopId} AND status = 1 AND deleted = 0 ORDER BY create_time DESC")
    List<Review> findByShopId(@Param("shopId") Long shopId);

    /**
     * 根据订单ID查询评价
     */
    @Select("SELECT * FROM reviews WHERE order_id = #{orderId} AND deleted = 0")
    Review findByOrderId(@Param("orderId") Long orderId);

    /**
     * 获取门店平均评分
     */
    @Select("SELECT AVG(overall_rating) FROM reviews WHERE shop_id = #{shopId} AND status = 1 AND deleted = 0")
    Double getAverageRating(@Param("shopId") Long shopId);

    /**
     * 获取门店评价统计
     */
    @Select("SELECT COUNT(*) FROM reviews WHERE shop_id = #{shopId} AND status = 1 AND deleted = 0")
    int getReviewCount(@Param("shopId") Long shopId);

    /**
     * 获取门店最新评价
     */
    @Select("SELECT * FROM reviews WHERE shop_id = #{shopId} AND status = 1 AND deleted = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<Review> getLatestReviews(@Param("shopId") Long shopId, @Param("limit") Integer limit);

    /**
     * 检查用户是否已评价该订单
     */
    @Select("SELECT COUNT(*) FROM reviews WHERE user_id = #{userId} AND order_id = #{orderId} AND deleted = 0")
    int checkUserReviewed(@Param("userId") Long userId, @Param("orderId") Long orderId);
}
