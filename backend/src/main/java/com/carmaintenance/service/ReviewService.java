package com.carmaintenance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.carmaintenance.dto.ReviewDTO;
import com.carmaintenance.entity.Review;

import java.util.List;
import java.util.Map;

/**
 * 评价Service接口
 */
public interface ReviewService extends IService<Review> {

    /**
     * 创建评价
     */
    String createReview(ReviewDTO reviewDTO, Long userId);

    /**
     * 更新评价
     */
    boolean updateReview(Long reviewId, ReviewDTO reviewDTO, Long userId);

    /**
     * 删除评价
     */
    boolean deleteReview(Long reviewId, Long userId);

    /**
     * 获取用户评价列表
     */
    Page<Review> getUserReviews(Page<Review> page, Long userId);

    /**
     * 获取门店评价列表
     */
    Page<Review> getShopReviews(Page<Review> page, Long shopId);

    /**
     * 获取评价详情
     */
    Review getReviewDetail(Long reviewId, Long userId, Integer userType);

    /**
     * 获取门店评价统计
     */
    Map<String, Object> getShopReviewStats(Long shopId);

    /**
     * 获取门店最新评价
     */
    List<Review> getLatestReviews(Long shopId, Integer limit);

    /**
     * 更新门店评分
     */
    boolean updateShopRating(Long shopId);

    /**
     * 检查用户是否已评价
     */
    boolean checkUserReviewed(Long userId, Long orderId);

    /**
     * 管理员更新评价状态
     */
    boolean updateReviewStatus(Long reviewId, Integer status);
}
