#!/bin/bash

# 汽车保养系统API接口测试脚本
# 测试所有主要接口功能

BASE_URL="http://localhost:8080/api"
TOKEN=""
CUSTOMER_TOKEN=""
SHOP_TOKEN=""
ADMIN_TOKEN=""

# 颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印函数
print_header() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}➜ $1${NC}"
}

# 测试API函数
test_api() {
    local method=$1
    local endpoint=$2
    local data=$3
    local token=$4
    local description=$5
    
    print_info "测试: $description"
    
    if [ -n "$token" ]; then
        if [ "$method" = "GET" ]; then
            response=$(curl -s -X GET "${BASE_URL}${endpoint}" \
                -H "Authorization: Bearer ${token}" \
                -H "Content-Type: application/json")
        else
            response=$(curl -s -X "$method" "${BASE_URL}${endpoint}" \
                -H "Authorization: Bearer ${token}" \
                -H "Content-Type: application/json" \
                -d "$data")
        fi
    else
        if [ "$method" = "GET" ]; then
            response=$(curl -s -X GET "${BASE_URL}${endpoint}" \
                -H "Content-Type: application/json")
        else
            response=$(curl -s -X "$method" "${BASE_URL}${endpoint}" \
                -H "Content-Type: application/json" \
                -d "$data")
        fi
    fi
    
    echo "Response: $response"
    
    if echo "$response" | grep -q '"code":200\|"code":0\|"success":true'; then
        print_success "$description - 成功"
        return 0
    else
        print_error "$description - 失败"
        return 1
    fi
}

# 开始测试
print_header "汽车保养系统API测试"
echo "测试服务器: $BASE_URL"
echo "开始时间: $(date '+%Y-%m-%d %H:%M:%S')"

# 统计
total_tests=0
passed_tests=0
failed_tests=0

# ==================== 1. 用户认证接口测试 ====================
print_header "1. 用户认证接口测试"

# 1.1 客户注册
print_info "1.1 测试客户注册"
register_data='{
    "username": "testuser_'$(date +%s)'",
    "password": "123456",
    "confirmPassword": "123456",
    "email": "test'$(date +%s)'@test.com",
    "phone": "138'$(date +%s | tail -c 9)'",
    "realName": "测试用户",
    "userType": 1
}'
response=$(curl -s -X POST "${BASE_URL}/auth/register" \
    -H "Content-Type: application/json" \
    -d "$register_data")
echo "Response: $response"
if echo "$response" | grep -q '"code":200\|"code":0\|注册成功'; then
    print_success "客户注册 - 成功"
    ((passed_tests++))
else
    print_error "客户注册 - 失败"
    ((failed_tests++))
fi
((total_tests++))

# 1.2 客户登录
print_info "1.2 测试客户登录"
login_data='{
    "username": "customer1",
    "password": "123456"
}'
response=$(curl -s -X POST "${BASE_URL}/auth/login" \
    -H "Content-Type: application/json" \
    -d "$login_data")
echo "Response: $response"
if echo "$response" | grep -q '"code":200\|"code":0\|token'; then
    CUSTOMER_TOKEN=$(echo "$response" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    print_success "客户登录 - 成功 (Token获取成功)"
    ((passed_tests++))
else
    print_error "客户登录 - 失败"
    ((failed_tests++))
fi
((total_tests++))

# 1.3 门店登录
print_info "1.3 测试门店登录"
shop_login_data='{
    "username": "shop_beijing",
    "password": "123456"
}'
response=$(curl -s -X POST "${BASE_URL}/auth/login" \
    -H "Content-Type: application/json" \
    -d "$shop_login_data")
echo "Response: $response"
if echo "$response" | grep -q '"code":200\|"code":0\|token'; then
    SHOP_TOKEN=$(echo "$response" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    print_success "门店登录 - 成功 (Token获取成功)"
    ((passed_tests++))
else
    print_error "门店登录 - 失败"
    ((failed_tests++))
fi
((total_tests++))

# 1.4 管理员登录
print_info "1.4 测试管理员登录"
admin_login_data='{
    "username": "admin",
    "password": "123456"
}'
response=$(curl -s -X POST "${BASE_URL}/auth/login" \
    -H "Content-Type: application/json" \
    -d "$admin_login_data")
echo "Response: $response"
if echo "$response" | grep -q '"code":200\|"code":0\|token'; then
    ADMIN_TOKEN=$(echo "$response" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    print_success "管理员登录 - 成功 (Token获取成功)"
    ((passed_tests++))
else
    print_error "管理员登录 - 失败"
    ((failed_tests++))
fi
((total_tests++))

# ==================== 2. 门店接口测试 ====================
print_header "2. 门店接口测试"

# 2.1 获取门店列表
print_info "2.1 获取门店列表"
response=$(curl -s -X GET "${BASE_URL}/shop/list" \
    -H "Content-Type: application/json")
echo "Response: $response"
if echo "$response" | grep -q '"code":200\|"code":0\|shop'; then
    print_success "获取门店列表 - 成功"
    ((passed_tests++))
else
    print_error "获取门店列表 - 失败"
    ((failed_tests++))
fi
((total_tests++))

# 2.2 获取门店详情
print_info "2.2 获取门店详情"
response=$(curl -s -X GET "${BASE_URL}/shop/1" \
    -H "Content-Type: application/json")
echo "Response: $response"
if echo "$response" | grep -q '"code":200\|"code":0'; then
    print_success "获取门店详情 - 成功"
    ((passed_tests++))
else
    print_error "获取门店详情 - 失败"
    ((failed_tests++))
fi
((total_tests++))

# 2.3 搜索门店
print_info "2.3 搜索门店"
response=$(curl -s -X GET "${BASE_URL}/shop/search?keyword=%E5%8C%97%E4%BA%AC" \
    -H "Content-Type: application/json")
echo "Response: $response"
if echo "$response" | grep -q '"code":200\|"code":0'; then
    print_success "搜索门店 - 成功"
    ((passed_tests++))
else
    print_error "搜索门店 - 失败"
    ((failed_tests++))
fi
((total_tests++))

# ==================== 3. 服务项目接口测试 ====================
print_header "3. 服务项目接口测试"

# 3.1 获取服务项目列表
print_info "3.1 获取门店服务项目"
response=$(curl -s -X GET "${BASE_URL}/service-item/shop/1" \
    -H "Content-Type: application/json")
echo "Response: $response"
if echo "$response" | grep -q '"code":200\|"code":0'; then
    print_success "获取服务项目 - 成功"
    ((passed_tests++))
else
    print_error "获取服务项目 - 失败"
    ((failed_tests++))
fi
((total_tests++))

# 3.2 获取套餐列表
print_info "3.2 获取门店套餐"
response=$(curl -s -X GET "${BASE_URL}/service-package/shop/1" \
    -H "Content-Type: application/json")
echo "Response: $response"
if echo "$response" | grep -q '"code":200\|"code":0'; then
    print_success "获取套餐列表 - 成功"
    ((passed_tests++))
else
    print_error "获取套餐列表 - 失败"
    ((failed_tests++))
fi
((total_tests++))

# ==================== 4. 车辆管理接口测试 ====================
print_header "4. 车辆管理接口测试"

if [ -n "$CUSTOMER_TOKEN" ]; then
    # 4.1 获取用户车辆列表
    print_info "4.1 获取用户车辆"
    response=$(curl -s -X GET "${BASE_URL}/vehicle" \
        -H "Authorization: Bearer ${CUSTOMER_TOKEN}" \
        -H "Content-Type: application/json")
    echo "Response: $response"
    if echo "$response" | grep -q '"code":200\|"code":0'; then
        print_success "获取车辆列表 - 成功"
        ((passed_tests++))
    else
        print_error "获取车辆列表 - 失败"
        ((failed_tests++))
    fi
    ((total_tests++))
    
    # 4.2 添加车辆
    print_info "4.2 添加车辆"
    vehicle_data='{
        "brand": "丰田",
        "model": "凯美瑞",
        "licensePlate": "京B'$(date +%s | tail -c 6)'",
        "color": "白色",
        "year": 2023
    }'
    response=$(curl -s -X POST "${BASE_URL}/vehicle" \
        -H "Authorization: Bearer ${CUSTOMER_TOKEN}" \
        -H "Content-Type: application/json" \
        -d "$vehicle_data")
    echo "Response: $response"
    if echo "$response" | grep -q '"code":200\|"code":0'; then
        print_success "添加车辆 - 成功"
        ((passed_tests++))
    else
        print_error "添加车辆 - 失败"
        ((failed_tests++))
    fi
    ((total_tests++))
else
    print_error "跳过车辆测试 - 客户Token未获取"
    ((failed_tests+=2))
    ((total_tests+=2))
fi

# ==================== 5. 预约接口测试 ====================
print_header "5. 预约接口测试"

if [ -n "$CUSTOMER_TOKEN" ]; then
    # 5.1 创建预约
    print_info "5.1 创建预约"
    future_date=$(date -v+3d '+%Y-%m-%d' 2>/dev/null || date -d '+3 days' '+%Y-%m-%d' 2>/dev/null || echo "2025-12-15")
    appointment_data='{
        "shopId": 1,
        "vehicleId": 1,
        "appointmentDate": "'$future_date'",
        "timeSlot": "10:00-11:00",
        "serviceItems": [{"id":1,"name":"常规机油保养","price":299}],
        "totalAmount": 299,
        "notes": "API测试预约"
    }'
    response=$(curl -s -X POST "${BASE_URL}/appointment" \
        -H "Authorization: Bearer ${CUSTOMER_TOKEN}" \
        -H "Content-Type: application/json" \
        -d "$appointment_data")
    echo "Response: $response"
    if echo "$response" | grep -q '"code":200\|"code":0'; then
        print_success "创建预约 - 成功"
        ((passed_tests++))
    else
        print_error "创建预约 - 失败"
        ((failed_tests++))
    fi
    ((total_tests++))
    
    # 5.2 获取用户预约列表
    print_info "5.2 获取用户预约列表"
    response=$(curl -s -X GET "${BASE_URL}/appointment/my" \
        -H "Authorization: Bearer ${CUSTOMER_TOKEN}" \
        -H "Content-Type: application/json")
    echo "Response: $response"
    if echo "$response" | grep -q '"code":200\|"code":0'; then
        print_success "获取预约列表 - 成功"
        ((passed_tests++))
    else
        print_error "获取预约列表 - 失败"
        ((failed_tests++))
    fi
    ((total_tests++))
else
    print_error "跳过预约测试 - 客户Token未获取"
    ((failed_tests+=2))
    ((total_tests+=2))
fi

# ==================== 6. 订单接口测试 ====================
print_header "6. 订单接口测试"

if [ -n "$CUSTOMER_TOKEN" ]; then
    # 6.1 获取用户订单列表
    print_info "6.1 获取用户订单"
    response=$(curl -s -X GET "${BASE_URL}/orders/my" \
        -H "Authorization: Bearer ${CUSTOMER_TOKEN}" \
        -H "Content-Type: application/json")
    echo "Response: $response"
    if echo "$response" | grep -q '"code":200\|"code":0'; then
        print_success "获取订单列表 - 成功"
        ((passed_tests++))
    else
        print_error "获取订单列表 - 失败"
        ((failed_tests++))
    fi
    ((total_tests++))
    
    # 6.2 获取订单详情
    print_info "6.2 获取订单详情"
    response=$(curl -s -X GET "${BASE_URL}/orders/1" \
        -H "Authorization: Bearer ${CUSTOMER_TOKEN}" \
        -H "Content-Type: application/json")
    echo "Response: $response"
    if echo "$response" | grep -q '"code":200\|"code":0'; then
        print_success "获取订单详情 - 成功"
        ((passed_tests++))
    else
        print_error "获取订单详情 - 失败"
        ((failed_tests++))
    fi
    ((total_tests++))
else
    print_error "跳过订单测试 - 客户Token未获取"
    ((failed_tests+=2))
    ((total_tests+=2))
fi

# ==================== 7. 评价接口测试 ====================
print_header "7. 评价接口测试"

# 7.1 获取门店评价
print_info "7.1 获取门店评价"
response=$(curl -s -X GET "${BASE_URL}/review/shop/1" \
    -H "Content-Type: application/json")
echo "Response: $response"
if echo "$response" | grep -q '"code":200\|"code":0'; then
    print_success "获取门店评价 - 成功"
    ((passed_tests++))
else
    print_error "获取门店评价 - 失败"
    ((failed_tests++))
fi
((total_tests++))

if [ -n "$CUSTOMER_TOKEN" ]; then
    # 7.2 获取用户评价
    print_info "7.2 获取用户评价"
    response=$(curl -s -X GET "${BASE_URL}/review/my" \
        -H "Authorization: Bearer ${CUSTOMER_TOKEN}" \
        -H "Content-Type: application/json")
    echo "Response: $response"
    if echo "$response" | grep -q '"code":200\|"code":0'; then
        print_success "获取用户评价 - 成功"
        ((passed_tests++))
    else
        print_error "获取用户评价 - 失败"
        ((failed_tests++))
    fi
    ((total_tests++))
else
    print_error "跳过用户评价测试 - 客户Token未获取"
    ((failed_tests++))
    ((total_tests++))
fi

# ==================== 8. 会员接口测试 ====================
print_header "8. 会员接口测试"

# 8.1 获取会员等级列表
print_info "8.1 获取会员等级"
response=$(curl -s -X GET "${BASE_URL}/member-level/all" \
    -H "Content-Type: application/json")
echo "Response: $response"
if echo "$response" | grep -q '"code":200\|"code":0'; then
    print_success "获取会员等级 - 成功"
    ((passed_tests++))
else
    print_error "获取会员等级 - 失败"
    ((failed_tests++))
fi
((total_tests++))

if [ -n "$CUSTOMER_TOKEN" ]; then
    # 8.2 获取用户会员信息
    print_info "8.2 获取会员信息"
    response=$(curl -s -X GET "${BASE_URL}/user-member/my" \
        -H "Authorization: Bearer ${CUSTOMER_TOKEN}" \
        -H "Content-Type: application/json")
    echo "Response: $response"
    if echo "$response" | grep -q '"code":200\|"code":0'; then
        print_success "获取会员信息 - 成功"
        ((passed_tests++))
    else
        print_error "获取会员信息 - 失败"
        ((failed_tests++))
    fi
    ((total_tests++))
else
    print_error "跳过会员信息测试 - 客户Token未获取"
    ((failed_tests++))
    ((total_tests++))
fi

# ==================== 9. 公告接口测试 ====================
print_header "9. 公告接口测试"

# 9.1 获取公告列表
print_info "9.1 获取公告列表"
response=$(curl -s -X GET "${BASE_URL}/announcement/active" \
    -H "Content-Type: application/json")
echo "Response: $response"
if echo "$response" | grep -q '"code":200\|"code":0'; then
    print_success "获取公告列表 - 成功"
    ((passed_tests++))
else
    print_error "获取公告列表 - 失败"
    ((failed_tests++))
fi
((total_tests++))

# ==================== 10. 门店端接口测试 ====================
print_header "10. 门店端接口测试"

if [ -n "$SHOP_TOKEN" ]; then
    # 10.1 获取门店预约
    print_info "10.1 获取门店预约"
    response=$(curl -s -X GET "${BASE_URL}/appointment/shop" \
        -H "Authorization: Bearer ${SHOP_TOKEN}" \
        -H "Content-Type: application/json")
    echo "Response: $response"
    if echo "$response" | grep -q '"code":200\|"code":0'; then
        print_success "获取门店预约 - 成功"
        ((passed_tests++))
    else
        print_error "获取门店预约 - 失败"
        ((failed_tests++))
    fi
    ((total_tests++))
    
    # 10.2 获取门店订单
    print_info "10.2 获取门店订单"
    response=$(curl -s -X GET "${BASE_URL}/orders/shop" \
        -H "Authorization: Bearer ${SHOP_TOKEN}" \
        -H "Content-Type: application/json")
    echo "Response: $response"
    if echo "$response" | grep -q '"code":200\|"code":0'; then
        print_success "获取门店订单 - 成功"
        ((passed_tests++))
    else
        print_error "获取门店订单 - 失败"
        ((failed_tests++))
    fi
    ((total_tests++))
else
    print_error "跳过门店端测试 - 门店Token未获取"
    ((failed_tests+=2))
    ((total_tests+=2))
fi

# ==================== 11. 管理员接口测试 ====================
print_header "11. 管理员接口测试"

if [ -n "$ADMIN_TOKEN" ]; then
    # 11.1 获取所有用户
    print_info "11.1 获取用户列表"
    response=$(curl -s -X GET "${BASE_URL}/admin/users" \
        -H "Authorization: Bearer ${ADMIN_TOKEN}" \
        -H "Content-Type: application/json")
    echo "Response: $response"
    if echo "$response" | grep -q '"code":200\|"code":0'; then
        print_success "获取用户列表 - 成功"
        ((passed_tests++))
    else
        print_error "获取用户列表 - 失败"
        ((failed_tests++))
    fi
    ((total_tests++))
    
    # 11.2 获取所有门店
    print_info "11.2 管理员获取门店列表"
    response=$(curl -s -X GET "${BASE_URL}/admin/shops" \
        -H "Authorization: Bearer ${ADMIN_TOKEN}" \
        -H "Content-Type: application/json")
    echo "Response: $response"
    if echo "$response" | grep -q '"code":200\|"code":0'; then
        print_success "管理员获取门店 - 成功"
        ((passed_tests++))
    else
        print_error "管理员获取门店 - 失败"
        ((failed_tests++))
    fi
    ((total_tests++))
    
    # 11.3 数据统计
    print_info "11.3 获取数据统计"
    response=$(curl -s -X GET "${BASE_URL}/admin/statistics" \
        -H "Authorization: Bearer ${ADMIN_TOKEN}" \
        -H "Content-Type: application/json")
    echo "Response: $response"
    if echo "$response" | grep -q '"code":200\|"code":0'; then
        print_success "获取数据统计 - 成功"
        ((passed_tests++))
    else
        print_error "获取数据统计 - 失败"
        ((failed_tests++))
    fi
    ((total_tests++))
else
    print_error "跳过管理员测试 - 管理员Token未获取"
    ((failed_tests+=3))
    ((total_tests+=3))
fi

# ==================== 测试总结 ====================
print_header "测试总结"
echo "结束时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo ""
echo "总测试数: $total_tests"
echo -e "${GREEN}成功: $passed_tests${NC}"
echo -e "${RED}失败: $failed_tests${NC}"
echo ""

success_rate=$(awk "BEGIN {printf \"%.2f\", ($passed_tests/$total_tests)*100}")
echo "成功率: ${success_rate}%"

if [ $failed_tests -eq 0 ]; then
    print_success "所有测试通过！✨"
    exit 0
else
    print_error "有 $failed_tests 个测试失败"
    exit 1
fi

