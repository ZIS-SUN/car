package com.carmaintenance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.carmaintenance.dto.OrderDTO;
import com.carmaintenance.dto.OrderVO;
import com.carmaintenance.entity.Order;

import java.util.List;
import java.util.Map;

/**
 * 订单Service接口
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单
     */
    String createOrder(OrderDTO orderDTO, Long userId);

    /**
     * 支付订单
     */
    boolean payOrder(Long orderId, Integer paymentMethod);

    /**
     * 取消订单
     */
    boolean cancelOrder(Long orderId, Long userId, String reason);

    /**
     * 开始服务
     */
    boolean startService(Long orderId, Long technicianId);

    /**
     * 完成服务
     */
    boolean completeService(Long orderId);

    /**
     * 获取用户订单列表
     */
    Page<Order> getUserOrders(Page<Order> page, Long userId, Integer status);

    /**
     * 获取门店订单列表
     */
    Page<Order> getShopOrders(Page<Order> page, Long shopId, Integer status);

    /**
     * 获取用户订单列表（包含完整信息）
     */
    List<OrderVO> getUserOrdersWithDetails(Long userId, Integer status);

    /**
     * 获取门店订单列表（包含完整信息）
     */
    List<OrderVO> getShopOrdersWithDetails(Long shopId, Integer status);

    /**
     * 获取订单详情
     */
    Map<String, Object> getOrderDetail(Long orderId, Long userId, Integer userType);

    /**
     * 更新订单状态
     */
    boolean updateOrderStatus(Long orderId, Integer status);

    /**
     * 分配技师
     */
    boolean assignTechnician(Long orderId, Long technicianId);

    /**
     * 获取订单统计信息
     */
    Map<String, Object> getOrderStats(Long shopId);
}
