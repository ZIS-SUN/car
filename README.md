# 汽车保养预约系统

## 项目简介

这是一个基于 Spring Boot + Vue 3 的线上汽车保养预约服务系统，支持客户预约保养服务、门店管理服务项目、管理员系统管理等功能。

## 功能特性

### 🚗 客户端
- 用户注册与登录
- 车辆信息管理
- 门店浏览与筛选
- 在线预约保养服务
- 订单查看与管理
- 服务评价
- 会员等级系统

### 🏪 门店端
- 门店信息管理
- 服务项目管理
- 套餐组合管理
- 预约订单处理
- 客户评价回复
- 营业数据统计

### 👨‍💼 管理端
- 用户管理
- 门店审核
- 订单管理
- 评价管理
- 系统公告发布
- 数据统计分析
- 价格监控

## 技术栈

### 后端
- **框架**: Spring Boot 3.x
- **安全**: Spring Security + JWT
- **数据库**: MySQL 8.0 + Spring Data JPA
- **构建工具**: Maven
- **API文档**: Swagger/OpenAPI

### 前端
- **框架**: Vue 3
- **路由**: Vue Router
- **HTTP**: Axios
- **构建工具**: Vite
- **UI组件**: Element Plus (可选)

## 快速开始

### 环境要求

- Java 17+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+

### 1. 数据库初始化

```bash
# 登录MySQL
mysql -u root -p

# 执行初始化脚本
source database/init_complete.sql
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

### 3. 启动前端

```bash
cd frontend/vue-app
npm install
npm run dev
```

前端服务将在 `http://localhost:5173` 启动

### 4. 快速启动（可选）

使用提供的启动脚本：

```bash
chmod +x start.sh
./start.sh
```

## 测试账户

所有测试账户的默认密码均为：`123456`

| 用户类型 | 账户名 | 说明 |
|---------|--------|------|
| 管理员 | admin | 系统管理员 |
| 客户 | customer1 | 普通会员 - 张三 |
| 客户 | customer2 | 银牌会员 - 李四 |
| 客户 | customer3 | 金牌会员 - 王五 |
| 门店 | shop_beijing | 北京快捷保养中心 |
| 门店 | shop_shanghai | 上海专业汽修 |

更多测试账户请查看数据库初始化脚本。

## 项目结构

```
car-maintenance-system/
├── backend/                    # Spring Boot 后端
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/          # Java源代码
│   │   │   │   └── com/carmaintenance/
│   │   │   │       ├── controller/    # 控制器
│   │   │   │       ├── service/       # 业务逻辑
│   │   │   │       ├── repository/    # 数据访问
│   │   │   │       ├── entity/        # 实体类
│   │   │   │       ├── dto/           # 数据传输对象
│   │   │   │       └── security/      # 安全配置
│   │   │   └── resources/
│   │   │       └── application.yml    # 应用配置
│   │   └── test/              # 测试代码
│   └── pom.xml                # Maven配置
│
├── frontend/                  # Vue 前端
│   └── vue-app/
│       ├── src/
│       │   ├── views/         # 页面组件
│       │   ├── components/    # 公共组件
│       │   ├── router/        # 路由配置
│       │   ├── api/           # API接口
│       │   └── assets/        # 静态资源
│       └── package.json       # npm配置
│
├── database/                  # 数据库相关
│   └── init_complete.sql     # 完整初始化脚本
│
├── README.md                  # 项目说明（本文件）
├── DEPLOYMENT.md              # 详细部署指南
└── start.sh                   # 快速启动脚本
```

## API 文档

启动后端后，访问以下地址查看API文档：

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Docs: http://localhost:8080/v3/api-docs

## 核心功能说明

### 预约流程
1. 客户选择门店和服务项目
2. 选择预约时间
3. 填写车辆和备注信息
4. 提交预约
5. 门店确认预约
6. 到店服务
7. 完成订单
8. 客户评价

### 会员体系
- 普通会员：0经验值，无折扣
- 银牌会员：100经验值，95折优惠
- 金牌会员：500经验值，9折优惠
- 钻石会员：1000经验值，85折优惠

经验值通过消费获得，1元 = 1经验值

### 价格监控
系统设置了平台指导价，管理员可以监控门店定价是否偏离指导价过多，确保价格合理。

## 配置说明

### 后端配置

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/car_maintenance_db
    username: root
    password: your_password
  
  jpa:
    hibernate:
      ddl-auto: none  # 使用SQL脚本管理数据库
    show-sql: false
    
jwt:
  secret: your_jwt_secret_key
  expiration: 86400000  # 24小时
```

### 前端配置

编辑 `frontend/vue-app/src/api/index.js` 中的 API 基础地址：

```javascript
const API_BASE_URL = 'http://localhost:8080';
```

## 常见问题

### 1. 数据库连接失败
- 检查MySQL服务是否启动
- 确认数据库名称、用户名、密码是否正确
- 检查数据库连接URL中的时区设置

### 2. 端口冲突
- 修改后端端口：`application.yml` 中的 `server.port`
- 修改前端端口：`vite.config.js` 中的 `server.port`

### 3. 登录失败
- 确认使用的是测试账户
- 检查密码是否为 `123456`
- 查看后端日志确认JWT配置

## 开发计划

- [ ] 移动端适配
- [ ] 支付功能集成
- [ ] 短信通知
- [ ] 地图定位
- [ ] 数据导出
- [ ] 更多统计图表

## 贡献指南

欢迎提交 Issue 和 Pull Request。

## 许可证

本项目仅供学习和参考使用。

## 详细文档

更多详细信息请查看：
- [部署指南](./DEPLOYMENT.md)
- [启动脚本说明](./start.sh)
- [数据库脚本](./database/init_complete.sql)

## 联系方式

如有问题或建议，欢迎联系项目维护者。
