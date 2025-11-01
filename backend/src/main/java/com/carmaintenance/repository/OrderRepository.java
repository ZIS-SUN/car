package com.carmaintenance.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carmaintenance.dto.OrderVO;
import com.carmaintenance.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单Repository接口
 */
@Mapper
public interface OrderRepository extends BaseMapper<Order> {

    /**
     * 根据用户ID查询订单
     */
    @Select("SELECT * FROM orders WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Order> findByUserId(@Param("userId") Long userId);

    /**
     * 根据门店ID查询订单
     */
    @Select("SELECT * FROM orders WHERE shop_id = #{shopId} ORDER BY create_time DESC")
    List<Order> findByShopId(@Param("shopId") Long shopId);

    /**
     * 根据预约ID查询订单
     */
    @Select("SELECT * FROM orders WHERE appointment_id = #{appointmentId}")
    Order findByAppointmentId(@Param("appointmentId") Long appointmentId);

    /**
     * 根据订单编号查询
     */
    @Select("SELECT * FROM orders WHERE order_no = #{orderNo}")
    Object findByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 获取用户订单统计
     */
    @Select("SELECT COUNT(*) FROM orders WHERE user_id = #{userId} AND status = #{status}")
    int countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 获取门店订单统计
     */
    @Select("SELECT COUNT(*) FROM orders WHERE shop_id = #{shopId} AND status = #{status}")
    int countByShopIdAndStatus(@Param("shopId") Long shopId, @Param("status") Integer status);

    /**
     * 获取用户订单列表（包含关联信息）
     */
    @Select("<script>" +
            "SELECT " +
            "o.id, o.order_no, o.appointment_id, o.user_id, o.shop_id, " +
            "o.total_amount, o.discount_amount, o.final_amount, " +
            "o.payment_method, o.payment_status, " +
            "o.technician_id, o.start_time, o.end_time, o.status, " +
            "o.create_time, o.update_time, " +
            "s.shop_name, s.address as shopAddress, s.phone as shopPhone, " +
            "a.appointment_date as appointmentDate, a.time_slot as appointmentTime, a.vehicle_id, " +
            "v.brand as vehicleBrand, v.model as vehicleModel, v.license_plate as licensePlate, " +
            "GROUP_CONCAT(DISTINCT si.name SEPARATOR ', ') as serviceItems, " +
            "u.real_name as technicianName " +
            "FROM orders o " +
            "LEFT JOIN shops s ON o.shop_id = s.id " +
            "LEFT JOIN appointments a ON o.appointment_id = a.id " +
            "LEFT JOIN vehicles v ON a.vehicle_id = v.id " +
            "LEFT JOIN appointment_items ai ON a.id = ai.appointment_id " +
            "LEFT JOIN service_items si ON ai.item_id = si.id " +
            "LEFT JOIN users u ON o.technician_id = u.id " +
            "WHERE o.user_id = #{userId} " +
            "<if test='status != null'> AND o.status = #{status} </if>" +
            "GROUP BY o.id " +
            "ORDER BY o.create_time DESC" +
            "</script>")
    List<OrderVO> findUserOrdersWithDetails(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 获取门店订单列表（包含关联信息）
     */
    @Select("<script>" +
            "SELECT " +
            "o.id, o.order_no, o.appointment_id, o.user_id, o.shop_id, " +
            "o.total_amount, o.discount_amount, o.final_amount, " +
            "o.payment_method, o.payment_status, " +
            "o.technician_id, o.start_time, o.end_time, o.status, " +
            "o.create_time, o.update_time, " +
            "s.shop_name, s.address as shopAddress, s.phone as shopPhone, " +
            "a.appointment_date as appointmentDate, a.time_slot as appointmentTime, a.vehicle_id, " +
            "v.brand as vehicleBrand, v.model as vehicleModel, v.license_plate as licensePlate, " +
            "GROUP_CONCAT(DISTINCT si.name SEPARATOR ', ') as serviceItems, " +
            "u.real_name as technicianName " +
            "FROM orders o " +
            "LEFT JOIN shops s ON o.shop_id = s.id " +
            "LEFT JOIN appointments a ON o.appointment_id = a.id " +
            "LEFT JOIN vehicles v ON a.vehicle_id = v.id " +
            "LEFT JOIN appointment_items ai ON a.id = ai.appointment_id " +
            "LEFT JOIN service_items si ON ai.item_id = si.id " +
            "LEFT JOIN users u ON o.technician_id = u.id " +
            "WHERE o.shop_id = #{shopId} " +
            "<if test='status != null'> AND o.status = #{status} </if>" +
            "GROUP BY o.id " +
            "ORDER BY o.create_time DESC" +
            "</script>")
    List<OrderVO> findShopOrdersWithDetails(@Param("shopId") Long shopId, @Param("status") Integer status);
}