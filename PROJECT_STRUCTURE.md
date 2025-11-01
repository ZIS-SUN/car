# 项目结构说明

## 目录结构

```
car-maintenance-system/
│
├── backend/                          # 后端项目目录
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/                # Java源代码
│   │   │   │   └── com/carmaintenance/
│   │   │   │       ├── CarMaintenanceApplication.java    # 主启动类
│   │   │   │       ├── config/                          # 配置类
│   │   │   │       ├── controller/                      # REST控制器
│   │   │   │       ├── dto/                            # 数据传输对象
│   │   │   │       ├── entity/                         # JPA实体类
│   │   │   │       ├── exception/                      # 异常处理
│   │   │   │       ├── mapper/                         # 对象映射
│   │   │   │       ├── repository/                     # 数据仓库
│   │   │   │       ├── security/                       # 安全配置
│   │   │   │       ├── service/                        # 业务逻辑
│   │   │   │       └── utils/                          # 工具类
│   │   │   └── resources/
│   │   │       └── application.yml                     # Spring配置文件
│   │   └── test/                    # 测试代码
│   └── pom.xml                      # Maven项目配置
│
├── frontend/                        # 前端项目目录
│   └── vue-app/
│       ├── src/
│       │   ├── api/                # API接口封装
│       │   │   ├── admin.js       # 管理端API
│       │   │   ├── index.js       # 客户端API
│       │   │   └── shop.js        # 门店端API
│       │   ├── assets/            # 静态资源
│       │   │   ├── css/          # 样式文件
│       │   │   └── js/           # JS工具
│       │   ├── components/        # 公共组件
│       │   │   ├── AdminLayout.vue          # 管理端布局
│       │   │   ├── ShopLayout.vue           # 门店端布局
│       │   │   ├── AppHeader.vue            # 头部组件
│       │   │   ├── AppFooter.vue            # 底部组件
│       │   │   ├── AnnouncementBanner.vue   # 公告横幅
│       │   │   ├── OrderDetail.vue          # 订单详情
│       │   │   └── VehicleManagement.vue    # 车辆管理
│       │   ├── router/            # 路由配置
│       │   │   └── index.js      # 路由定义
│       │   ├── store/             # 状态管理（预留）
│       │   ├── views/             # 页面组件
│       │   │   ├── admin/         # 管理端页面
│       │   │   │   ├── AdminDashboard.vue
│       │   │   │   ├── MemberManagement.vue
│       │   │   │   ├── ShopManagement.vue
│       │   │   │   ├── OrderManagement.vue
│       │   │   │   ├── ReviewManagement.vue
│       │   │   │   ├── AnnouncementManagement.vue
│       │   │   │   ├── SystemSettings.vue
│       │   │   │   ├── DataStatistics.vue
│       │   │   │   ├── DataExport.vue
│       │   │   │   └── PriceMonitor.vue
│       │   │   ├── shop/          # 门店端页面
│       │   │   │   ├── ShopDashboard.vue
│       │   │   │   ├── ShopLogin.vue
│       │   │   │   ├── ShopProfile.vue
│       │   │   │   ├── ServiceManagement.vue
│       │   │   │   ├── PackageManagement.vue
│       │   │   │   └── OrderManagement.vue
│       │   │   ├── Home.vue       # 首页
│       │   │   ├── Login.vue      # 登录
│       │   │   ├── Register.vue   # 注册
│       │   │   ├── ShopList.vue   # 门店列表
│       │   │   ├── ShopDetail.vue # 门店详情
│       │   │   ├── Appointment.vue         # 预约服务
│       │   │   ├── MyOrders.vue            # 我的订单
│       │   │   ├── OrderDetail.vue         # 订单详情
│       │   │   ├── CreateReview.vue        # 创建评价
│       │   │   ├── MyReviews.vue           # 我的评价
│       │   │   ├── MemberCenter.vue        # 会员中心
│       │   │   ├── Services.vue            # 服务项目
│       │   │   ├── Announcements.vue       # 系统公告
│       │   │   └── About.vue               # 关于我们
│       │   ├── App.vue            # 根组件
│       │   └── main.js            # 入口文件
│       ├── public/                # 公共资源
│       │   ├── logo.png          # Logo图片
│       │   └── logo-mini.png     # 小Logo
│       ├── dist/                  # 构建输出目录
│       ├── index.html             # HTML模板
│       ├── package.json           # npm配置
│       ├── package-lock.json      # npm依赖锁定
│       └── vite.config.js         # Vite配置
│
├── database/                      # 数据库相关
│   └── init_complete.sql         # 完整的数据库初始化脚本
│
├── README.md                      # 项目说明文档
├── DEPLOYMENT.md                  # 详细部署指南
├── PROJECT_STRUCTURE.md           # 项目结构说明（本文件）
└── start.sh                       # 快速启动脚本
```

## 核心文件说明

### 后端核心文件

#### 配置类（config/）
- `CorsConfig.java` - 跨域配置
- `JwtConfig.java` - JWT配置
- `SecurityConfig.java` - Spring Security配置
- `SwaggerConfig.java` - API文档配置

#### 控制器（controller/）
按功能模块划分：
- 用户管理：UserController
- 门店管理：ShopController
- 车辆管理：VehicleController
- 预约管理：AppointmentController
- 订单管理：OrderController
- 评价管理：ReviewController
- 服务项目：ServiceItemController, ServicePackageController
- 会员系统：MemberController
- 系统配置：SystemConfigController, AnnouncementController
- 数据统计：StatisticsController

#### 实体类（entity/）
对应数据库表的JPA实体：
- User - 用户
- Shop - 门店
- Vehicle - 车辆
- MemberLevel - 会员等级
- ServiceItem - 服务项目
- ServicePackage - 套餐
- Appointment - 预约
- Order - 订单
- Review - 评价
- Announcement - 公告
- SystemConfig - 系统配置
- 等等...

#### 服务类（service/）
业务逻辑实现，与控制器一一对应

### 前端核心文件

#### API接口（src/api/）
- `index.js` - 客户端API（登录、注册、预约、订单等）
- `admin.js` - 管理端API（用户管理、门店审核、统计等）
- `shop.js` - 门店端API（服务管理、订单处理等）

#### 路由配置（src/router/index.js）
定义了所有页面路由，包括：
- 公共路由（首页、登录、注册等）
- 客户路由（预约、订单、评价等）
- 门店路由（服务管理、订单处理等）
- 管理路由（系统管理、数据统计等）

#### 组件（src/components/）
- 布局组件：AdminLayout、ShopLayout
- 功能组件：AppHeader、AppFooter、AnnouncementBanner
- 业务组件：OrderDetail、VehicleManagement

### 数据库脚本

#### init_complete.sql
完整的数据库初始化脚本，包含：
1. 数据库创建
2. 17张数据表创建
3. 索引创建
4. 基础数据插入（会员等级、系统配置、平台指导价）
5. 测试账户创建（管理员、客户、门店）
6. 测试数据插入（门店、车辆、服务项目、预约、订单等）

## 数据库表结构

### 核心业务表
1. **users** - 用户表（客户、门店、管理员）
2. **shops** - 门店信息表
3. **vehicles** - 车辆信息表
4. **service_items** - 服务项目表
5. **service_packages** - 套餐表
6. **package_items** - 套餐项目关联表
7. **appointments** - 预约表
8. **appointment_items** - 预约项目表
9. **orders** - 订单表
10. **reviews** - 评价表

### 会员系统表
11. **member_levels** - 会员等级表
12. **user_members** - 用户会员信息表
13. **experience_records** - 经验记录表

### 系统管理表
14. **announcements** - 系统公告表
15. **system_configs** - 系统配置表
16. **platform_guide_prices** - 平台指导价表
17. **price_monitor_records** - 价格监控记录表

## 技术架构

### 后端技术栈
- **Spring Boot 3.x** - 应用框架
- **Spring Security** - 安全框架
- **Spring Data JPA** - 数据访问
- **JWT** - 认证授权
- **MySQL** - 关系数据库
- **Maven** - 项目管理
- **Lombok** - 代码简化
- **Swagger/OpenAPI** - API文档

### 前端技术栈
- **Vue 3** - 前端框架
- **Vue Router** - 路由管理
- **Axios** - HTTP客户端
- **Vite** - 构建工具
- **Element Plus**（可选） - UI组件库

## 功能模块

### 1. 客户端模块
- 用户注册/登录
- 车辆管理
- 门店浏览与搜索
- 服务预约
- 订单管理
- 服务评价
- 会员中心

### 2. 门店端模块
- 门店信息管理
- 服务项目管理
- 套餐管理
- 预约管理
- 订单处理
- 评价回复
- 营业统计

### 3. 管理端模块
- 用户管理
- 门店审核与管理
- 订单管理
- 评价管理
- 系统公告
- 数据统计
- 价格监控
- 数据导出

## 开发规范

### 后端开发规范
1. Controller只做参数校验和调用Service
2. Service实现具体业务逻辑
3. Repository只做数据访问
4. 使用DTO在Controller和Service之间传输数据
5. 统一异常处理
6. 统一返回结果封装

### 前端开发规范
1. 组件化开发
2. API统一封装在api目录
3. 路由统一配置在router目录
4. 样式文件统一放在assets/css目录
5. 使用async/await处理异步操作
6. 统一的错误处理

## 部署说明

详细部署步骤请参考 [DEPLOYMENT.md](./DEPLOYMENT.md)

## 测试账户

| 类型 | 账户名 | 密码 | 说明 |
|-----|--------|------|------|
| 管理员 | admin | 123456 | 系统管理员 |
| 客户 | customer1-5 | 123456 | 测试客户账户 |
| 门店 | shop_beijing 等 | 123456 | 测试门店账户 |

## 联系方式

如有问题或建议，欢迎联系项目维护者。


