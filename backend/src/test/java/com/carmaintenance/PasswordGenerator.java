package com.carmaintenance;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password = "123456";
        String hash = encoder.encode(password);
        
        System.out.println("========================================");
        System.out.println("密码: " + password);
        System.out.println("BCrypt哈希: " + hash);
        System.out.println("========================================");
        System.out.println("\nSQL更新语句:");
        System.out.println("UPDATE users SET password = '" + hash + "' WHERE username IN ('admin', 'customer1', 'customer2', 'customer3', 'customer4', 'customer5', 'shop_beijing', 'shop_shanghai', 'shop_guangzhou', 'shop_shenzhen', 'shop_hangzhou');");
        System.out.println("========================================");
        
        // 验证
        boolean matches = encoder.matches(password, hash);
        System.out.println("\n验证结果: " + matches);
    }
}
