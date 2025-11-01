#!/bin/bash

# 汽车保养预约系统启动脚本

echo "==================== 汽车保养预约系统启动 ===================="

# 检查MySQL是否运行
echo "检查MySQL服务状态..."
if ! pgrep -x "mysqld" > /dev/null; then
    echo "MySQL服务未运行，请先启动MySQL服务"
    echo "macOS: brew services start mysql"
    echo "Ubuntu: sudo systemctl start mysql"
    exit 1
fi

echo "MySQL服务正在运行..."

# 初始化数据库
echo "初始化数据库..."
mysql -u root -p1234 < database/schema/init.sql

if [ $? -eq 0 ]; then
    echo "数据库初始化成功！"
else
    echo "数据库初始化失败，请检查连接配置"
    exit 1
fi

# 启动Spring Boot应用
echo "启动Spring Boot应用..."
cd backend

# 检查是否存在Maven wrapper
if [ -f "./mvnw" ]; then
    echo "使用Maven Wrapper启动应用..."
    ./mvnw spring-boot:run
else
    echo "使用系统Maven启动应用..."
    mvn spring-boot:run
fi

echo "应用启动完成！"
echo "访问地址: http://localhost:8080"
echo "API文档: http://localhost:8080/api"
echo "默认管理员账号: admin / admin123"