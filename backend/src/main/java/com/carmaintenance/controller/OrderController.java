package com.carmaintenance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carmaintenance.dto.OrderDTO;
import com.carmaintenance.dto.OrderVO;
import com.carmaintenance.dto.Result;
import com.carmaintenance.entity.Order;
import com.carmaintenance.entity.Shop;
import com.carmaintenance.repository.ShopRepository;
import com.carmaintenance.security.UserPrincipal;
import com.carmaintenance.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ShopRepository shopRepository;

    /**
     * 创建订单
     */
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<String> createOrder(@Valid @RequestBody OrderDTO orderDTO,
                                     @AuthenticationPrincipal UserPrincipal userPrincipal) {
        String message = orderService.createOrder(orderDTO, userPrincipal.getUserId());
        return Result.success(message);
    }

    /**
     * 支付订单
     */
    @PutMapping("/{orderId}/pay")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<String> payOrder(@PathVariable Long orderId,
                                   @RequestParam Integer paymentMethod,
                                   @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (orderService.payOrder(orderId, paymentMethod)) {
            return Result.success("支付成功");
        } else {
            return Result.error("支付失败");
        }
    }

    /**
     * 取消订单
     */
    @PutMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<String> cancelOrder(@PathVariable Long orderId,
                                     @RequestParam(required = false) String reason,
                                     @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (orderService.cancelOrder(orderId, userPrincipal.getUserId(), reason)) {
            return Result.success("订单取消成功");
        } else {
            return Result.error("订单取消失败");
        }
    }

    /**
     * 获取用户订单列表
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<java.util.List<OrderVO>> getMyOrders(
            @RequestParam(required = false) Integer status,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        java.util.List<OrderVO> orders = orderService.getUserOrdersWithDetails(userPrincipal.getUserId(), status);

        return Result.success(orders);
    }

    /**
     * 获取门店订单列表
     */
    @GetMapping("/shop")
    @PreAuthorize("hasRole('SHOP')")
    public Result<java.util.List<OrderVO>> getShopOrders(
            @RequestParam(required = false) Integer status,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // 根据用户ID获取门店ID
        Shop shop = shopRepository.findByUserId(userPrincipal.getUserId());
        if (shop == null) {
            return Result.error("门店信息不存在");
        }
        Long shopId = shop.getId();

        java.util.List<OrderVO> orders = orderService.getShopOrdersWithDetails(shopId, status);

        return Result.success(orders);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SHOP')")
    public Result<Map<String, Object>> getOrderDetail(@PathVariable Long orderId,
                                                     @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Map<String, Object> detail = orderService.getOrderDetail(orderId, userPrincipal.getUserId(), userPrincipal.getUserType());
        return Result.success(detail);
    }

    /**
     * 开始服务（门店用户）
     */
    @PutMapping("/{orderId}/start")
    @PreAuthorize("hasRole('SHOP')")
    public Result<String> startService(@PathVariable Long orderId,
                                     @RequestParam(required = false) Long technicianId,
                                     @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (orderService.startService(orderId, technicianId)) {
            return Result.success("服务开始");
        } else {
            return Result.error("服务开始失败");
        }
    }

    /**
     * 完成服务（门店用户）
     */
    @PutMapping("/{orderId}/complete")
    @PreAuthorize("hasRole('SHOP')")
    public Result<String> completeService(@PathVariable Long orderId,
                                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (orderService.completeService(orderId)) {
            return Result.success("服务完成");
        } else {
            return Result.error("服务完成失败");
        }
    }

    /**
     * 更新订单状态（门店用户）
     */
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('SHOP')")
    public Result<String> updateOrderStatus(@PathVariable Long orderId,
                                           @RequestParam Integer status,
                                           @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (orderService.updateOrderStatus(orderId, status)) {
            return Result.success("状态更新成功");
        } else {
            return Result.error("状态更新失败");
        }
    }

    /**
     * 分配技师（门店用户）
     */
    @PutMapping("/{orderId}/technician")
    @PreAuthorize("hasRole('SHOP')")
    public Result<String> assignTechnician(@PathVariable Long orderId,
                                          @RequestParam Long technicianId,
                                          @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (orderService.assignTechnician(orderId, technicianId)) {
            return Result.success("技师分配成功");
        } else {
            return Result.error("技师分配失败");
        }
    }

    /**
     * 获取订单统计信息（门店用户）
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('SHOP')")
    public Result<Map<String, Object>> getOrderStats(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        // 根据用户ID获取门店ID
        Shop shop = shopRepository.findByUserId(userPrincipal.getUserId());
        if (shop == null) {
            return Result.error("门店信息不存在");
        }
        Long shopId = shop.getId();
        Map<String, Object> stats = orderService.getOrderStats(shopId);
        return Result.success(stats);
    }
}
