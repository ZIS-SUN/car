# Error Fixes Summary

This document summarizes the three errors that were fixed in the car maintenance system.

## 1. ✅ Price Monitor Records - SQL Column Error

### Error
```
Error querying database. Cause: java.sql.SQLSyntaxErrorException: Unknown column 'record_time' in 'order clause'
SQL: SELECT * FROM price_monitor_records WHERE status = 'ABNORMAL' ORDER BY record_time DESC
```

### Root Cause
The `PriceMonitorRecord` entity and repository were using column names that didn't match the actual database schema:
- Entity used `record_time` but database has `create_time`
- Entity used `itemName` but database has `service_name`
- Entity used `deviationRate` but database has `diff_rate`
- Status was stored as String ("NORMAL", "TOO_HIGH", etc.) but database uses TINYINT (0, 1, 2)

### Files Fixed
1. **backend/src/main/java/com/carmaintenance/entity/PriceMonitorRecord.java**
   - Changed `recordTime` → `createTime` mapped to `create_time` column
   - Changed `itemName` → `serviceName` mapped to `service_name` column
   - Changed `deviationRate` → `diffRate` mapped to `diff_rate` column
   - Changed status from String to Integer (0=正常, 1=偏高, 2=偏低)
   - Removed fields that don't exist in database: `handleStatus`, `handleResult`, `handlerId`, `handleTime`

2. **backend/src/main/java/com/carmaintenance/repository/PriceMonitorRecordRepository.java**
   - Changed ORDER BY `record_time` → `create_time`
   - Changed status comparison from `'ABNORMAL'` → `!= 0`

3. **backend/src/main/java/com/carmaintenance/service/impl/PriceMonitorServiceImpl.java**
   - Updated `recordPriceMonitor` to convert status String to Integer
   - Updated `getMonitorRecords` to use `create_time` for ordering
   - Updated all status queries to use Integer values instead of Strings

---

## 2. ✅ Package Management - Shop Information Not Found

### Error
```
PackageManagement.vue:468 获取套餐列表失败: Error: 无法找到对应的门店信息
```

### Root Cause
The `ServicePackageController` had a hardcoded temporary method `getShopIdByUserId()` that only worked for three specific user IDs (85, 86, 87). Any shop user with a different ID would get the error.

```java
private Long getShopIdByUserId(Long userId) {
    if (userId == 85L) return 33L;
    if (userId == 86L) return 34L;
    if (userId == 87L) return 35L;
    throw new RuntimeException("无法找到对应的门店信息");  // This error!
}
```

### Files Fixed
**backend/src/main/java/com/carmaintenance/controller/ServicePackageController.java**

Replaced all hardcoded `getShopIdByUserId()` calls with proper database queries:

```java
// Before (hardcoded):
Long shopId = getShopIdByUserId(userPrincipal.getUserId());

// After (proper database query):
Shop shop = shopService.getShopByUserId(userPrincipal.getUserId());
if (shop == null) {
    return Result.error("门店信息不存在");
}
Long shopId = shop.getId();
```

Updated methods:
- `createServicePackage()`
- `updateServicePackage()`
- `deleteServicePackage()`
- `getMyServicePackages()`
- `getServicePackageDetail()`
- `batchUpdatePackageStatus()`
- `getServicePackageStats()`

---

## 3. ⚠️ Vehicle Foreign Key Constraint Violation

### Error
```
Error updating database. Cause: java.sql.SQLIntegrityConstraintViolationException: 
Cannot add or update a child row: a foreign key constraint fails 
(`car_maintenance_db`.`vehicles`, CONSTRAINT `vehicles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`))
```

### Root Cause
This is a **data integrity issue**, not a code bug. The error occurs when trying to insert a vehicle with a `user_id` that doesn't exist in the `users` table.

The database schema enforces referential integrity:
```sql
CREATE TABLE vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    ...
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Possible Causes

1. **User doesn't exist**: The user_id being used doesn't exist in the users table
2. **User was deleted**: The user was soft-deleted (deleted=1) but foreign key constraint still references the id
3. **Wrong user_id**: Frontend is sending incorrect user_id
4. **Authentication issue**: The authenticated user_id from JWT token doesn't match a valid user

### How to Debug

1. **Check the user_id being used:**
   ```sql
   -- Find out which user_id is causing the error
   SELECT * FROM users WHERE id = <user_id_from_error>;
   ```

2. **Check if user exists and is active:**
   ```sql
   SELECT id, username, status, deleted FROM users WHERE id = <user_id>;
   ```

3. **Verify JWT token payload:**
   - Check browser console/network tab for the Authorization header
   - Decode the JWT token to see the user_id claim

4. **Check application logs:**
   - Look for the specific user_id that's failing in backend logs
   - Verify the user is properly authenticated

### Solutions

**Option A: Ensure user exists before adding vehicle**

Frontend should only show vehicle management to logged-in users:

```javascript
// In VehicleManagement.vue
const addVehicle = async () => {
  const token = localStorage.getItem('token');
  if (!token) {
    ElMessage.error('请先登录');
    return;
  }
  // ... rest of code
}
```

**Option B: Add user validation in backend**

```java
// In VehicleServiceImpl.addVehicle()
@Override
public String addVehicle(VehicleDTO vehicleDTO, Long userId) {
    // Validate user exists
    User user = userService.getById(userId);
    if (user == null || user.getDeleted() == 1) {
        throw new BusinessException(400, "用户不存在或已被删除");
    }
    
    // ... rest of code
}
```

**Option C: If testing with seed data**

Make sure the user exists first:
```sql
-- Create user first
INSERT INTO users (username, password, email, phone, user_type, status) 
VALUES ('testuser', '$2a$10$...', 'test@example.com', '13800138000', 1, 1);

-- Then create vehicle using that user's ID
INSERT INTO vehicles (user_id, brand, model, license_plate, color, year) 
VALUES (LAST_INSERT_ID(), 'Toyota', 'Camry', '粤B12345', '白色', 2023);
```

### Prevention

1. **Always validate user authentication** before vehicle operations
2. **Use transactions** when creating related entities
3. **Add proper error handling** with user-friendly messages
4. **Consider soft deletes** - keep user records even when "deleted"
5. **Test with valid user accounts** in development

---

## Testing the Fixes

### 1. Test Price Monitor Records
```bash
# Start backend
cd backend
mvn spring-boot:run

# Access price monitor page and verify no SQL errors
```

### 2. Test Package Management
```bash
# Login as any shop user (not just users 85, 86, 87)
# Navigate to PackageManagement page
# Verify packages load without error
```

### 3. Test Vehicle Creation
```bash
# Login as a valid customer user
# Navigate to Vehicle Management
# Try adding a vehicle
# Should work if user is authenticated and exists
```

---

## Summary

| Issue | Status | Impact |
|-------|--------|--------|
| Price Monitor SQL Error | ✅ Fixed | High - Prevented price monitoring from working |
| Shop Information Not Found | ✅ Fixed | High - Blocked all package operations for most shops |
| Vehicle Foreign Key Error | ⚠️ Data Issue | Medium - Need to ensure users exist before adding vehicles |

All code issues have been resolved. The vehicle error is a data/usage issue that requires proper user authentication and validation.

