# 汽车保养预约系统 - 部署指南

## 项目简介

这是一个线上预约汽车保养服务系统，包含前端（Vue.js）和后端（Spring Boot）两部分。

## 系统要求

### 后端要求
- Java 17 或更高版本
- Maven 3.6+
- MySQL 8.0+

### 前端要求
- Node.js 16+ 
- npm 或 yarn

## 数据库部署

### 1. 创建数据库

使用以下SQL脚本完成数据库初始化：

```bash
mysql -u root -p < database/init_complete.sql
```

该脚本将完成：
- 创建数据库 `car_maintenance_db`
- 创建所有必要的表结构
- 插入基础配置数据
- 创建测试账户和示例数据

### 2. 测试账户

系统初始化后会创建以下测试账户（所有密码均为：`123456`）：

#### 管理员账户
- 账户名：`admin`
- 密码：`123456`
- 类型：系统管理员

#### 客户账户
- `customer1` - 张三（普通会员）
- `customer2` - 李四（银牌会员）
- `customer3` - 王五（金牌会员）
- `customer4` - 赵六（普通会员）
- `customer5` - 钱七（钻石会员）

#### 门店账户
- `shop_beijing` - 北京快捷保养中心
- `shop_shanghai` - 上海专业汽修
- `shop_guangzhou` - 广州精工保养
- `shop_shenzhen` - 深圳车管家
- `shop_hangzhou` - 杭州爱车坊

## 后端部署

### 1. 配置数据库连接

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/car_maintenance_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 你的数据库密码
```

### 2. 编译和运行

```bash
cd backend
mvn clean package
java -jar target/car-maintenance-0.0.1-SNAPSHOT.jar
```

或者使用 Maven 直接运行：

```bash
cd backend
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动。

## 前端部署

### 1. 安装依赖

```bash
cd frontend/vue-app
npm install
```

### 2. 开发模式运行

```bash
npm run dev
```

前端开发服务器将在 `http://localhost:5173` 启动。

### 3. 生产构建

```bash
npm run build
```

构建产物将输出到 `frontend/vue-app/dist` 目录。

## 快速启动

项目根目录提供了快速启动脚本：

```bash
./start.sh
```

该脚本将自动：
1. 启动后端服务
2. 启动前端开发服务器

## API 文档

后端启动后，可以访问以下地址查看 API 文档：

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/v3/api-docs`

## 项目结构

```
car-maintenance-system/
├── backend/                 # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/       # Java 源代码
│   │   │   └── resources/  # 配置文件
│   │   └── test/           # 测试代码
│   └── pom.xml             # Maven 配置
│
├── frontend/               # 前端项目
│   └── vue-app/
│       ├── src/            # Vue 源代码
│       ├── public/         # 静态资源
│       └── package.json    # npm 配置
│
├── database/               # 数据库脚本
│   └── init_complete.sql  # 完整数据库初始化脚本
│
├── README.md              # 项目说明
├── DEPLOYMENT.md          # 部署指南（本文件）
└── start.sh               # 快速启动脚本
```

## 主要功能模块

### 客户端功能
- 用户注册/登录
- 车辆管理
- 门店浏览
- 服务预约
- 订单管理
- 评价管理
- 会员中心

### 门店端功能
- 门店资料管理
- 服务项目管理
- 套餐管理
- 预约管理
- 订单处理
- 评价回复

### 管理端功能
- 用户管理
- 门店审核
- 订单管理
- 评价管理
- 系统公告
- 数据统计
- 价格监控

## 常见问题

### 1. 数据库连接失败

检查：
- MySQL 服务是否启动
- 数据库用户名密码是否正确
- 数据库是否已创建

### 2. 端口被占用

修改配置文件中的端口：
- 后端：`application.yml` 中的 `server.port`
- 前端：`vite.config.js` 中的 `server.port`

### 3. 前端无法访问后端接口

检查：
- 后端服务是否启动
- 前端配置的 API 地址是否正确
- 是否存在跨域问题

## 技术栈

### 后端
- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- MySQL
- JWT

### 前端
- Vue 3
- Vue Router
- Axios
- Element Plus / Vite

## 许可证

本项目仅供学习和参考使用。

## 联系方式

如有问题，请联系项目维护者。


