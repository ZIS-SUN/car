package com.carmaintenance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carmaintenance.dto.Result;
import com.carmaintenance.dto.ReviewDTO;
import com.carmaintenance.entity.Review;
import com.carmaintenance.security.UserPrincipal;
import com.carmaintenance.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 评价控制器
 */
@RestController
@RequestMapping("/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * 创建评价
     */
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<String> createReview(@Valid @RequestBody ReviewDTO reviewDTO,
                                      @AuthenticationPrincipal UserPrincipal userPrincipal) {
        String message = reviewService.createReview(reviewDTO, userPrincipal.getUserId());
        return Result.success(message);
    }

    /**
     * 更新评价
     */
    @PutMapping("/{reviewId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<String> updateReview(@PathVariable Long reviewId,
                                      @Valid @RequestBody ReviewDTO reviewDTO,
                                      @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (reviewService.updateReview(reviewId, reviewDTO, userPrincipal.getUserId())) {
            return Result.success("评价更新成功");
        } else {
            return Result.error("评价更新失败");
        }
    }

    /**
     * 删除评价
     */
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<String> deleteReview(@PathVariable Long reviewId,
                                      @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (reviewService.deleteReview(reviewId, userPrincipal.getUserId())) {
            return Result.success("评价删除成功");
        } else {
            return Result.error("评价删除失败");
        }
    }

    /**
     * 获取用户评价列表
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<Page<Review>> getMyReviews(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Page<Review> page = new Page<>(current, size);
        Page<Review> reviewPage = reviewService.getUserReviews(page, userPrincipal.getUserId());

        return Result.success(reviewPage);
    }

    /**
     * 获取门店评价列表
     */
    @GetMapping("/shop/{shopId}")
    public Result<Page<Review>> getShopReviews(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<Review> page = new Page<>(current, size);
        Page<Review> reviewPage = reviewService.getShopReviews(page, shopId);

        return Result.success(reviewPage);
    }

    /**
     * 获取评价详情
     */
    @GetMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SHOP')")
    public Result<Review> getReviewDetail(@PathVariable Long reviewId,
                                          @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Review review = reviewService.getReviewDetail(reviewId, userPrincipal.getUserId(), userPrincipal.getUserType());
        return Result.success(review);
    }

    /**
     * 获取门店评价统计
     */
    @GetMapping("/stats/{shopId}")
    public Result<Map<String, Object>> getShopReviewStats(@PathVariable Long shopId) {
        Map<String, Object> stats = reviewService.getShopReviewStats(shopId);
        return Result.success(stats);
    }

    /**
     * 获取门店最新评价
     */
    @GetMapping("/latest/{shopId}")
    public Result<List<Review>> getLatestReviews(@PathVariable Long shopId,
                                                @RequestParam(defaultValue = "10") Integer limit) {
        List<Review> reviews = reviewService.getLatestReviews(shopId, limit);
        return Result.success(reviews);
    }

    /**
     * 检查用户是否已评价
     */
    @GetMapping("/check")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<Boolean> checkUserReviewed(@RequestParam Long orderId,
                                            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        boolean reviewed = reviewService.checkUserReviewed(userPrincipal.getUserId(), orderId);
        return Result.success(reviewed);
    }

    /**
     * 管理员更新评价状态
     */
    @PutMapping("/{reviewId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> updateReviewStatus(@PathVariable Long reviewId,
                                           @RequestParam Integer status) {
        if (reviewService.updateReviewStatus(reviewId, status)) {
            String statusText = status == 1 ? "显示" : "隐藏";
            return Result.success("评价已" + statusText);
        } else {
            return Result.error("状态更新失败");
        }
    }
}
