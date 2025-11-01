package com.carmaintenance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carmaintenance.dto.ReviewDTO;
import com.carmaintenance.entity.Order;
import com.carmaintenance.entity.Review;
import com.carmaintenance.entity.Shop;
import com.carmaintenance.exception.BusinessException;
import com.carmaintenance.repository.OrderRepository;
import com.carmaintenance.repository.ReviewRepository;
import com.carmaintenance.repository.ShopRepository;
import com.carmaintenance.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评价Service实现类
 */
@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewRepository, Review> implements ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewServiceImpl.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Override
    @Transactional
    public String createReview(ReviewDTO reviewDTO, Long userId) {
        logger.info("用户创建评价: userId={}, orderId={}", userId, reviewDTO.getOrderId());
        
        // 检查订单是否存在
        Order order = orderRepository.selectById(reviewDTO.getOrderId());
        if (order == null) {
            logger.warn("评价创建失败: 订单不存在, orderId={}", reviewDTO.getOrderId());
            throw new BusinessException(404, "订单不存在");
        }

        // 检查是否为订单所有者
        if (!order.getUserId().equals(userId)) {
            logger.warn("评价创建失败: 无权评价订单, userId={}, orderId={}", userId, reviewDTO.getOrderId());
            throw new BusinessException(403, "无权评价该订单");
        }

        // 检查订单状态
        if (order.getStatus() != Order.Status.COMPLETED.getCode()) {
            logger.warn("评价创建失败: 订单状态不允许, orderId={}, status={}", reviewDTO.getOrderId(), order.getStatus());
            throw new BusinessException(400, "只能评价已完成的订单");
        }

        // 检查是否已评价
        if (checkUserReviewed(userId, reviewDTO.getOrderId())) {
            logger.warn("评价创建失败: 订单已评价, userId={}, orderId={}", userId, reviewDTO.getOrderId());
            throw new BusinessException(400, "该订单已评价");
        }

        // 计算综合评分
        BigDecimal overallRating = calculateOverallRating(reviewDTO);

        // 创建评价
        Review review = new Review();
        BeanUtils.copyProperties(reviewDTO, review);
        review.setUserId(userId);
        review.setShopId(order.getShopId());
        review.setOverallRating(overallRating);
        review.setStatus(1); // 默认显示

        if (save(review)) {
            // 更新门店评分
            updateShopRating(order.getShopId());
            logger.info("评价创建成功: userId={}, reviewId={}, orderId={}", userId, review.getId(), reviewDTO.getOrderId());
            return "评价成功";
        } else {
            logger.error("评价创建失败: 数据库保存失败, userId={}, orderId={}", userId, reviewDTO.getOrderId());
            throw new BusinessException(500, "评价失败");
        }
    }

    @Override
    @Transactional
    public boolean updateReview(Long reviewId, ReviewDTO reviewDTO, Long userId) {
        Review existingReview = getById(reviewId);
        if (existingReview == null) {
            throw new BusinessException(404, "评价不存在");
        }

        // 检查权限
        if (!existingReview.getUserId().equals(userId)) {
            logger.warn("评价更新失败: 无权修改, userId={}, reviewId={}", userId, reviewId);
            throw new BusinessException(403, "无权修改该评价");
        }

        // 更新评价信息
        BeanUtils.copyProperties(reviewDTO, existingReview, "id", "userId", "orderId", "shopId", "createTime");

        // 重新计算综合评分
        BigDecimal overallRating = calculateOverallRating(reviewDTO);
        existingReview.setOverallRating(overallRating);

        if (updateById(existingReview)) {
            // 更新门店评分
            updateShopRating(existingReview.getShopId());
            return true;
        }

        return false;
    }

    @Override
    @Transactional
    public boolean deleteReview(Long reviewId, Long userId) {
        Review review = getById(reviewId);
        if (review == null) {
            throw new BusinessException(404, "评价不存在");
        }

        // 检查权限
        if (!review.getUserId().equals(userId)) {
            logger.warn("评价删除失败: 无权删除, userId={}, reviewId={}", userId, reviewId);
            throw new BusinessException(403, "无权删除该评价");
        }

        Long shopId = review.getShopId();
        boolean result = removeById(reviewId);

        // 更新门店评分
        if (result) {
            updateShopRating(shopId);
        }

        return result;
    }

    @Override
    public Page<Review> getUserReviews(Page<Review> page, Long userId) {
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");
        return page(page, wrapper);
    }

    @Override
    public Page<Review> getShopReviews(Page<Review> page, Long shopId) {
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("shop_id", shopId);
        wrapper.eq("status", 1); // 只显示公开的评价
        wrapper.orderByDesc("create_time");
        return page(page, wrapper);
    }

    @Override
    public Review getReviewDetail(Long reviewId, Long userId, Integer userType) {
        Review review = getById(reviewId);
        if (review == null) {
            throw new BusinessException(404, "评价不存在");
        }

        boolean isReviewer = review.getUserId().equals(userId);
        boolean isAdmin = userType != null && com.carmaintenance.entity.User.UserType.ADMIN.getCode().equals(userType);
        boolean isShopOwner = false;

        if (!isReviewer && !isAdmin) {
            Shop shop = shopRepository.findByUserId(userId);
            if (shop != null && shop.getId().equals(review.getShopId())) {
                isShopOwner = true;
            }
        }

        if (!isReviewer && !isAdmin && !isShopOwner) {
            logger.warn("评价查看失败: 无权查看, userId={}, reviewId={}", userId, reviewId);
            throw new BusinessException(403, "无权查看该评价");
        }

        return review;
    }

    @Override
    public Map<String, Object> getShopReviewStats(Long shopId) {
        Map<String, Object> stats = new HashMap<>();

        // 总评价数
        int totalCount = baseMapper.getReviewCount(shopId);
        stats.put("totalCount", totalCount);

        // 平均评分
        Double avgRating = baseMapper.getAverageRating(shopId);
        if (avgRating != null) {
            stats.put("averageRating", BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP));
        } else {
            stats.put("averageRating", BigDecimal.ZERO);
        }

        // 评分分布
        if (totalCount > 0) {
            Map<Integer, Long> ratingDistribution = new HashMap<>();
            for (int i = 1; i <= 5; i++) {
                long count = count(new QueryWrapper<Review>()
                        .eq("shop_id", shopId)
                        .eq("status", 1)
                        .eq("overall_rating", i));
                ratingDistribution.put(i, count);
            }
            stats.put("ratingDistribution", ratingDistribution);
        }

        return stats;
    }

    @Override
    public List<Review> getLatestReviews(Long shopId, Integer limit) {
        return baseMapper.getLatestReviews(shopId, limit != null ? limit : 10);
    }

    @Override
    @Transactional
    public boolean updateShopRating(Long shopId) {
        Double avgRating = baseMapper.getAverageRating(shopId);
        Shop shop = shopRepository.selectById(shopId);

        if (shop != null) {
            shop.setRating(avgRating != null ?
                BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP) :
                BigDecimal.valueOf(5.0));
            return shopRepository.updateById(shop) > 0;
        }

        return false;
    }

    @Override
    public boolean checkUserReviewed(Long userId, Long orderId) {
        int count = baseMapper.checkUserReviewed(userId, orderId);
        return count > 0;
    }

    @Override
    @Transactional
    public boolean updateReviewStatus(Long reviewId, Integer status) {
        Review review = getById(reviewId);
        if (review == null) {
            throw new BusinessException(404, "评价不存在");
        }

        review.setStatus(status);
        boolean result = updateById(review);

        // 如果隐藏评价，需要重新计算门店评分
        if (result && (status == 0 || status == 1)) {
            updateShopRating(review.getShopId());
        }

        return result;
    }

    /**
     * 计算综合评分
     */
    private BigDecimal calculateOverallRating(ReviewDTO reviewDTO) {
        int totalRating = 0;
        int ratingCount = 0;

        if (reviewDTO.getTechnicianRating() != null) {
            totalRating += reviewDTO.getTechnicianRating();
            ratingCount++;
        }
        if (reviewDTO.getServiceRating() != null) {
            totalRating += reviewDTO.getServiceRating();
            ratingCount++;
        }
        if (reviewDTO.getPriceRating() != null) {
            totalRating += reviewDTO.getPriceRating();
            ratingCount++;
        }
        if (reviewDTO.getEnvironmentRating() != null) {
            totalRating += reviewDTO.getEnvironmentRating();
            ratingCount++;
        }

        if (ratingCount == 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf((double) totalRating / ratingCount)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
