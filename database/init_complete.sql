-- ============================================================
-- 汽车保养预约系统 - 完整数据库部署脚本
-- Database: car_maintenance_db
-- Version: 1.0
-- Description: 包含完整的表结构、索引、初始数据和测试数据
-- ============================================================

-- 创建数据库
DROP DATABASE IF EXISTS car_maintenance_db;
CREATE DATABASE car_maintenance_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE car_maintenance_db;

-- ============================================================
-- 1. 基础表结构创建
-- ============================================================

-- 1.1 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT 'BCrypt加密密码',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) UNIQUE COMMENT '手机号',
    user_type TINYINT DEFAULT 1 COMMENT '用户类型: 1-客户, 2-门店, 3-管理员',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-正常, 0-禁用',
    member_level_id INT DEFAULT 1 COMMENT '会员等级ID',
    experience_points INT DEFAULT 0 COMMENT '经验值',
    real_name VARCHAR(50) COMMENT '真实姓名',
    avatar_url VARCHAR(255) COMMENT '头像URL',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_phone (phone),
    INDEX idx_user_type (user_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 1.2 门店表
CREATE TABLE IF NOT EXISTS shops (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '关联用户ID',
    shop_name VARCHAR(100) NOT NULL COMMENT '门店名称',
    description TEXT COMMENT '门店描述',
    address VARCHAR(255) COMMENT '详细地址',
    city VARCHAR(50) COMMENT '城市',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    business_hours VARCHAR(100) COMMENT '营业时间',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-待审核, 1-正常, 2-暂停, 3-拒绝',
    images TEXT COMMENT '门店图片',
    logo_url VARCHAR(255) COMMENT '门店Logo URL',
    rating DECIMAL(3,2) DEFAULT 5.0 COMMENT '平均评分',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user_id (user_id),
    INDEX idx_city (city),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店表';

-- 1.3 车辆表
CREATE TABLE IF NOT EXISTS vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '车主ID',
    brand VARCHAR(50) COMMENT '品牌',
    model VARCHAR(50) COMMENT '车型',
    license_plate VARCHAR(20) NOT NULL COMMENT '车牌号',
    color VARCHAR(30) COMMENT '颜色',
    year INT COMMENT '年份',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user_id (user_id),
    INDEX idx_license_plate (license_plate)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆表';

-- 1.4 会员等级表
CREATE TABLE IF NOT EXISTS member_levels (
    id INT AUTO_INCREMENT PRIMARY KEY,
    level_name VARCHAR(50) NOT NULL COMMENT '等级名称',
    min_experience INT DEFAULT 0 COMMENT '最低经验值',
    discount_rate DECIMAL(5,4) DEFAULT 1.0000 COMMENT '折扣率',
    benefits TEXT COMMENT '等级权益',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员等级表';

-- 1.5 用户会员信息表
CREATE TABLE IF NOT EXISTS user_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    level_id INT NOT NULL COMMENT '等级ID',
    total_experience INT DEFAULT 0 COMMENT '总经验值',
    available_experience INT DEFAULT 0 COMMENT '可用经验值',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (level_id) REFERENCES member_levels(id),
    INDEX idx_user_id (user_id),
    INDEX idx_level_id (level_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户会员信息表';

-- 1.6 经验记录表
CREATE TABLE IF NOT EXISTS experience_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_id BIGINT COMMENT '关联订单ID',
    experience_change INT NOT NULL COMMENT '经验值变化',
    experience_type TINYINT NOT NULL COMMENT '类型: 1-消费获得, 2-投诉扣减, 3-手动调整',
    reason VARCHAR(255) COMMENT '原因说明',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user_id (user_id),
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='经验记录表';

-- 1.7 服务项目表
CREATE TABLE IF NOT EXISTS service_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shop_id BIGINT NOT NULL COMMENT '门店ID',
    name VARCHAR(100) NOT NULL COMMENT '项目名称',
    description TEXT COMMENT '项目描述',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    duration_minutes INT NOT NULL COMMENT '标准工时(分钟)',
    category VARCHAR(50) COMMENT '项目分类',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-上架, 0-下架',
    image_url VARCHAR(255) COMMENT '项目图片URL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    FOREIGN KEY (shop_id) REFERENCES shops(id),
    INDEX idx_shop_id (shop_id),
    INDEX idx_status (status),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务项目表';

-- 1.8 套餐表
CREATE TABLE IF NOT EXISTS service_packages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shop_id BIGINT NOT NULL COMMENT '门店ID',
    package_name VARCHAR(100) NOT NULL COMMENT '套餐名称',
    description TEXT COMMENT '套餐描述',
    package_price DECIMAL(10,2) NOT NULL COMMENT '套餐价格',
    original_price DECIMAL(10,2) COMMENT '原价',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-上架, 0-下架',
    image_url VARCHAR(255) COMMENT '套餐图片URL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    FOREIGN KEY (shop_id) REFERENCES shops(id),
    INDEX idx_shop_id (shop_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐表';

-- 1.9 套餐项目关联表
CREATE TABLE IF NOT EXISTS package_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    package_id BIGINT NOT NULL COMMENT '套餐ID',
    item_id BIGINT NOT NULL COMMENT '服务项目ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (package_id) REFERENCES service_packages(id),
    FOREIGN KEY (item_id) REFERENCES service_items(id),
    UNIQUE KEY uk_package_item (package_id, item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐项目关联表';

-- 1.10 预约表
CREATE TABLE IF NOT EXISTS appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_no VARCHAR(50) COMMENT '预约编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    shop_id BIGINT NOT NULL COMMENT '门店ID',
    vehicle_id BIGINT NOT NULL COMMENT '车辆ID',
    appointment_date DATETIME NOT NULL COMMENT '预约时间',
    time_slot VARCHAR(20) COMMENT '时间段',
    bay_number INT COMMENT '工位号',
    service_items TEXT COMMENT '服务项目JSON',
    total_amount DECIMAL(10,2) DEFAULT 0 COMMENT '总金额',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-待确认, 2-已确认, 3-进行中, 4-已完成, 5-已取消',
    cancel_reason VARCHAR(255) COMMENT '取消原因',
    cancel_time DATETIME COMMENT '取消时间',
    notes TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (shop_id) REFERENCES shops(id),
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
    INDEX idx_user_id (user_id),
    INDEX idx_shop_id (shop_id),
    INDEX idx_appointment_date (appointment_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

-- 1.11 预约项目表
CREATE TABLE IF NOT EXISTS appointment_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT NOT NULL COMMENT '预约ID',
    item_id BIGINT COMMENT '服务项目ID',
    package_id BIGINT COMMENT '套餐ID',
    item_name VARCHAR(100) COMMENT '项目/套餐名称',
    price DECIMAL(10,2) COMMENT '价格',
    quantity INT DEFAULT 1 COMMENT '数量',
    subtotal DECIMAL(10,2) COMMENT '小计',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (appointment_id) REFERENCES appointments(id),
    FOREIGN KEY (item_id) REFERENCES service_items(id),
    FOREIGN KEY (package_id) REFERENCES service_packages(id),
    INDEX idx_appointment_id (appointment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约项目表';

-- 1.12 订单表
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单编号',
    appointment_id BIGINT COMMENT '预约ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    shop_id BIGINT NOT NULL COMMENT '门店ID',
    vehicle_id BIGINT NOT NULL COMMENT '车辆ID',
    service_items TEXT COMMENT '服务项目详情JSON',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    discount_amount DECIMAL(10,2) DEFAULT 0 COMMENT '折扣金额',
    final_amount DECIMAL(10,2) DEFAULT 0 COMMENT '实付金额',
    payment_method TINYINT COMMENT '支付方式: 1-现金, 2-微信, 3-支付宝, 4-银行卡',
    payment_status TINYINT DEFAULT 0 COMMENT '支付状态: 0-未支付, 1-已支付',
    technician_id BIGINT COMMENT '技师ID',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-待服务, 2-进行中, 3-已完成, 4-已取消',
    start_time DATETIME COMMENT '服务开始时间',
    end_time DATETIME COMMENT '服务结束时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    FOREIGN KEY (appointment_id) REFERENCES appointments(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (shop_id) REFERENCES shops(id),
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
    INDEX idx_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_shop_id (shop_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 1.13 评价表
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    shop_id BIGINT NOT NULL COMMENT '门店ID',
    overall_rating DECIMAL(3,2) NOT NULL COMMENT '综合评分',
    technician_rating TINYINT CHECK (technician_rating >= 1 AND technician_rating <= 5) COMMENT '技师评分',
    service_rating TINYINT CHECK (service_rating >= 1 AND service_rating <= 5) COMMENT '服务评分',
    price_rating TINYINT CHECK (price_rating >= 1 AND price_rating <= 5) COMMENT '价格评分',
    environment_rating TINYINT CHECK (environment_rating >= 1 AND environment_rating <= 5) COMMENT '环境评分',
    comment TEXT COMMENT '评价内容',
    reply TEXT COMMENT '商家回复',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-显示, 0-隐藏',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (shop_id) REFERENCES shops(id),
    INDEX idx_order_id (order_id),
    INDEX idx_shop_id (shop_id),
    INDEX idx_overall_rating (overall_rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- 1.14 公告表
CREATE TABLE IF NOT EXISTS announcements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '公告标题',
    content TEXT NOT NULL COMMENT '公告内容',
    type TINYINT DEFAULT 1 COMMENT '类型: 1-系统公告, 2-活动通知',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-草稿, 1-已发布, 2-已下线',
    priority TINYINT DEFAULT 1 COMMENT '优先级: 1-普通, 2-重要',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    created_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_status (status),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- 1.15 系统配置表
CREATE TABLE IF NOT EXISTS system_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(255) COMMENT '配置说明',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 1.16 平台指导价表
CREATE TABLE IF NOT EXISTS platform_guide_prices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    service_name VARCHAR(100) NOT NULL COMMENT '服务名称',
    category VARCHAR(50) COMMENT '服务类别',
    guide_price DECIMAL(10,2) NOT NULL COMMENT '指导价格',
    min_price DECIMAL(10,2) COMMENT '最低价格',
    max_price DECIMAL(10,2) COMMENT '最高价格',
    description TEXT COMMENT '说明',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_service_name (service_name),
    INDEX idx_category (category),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台指导价表';

-- 1.17 价格监控记录表
CREATE TABLE IF NOT EXISTS price_monitor_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    shop_id BIGINT NOT NULL COMMENT '门店ID',
    service_item_id BIGINT COMMENT '服务项目ID',
    service_name VARCHAR(100) NOT NULL COMMENT '服务名称',
    shop_price DECIMAL(10,2) NOT NULL COMMENT '门店价格',
    guide_price DECIMAL(10,2) COMMENT '平台指导价',
    price_diff DECIMAL(10,2) COMMENT '价格差异',
    diff_rate DECIMAL(5,2) COMMENT '差异百分比',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-正常, 1-偏高, 2-偏低',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE CASCADE,
    INDEX idx_shop_id (shop_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='价格监控记录表';

-- ============================================================
-- 2. 插入基础数据
-- ============================================================

-- 2.1 会员等级数据
INSERT INTO member_levels (level_name, min_experience, discount_rate, benefits) VALUES
('普通会员', 0, 1.0000, '基础服务'),
('银牌会员', 100, 0.9500, '95折优惠+免费洗车'),
('金牌会员', 500, 0.9000, '9折优惠+免费洗车+专属客服'),
('钻石会员', 1000, 0.8500, '85折优惠+免费洗车+专属客服+优先预约');

-- 2.2 系统配置数据
INSERT INTO system_configs (config_key, config_value, description) VALUES
('system_name', '汽车保养预约系统', '系统名称'),
('max_appointments_per_day', '50', '每日最大预约数'),
('appointment_advance_days', '7', '可提前预约天数'),
('default_service_duration', '60', '默认服务时长(分钟)'),
('review_points', '10', '评价积分奖励'),
('appointment_time_interval', '30', '预约时间间隔(分钟)'),
('business_hours_start', '08:00', '营业开始时间'),
('business_hours_end', '18:00', '营业结束时间'),
('experience_per_yuan', '1', '每消费1元获得经验值'),
('cancel_penalty_hours', '2', '预约前多少小时取消不算违约');

-- 2.3 平台指导价数据
INSERT INTO platform_guide_prices (service_name, category, guide_price, min_price, max_price, description) VALUES
('机油更换', '保养', 300.00, 200.00, 500.00, '标准机油更换服务'),
('机滤更换', '保养', 80.00, 50.00, 150.00, '机油滤清器更换'),
('空滤更换', '保养', 60.00, 40.00, 120.00, '空气滤清器更换'),
('空调滤芯更换', '保养', 100.00, 60.00, 200.00, '空调滤芯更换'),
('轮胎更换', '维修', 800.00, 500.00, 1500.00, '单条轮胎更换'),
('刹车片更换', '维修', 400.00, 250.00, 800.00, '前或后刹车片更换'),
('火花塞更换', '保养', 200.00, 100.00, 400.00, '火花塞更换'),
('防冻液更换', '保养', 150.00, 80.00, 300.00, '防冻液更换');

-- ============================================================
-- 3. 创建测试账户
-- ============================================================
-- 所有测试账户密码均为: 123456
-- BCrypt加密: $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi

-- 3.1 管理员账户
INSERT INTO users (username, password, email, phone, user_type, status, member_level_id, real_name) VALUES
('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'admin@example.com', '13900000000', 3, 1, 4, '系统管理员');

-- 3.2 客户账户
INSERT INTO users (username, password, email, phone, user_type, status, member_level_id, experience_points, real_name) VALUES
('customer1', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'customer1@example.com', '13800138001', 1, 1, 1, 0, '张三'),
('customer2', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'customer2@example.com', '13800138002', 1, 1, 2, 150, '李四'),
('customer3', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'customer3@example.com', '13800138003', 1, 1, 3, 600, '王五'),
('customer4', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'customer4@example.com', '13800138004', 1, 1, 1, 0, '赵六'),
('customer5', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'customer5@example.com', '13800138005', 1, 1, 4, 1200, '钱七');

-- 3.3 门店账户
INSERT INTO users (username, password, email, phone, user_type, status, member_level_id, real_name) VALUES
('shop_beijing', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'shop_beijing@example.com', '010-12345678', 2, 1, 4, '北京快捷保养中心'),
('shop_shanghai', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'shop_shanghai@example.com', '021-12345678', 2, 1, 4, '上海专业汽修'),
('shop_guangzhou', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'shop_guangzhou@example.com', '020-12345678', 2, 1, 4, '广州精工保养'),
('shop_shenzhen', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'shop_shenzhen@example.com', '0755-12345678', 2, 1, 4, '深圳车管家'),
('shop_hangzhou', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'shop_hangzhou@example.com', '0571-12345678', 2, 1, 4, '杭州爱车坊');

-- ============================================================
-- 4. 插入测试数据
-- ============================================================

-- 4.1 门店数据
INSERT INTO shops (user_id, shop_name, description, address, city, phone, email, business_hours, status) VALUES
(7, '北京快捷保养中心', '专业汽车保养维修服务，品质保证，价格实惠。提供快速保养、维修、美容等一站式服务。', '北京市朝阳区建国路88号SOHO现代城', '北京', '010-12345678', 'shop_beijing@example.com', '周一至周日 8:00-20:00', 1),
(8, '上海专业汽修', '资深技师团队，专业维修各类品牌汽车。配备先进设备，确保维修质量。', '上海市浦东新区世纪大道100号', '上海', '021-12345678', 'shop_shanghai@example.com', '周一至周六 9:00-18:00', 1),
(9, '广州精工保养', '专注汽车精工保养，使用原厂配件。提供上门取送车服务。', '广州市天河区珠江新城花城大道', '广州', '020-12345678', 'shop_guangzhou@example.com', '周一至周日 8:30-19:30', 1),
(10, '深圳车管家', '全方位汽车管家服务，包括保养、维修、保险、年检等。会员专享优惠。', '深圳市南山区科技园南区', '深圳', '0755-12345678', 'shop_shenzhen@example.com', '周一至周六 8:00-18:00', 1),
(11, '杭州爱车坊', '温馨的汽车保养环境，专业的服务团队。提供女性专享服务。', '杭州市西湖区文三路478号', '杭州', '0571-12345678', 'shop_hangzhou@example.com', '周二至周日 9:00-18:00', 1);

-- 4.2 车辆数据
INSERT INTO vehicles (user_id, brand, model, license_plate, color, year) VALUES
(2, '大众', '朗逸', '京A12345', '白色', 2020),
(2, '丰田', '卡罗拉', '京A67890', '银色', 2019),
(3, '本田', '雅阁', '沪B23456', '黑色', 2021),
(4, '奥迪', 'A4L', '粤C34567', '灰色', 2022),
(5, '宝马', '3系', '京D45678', '白色', 2020),
(6, '奔驰', 'C级', '浙E56789', '蓝色', 2023);

-- 4.3 服务项目数据
INSERT INTO service_items (shop_id, name, description, price, duration_minutes, category, status) VALUES
-- 北京门店
(1, '常规机油保养', '更换机油机滤，检查常规项目（刹车、轮胎、灯光等）', 299.00, 60, '保养', 1),
(1, '全合成机油保养', '使用全合成机油，性能更优，保护更全面', 499.00, 75, '保养', 1),
(1, '空调滤芯更换', '更换空调滤芯，清洗空调管道，除异味', 199.00, 45, '保养', 1),
(1, '刹车片更换', '更换前轮刹车片，检查刹车系统，确保安全', 599.00, 120, '维修', 1),
(1, '轮胎换位动平衡', '四轮轮胎换位，做动平衡，延长轮胎寿命', 149.00, 60, '保养', 1),
(1, '发动机清洗', '清洗发动机内部积碳，提升动力，降低油耗', 399.00, 90, '维修', 1),

-- 上海门店
(2, '豪华保养套餐', '使用进口全合成机油，包含28项全面检测', 699.00, 90, '保养', 1),
(2, '变速箱油更换', '更换变速箱油，检查变速箱系统', 799.00, 150, '维修', 1),
(2, '火花塞更换', '更换四缸火花塞，检查点火系统', 299.00, 80, '维修', 1),
(2, '刹车油更换', '更换刹车油，排空气，确保刹车性能', 399.00, 100, '维修', 1),
(2, '防冻液更换', '更换防冻液，检查冷却系统', 299.00, 70, '保养', 1),

-- 广州门店
(3, '基础保养', '经济实惠的保养方案，适合日常使用', 259.00, 50, '保养', 1),
(3, '空调系统清洗', '深度清洗空调系统，杀菌除味', 299.00, 60, '保养', 1),
(3, '节气门清洗', '清洗节气门，改善怠速不稳问题', 199.00, 40, '维修', 1),
(3, '雨刮器更换', '更换前后雨刮器，确保雨天视线清晰', 99.00, 20, '保养', 1),
(3, '电瓶检测更换', '检测电瓶状态，必要时更换', 399.00, 50, '维修', 1),

-- 深圳门店
(4, '商务车保养', '针对商务用车的高效保养方案', 599.00, 80, '保养', 1),
(4, 'SUV专项保养', '针对SUV车型的专业保养', 699.00, 100, '保养', 1),
(4, '新能源车检查', '新能源车专项检查服务', 399.00, 90, '检查', 1),
(4, '车身抛光打蜡', '车身抛光，打蜡养护，焕然一新', 499.00, 120, '美容', 1),
(4, '内饰深度清洁', '全车内饰清洁消毒，呵护健康', 299.00, 90, '美容', 1),

-- 杭州门店
(5, '女性专享保养', '为女性车主定制的贴心保养服务', 499.00, 70, '保养', 1),
(5, '老人车关爱保养', '针对老年人用车的安全检查保养', 399.00, 80, '保养', 1),
(5, '新车首保', '新车首次保养，确保车辆最佳状态', 199.00, 60, '保养', 1),
(5, '年检前保养', '年检前的全面检查保养', 299.00, 90, '检查', 1),
(5, '长途出行检查', '长途出行前的安全检查', 199.00, 60, '检查', 1);

-- 4.4 套餐数据
INSERT INTO service_packages (shop_id, package_name, description, package_price, original_price, status) VALUES
-- 北京门店
(1, '经济保养套餐', '常规机油保养+空调滤芯更换+轮胎换位', 499.00, 647.00, 1),
(1, '豪华保养套餐', '全合成机油保养+空调清洗+发动机清洗+全面检测', 899.00, 1197.00, 1),
(1, '安全出行套餐', '刹车片检查+轮胎检查+灯光检查+油液检查', 399.00, 547.00, 1),

-- 上海门店
(2, '商务精英套餐', '豪华保养+变速箱检查+电瓶检测+内饰清洁', 1299.00, 1697.00, 1),
(2, '四季无忧套餐', '包含春夏秋冬四季保养项目', 1999.00, 2597.00, 1),

-- 广州门店
(3, '城市通勤套餐', '基础保养+空调清洗+节油检查', 399.00, 557.00, 1),
(3, '健康呼吸套餐', '空调系统清洗+内饰消毒+空气净化', 399.00, 598.00, 1),

-- 深圳门店
(4, '精品养护套餐', '商务车保养+内饰清洁+车身美容', 999.00, 1397.00, 1),
(4, '科技护航套餐', '新能源车检查+智能系统检测+软件升级', 799.00, 1098.00, 1),

-- 杭州门店
(5, '温馨家庭套餐', '女性专享保养+儿童安全座椅检查+车内消毒', 699.00, 998.00, 1),
(5, '畅行无忧套餐', '新车首保+年检前检查+应急包准备', 499.00, 698.00, 1);

-- 4.5 套餐项目关联
INSERT INTO package_items (package_id, item_id) VALUES
-- 北京门店套餐
(1, 1), (1, 3), (1, 5),
(2, 2), (2, 3), (2, 6),

-- 上海门店套餐
(3, 7), (3, 9), (3, 10),
(4, 7), (4, 8), (4, 9), (4, 10), (4, 11),

-- 广州门店套餐
(5, 12), (5, 14),
(6, 12), (6, 13), (6, 15),

-- 深圳门店套餐
(7, 17), (7, 20), (7, 21),
(8, 18), (8, 19),

-- 杭州门店套餐
(9, 22), (9, 24),
(10, 24), (10, 25);

-- 4.6 预约数据
INSERT INTO appointments (user_id, shop_id, vehicle_id, appointment_date, service_items, total_amount, status, notes) VALUES
-- 已完成的预约
(2, 1, 1, '2024-01-15 09:00:00', '[{"id":1,"name":"常规机油保养","price":299}]', 299.00, 4, '客户很满意，推荐朋友来'),
(3, 2, 3, '2024-01-20 14:00:00', '[{"id":7,"name":"豪华保养套餐","price":699}]', 699.00, 4, '服务很好，技师很专业'),

-- 进行中的预约
(4, 3, 4, '2024-01-25 10:00:00', '[{"id":12,"name":"基础保养","price":259}]', 259.00, 3, '客户要求更换高级机油'),

-- 已确认的预约
(5, 4, 5, '2024-01-28 13:00:00', '[{"id":17,"name":"商务车保养","price":599}]', 599.00, 2, '预约周末时间'),
(6, 5, 6, '2024-01-30 11:00:00', '[{"id":22,"name":"女性专享保养","price":499}]', 499.00, 2, '第一次来这家店');

-- 4.7 订单数据
INSERT INTO orders (order_no, appointment_id, user_id, shop_id, vehicle_id, service_items, total_amount, discount_amount, final_amount, payment_method, payment_status, status, start_time, end_time) VALUES
-- 已完成订单
('ORD20240115001', 1, 2, 1, 1, '[{"id":1,"name":"常规机油保养","price":299,"quantity":1}]', 299.00, 0, 299.00, 2, 1, 3, '2024-01-15 09:00:00', '2024-01-15 10:15:00'),
('ORD20240120002', 2, 3, 2, 3, '[{"id":7,"name":"豪华保养套餐","price":699,"quantity":1}]', 699.00, 0, 699.00, 3, 1, 3, '2024-01-20 14:00:00', '2024-01-20 15:45:00'),

-- 进行中订单
('ORD20240125003', 3, 4, 3, 4, '[{"id":12,"name":"基础保养","price":259,"quantity":1}]', 259.00, 0, 259.00, 1, 1, 2, '2024-01-25 10:00:00', NULL),

-- 待服务订单
('ORD20240128004', 4, 5, 4, 5, '[{"id":17,"name":"商务车保养","price":599,"quantity":1}]', 599.00, 0, 599.00, NULL, 0, 1, NULL, NULL),
('ORD20240130005', 5, 6, 5, 6, '[{"id":22,"name":"女性专享保养","price":499,"quantity":1}]', 499.00, 0, 499.00, NULL, 0, 1, NULL, NULL);

-- 4.8 评价数据
INSERT INTO reviews (order_id, user_id, shop_id, overall_rating, technician_rating, service_rating, price_rating, environment_rating, comment, status) VALUES
(1, 2, 1, 5.00, 5, 5, 4, 5, '服务非常专业，技师很细心，价格也合理。推荐！', 1),
(2, 3, 2, 4.00, 4, 5, 3, 4, '整体不错，就是价格稍微有点贵。', 1);

-- 4.9 公告数据
INSERT INTO announcements (title, content, type, status, priority, created_by) VALUES
('系统升级通知', '本系统将于2024年2月1日凌晨2:00-4:00进行系统升级，期间可能影响正常使用，请提前做好准备。', 1, 1, 2, 1),
('春节优惠活动', '春节期间（2月8日-2月17日），所有保养项目8.8折优惠！新用户首次到店再送精美礼品一份。', 2, 1, 1, 1),
('新增服务项目', '我们新增了新能源汽车专项检查服务，欢迎新能源车主预约体验！', 2, 1, 1, 1),
('会员等级升级通知', '会员等级权益升级，钻石会员可享受优先预约服务，详情请查看会员中心。', 1, 1, 1, 1);

-- ============================================================
-- 完成部署
-- ============================================================

SELECT '数据库初始化完成！' as message;
SELECT '======================================' as line;
SELECT '测试账户信息（密码均为：123456）' as info;
SELECT '======================================' as line;

SELECT 
    CASE user_type 
        WHEN 1 THEN '客户' 
        WHEN 2 THEN '门店' 
        WHEN 3 THEN '管理员' 
    END as '用户类型',
    username as '账户名',
    real_name as '姓名',
    email as '邮箱',
    phone as '电话'
FROM users 
WHERE deleted = 0
ORDER BY user_type, id;

SELECT '======================================' as line;
SELECT '数据统计信息' as info;
SELECT '======================================' as line;

SELECT 
    (SELECT COUNT(*) FROM users WHERE user_type = 1 AND deleted = 0) as '客户数量',
    (SELECT COUNT(*) FROM users WHERE user_type = 2 AND deleted = 0) as '门店数量',
    (SELECT COUNT(*) FROM shops WHERE deleted = 0) as '门店信息数',
    (SELECT COUNT(*) FROM vehicles WHERE deleted = 0) as '车辆数量',
    (SELECT COUNT(*) FROM service_items WHERE deleted = 0) as '服务项目数',
    (SELECT COUNT(*) FROM service_packages WHERE deleted = 0) as '套餐数量',
    (SELECT COUNT(*) FROM appointments WHERE deleted = 0) as '预约数量',
    (SELECT COUNT(*) FROM orders WHERE deleted = 0) as '订单数量';

