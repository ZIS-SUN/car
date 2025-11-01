package com.carmaintenance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carmaintenance.dto.ShopRegisterDTO;
import com.carmaintenance.entity.Shop;
import com.carmaintenance.entity.User;
import com.carmaintenance.exception.BusinessException;
import com.carmaintenance.repository.ShopRepository;
import com.carmaintenance.service.ShopService;
import com.carmaintenance.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 门店Service实现类
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopRepository, Shop> implements ShopService {

    private static final Logger logger = LoggerFactory.getLogger(ShopServiceImpl.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public String registerShop(ShopRegisterDTO registerDTO) {
        logger.info("门店注册尝试: shopName={}, username={}, email={}", 
                   registerDTO.getShopName(), registerDTO.getUsername(), registerDTO.getEmail());
        
        // 验证密码确认
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            logger.warn("门店注册失败: 密码不一致, username={}", registerDTO.getUsername());
            throw new BusinessException(400, "两次输入的密码不一致");
        }

        // 检查用户名是否已存在
        if (userService.findByUsername(registerDTO.getUsername()) != null) {
            logger.warn("门店注册失败: 用户名已存在, username={}", registerDTO.getUsername());
            throw new BusinessException(400, "用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userService.findByEmail(registerDTO.getEmail()) != null) {
            logger.warn("门店注册失败: 邮箱已被注册, email={}", registerDTO.getEmail());
            throw new BusinessException(400, "邮箱已被注册");
        }

        // 创建用户对象
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword())); // 使用BCrypt加密
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        user.setRealName(registerDTO.getShopName());
        user.setUserType(2); // 门店用户
        user.setStatus(0); // 待审核状态

        // 保存用户
        if (!userService.save(user)) {
            logger.error("门店注册失败: 用户创建失败, username={}", registerDTO.getUsername());
            throw new BusinessException(500, "用户创建失败");
        }

        // 创建门店对象
        Shop shop = new Shop();
        shop.setUserId(user.getId());
        shop.setShopName(registerDTO.getShopName());
        shop.setAddress(registerDTO.getAddress());
        shop.setProvince(registerDTO.getProvince());
        shop.setCity(registerDTO.getCity());
        shop.setDistrict(registerDTO.getDistrict());

        if (registerDTO.getLongitude() != null) {
            shop.setLongitude(BigDecimal.valueOf(registerDTO.getLongitude()));
        }
        if (registerDTO.getLatitude() != null) {
            shop.setLatitude(BigDecimal.valueOf(registerDTO.getLatitude()));
        }

        shop.setPhone(registerDTO.getPhone());
        shop.setBusinessHours(registerDTO.getBusinessHours());
        shop.setShopBays(registerDTO.getShopBays());
        shop.setDescription(registerDTO.getDescription());
        shop.setStatus(0); // 待审核状态
        // shop.setRating(BigDecimal.valueOf(5.0)); // rating字段不存在于数据库

        // 保存门店
        if (save(shop)) {
            logger.info("门店注册成功: shopName={}, shopId={}, userId={}", 
                       shop.getShopName(), shop.getId(), user.getId());
            return "门店注册成功，请等待管理员审核";
        } else {
            logger.error("门店注册失败: 数据库保存失败, shopName={}", registerDTO.getShopName());
            throw new BusinessException(500, "门店注册失败");
        }
    }

    @Override
    public List<Shop> getShopsByCity(String city) {
        if (!StringUtils.hasText(city)) {
            return list(new QueryWrapper<Shop>().eq("status", 1).orderByDesc("create_time"));
        }
        return baseMapper.findByCity(city);
    }

    @Override
    public List<Shop> searchShops(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return list(new QueryWrapper<Shop>().eq("status", 1).orderByDesc("create_time"));
        }
        return baseMapper.searchShops(keyword);
    }

    @Override
    public Page<Shop> getShopPage(Page<Shop> page, String city, String keyword, Integer status) {
        QueryWrapper<Shop> wrapper = new QueryWrapper<>();

        // 按状态过滤
        if (status != null) {
            wrapper.eq("status", status);
        } else {
            wrapper.eq("status", 1); // 默认只显示正常状态的门店
        }

        // 按城市过滤
        if (StringUtils.hasText(city)) {
            wrapper.eq("city", city);
        }

        // 按关键词搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("shop_name", keyword)
                    .or().like("address", keyword)
                    .or().like("description", keyword));
        }

        wrapper.orderByDesc("create_time");

        return page(page, wrapper);
    }

    @Override
    @Transactional
    public boolean updateShopStatus(Long shopId, Integer status) {
        Shop shop = getById(shopId);
        if (shop == null) {
            throw new RuntimeException("门店不存在");
        }

        shop.setStatus(status);
        boolean result = updateById(shop);

        // 如果审核通过，同时激活用户账号
        if (result && status == 1) {
            User user = userService.getById(shop.getUserId());
            if (user != null) {
                user.setStatus(1);
                userService.updateById(user);
            }
        }

        return result;
    }

    @Override
    public Shop getShopByUserId(Long userId) {
        return baseMapper.findByUserId(userId);
    }

    @Override
    @Transactional
    public boolean updateShopInfo(Long shopId, Shop shop) {
        Shop existingShop = getById(shopId);
        if (existingShop == null) {
            throw new BusinessException(404, "门店不存在");
        }

        // 只允许更新特定字段
        existingShop.setShopName(shop.getShopName());
        existingShop.setAddress(shop.getAddress());
        existingShop.setProvince(shop.getProvince());
        existingShop.setCity(shop.getCity());
        existingShop.setDistrict(shop.getDistrict());
        existingShop.setLongitude(shop.getLongitude());
        existingShop.setLatitude(shop.getLatitude());
        existingShop.setPhone(shop.getPhone());
        existingShop.setBusinessHours(shop.getBusinessHours());
        existingShop.setShopBays(shop.getShopBays());
        existingShop.setDescription(shop.getDescription());
        existingShop.setLogoUrl(shop.getLogoUrl());

        return updateById(existingShop);
    }

    @Override
    public Shop getShopStats(Long shopId) {
        Shop shop = getById(shopId);
        if (shop == null) {
            throw new RuntimeException("门店不存在");
        }

        // 这里可以添加更多统计信息，比如订单数量、营业额等
        return shop;
    }

    @Override
    public Shop authenticateShop(String username, String password) {
        try {
            // 通过用户名查找用户
            User user = userService.findByUsername(username);
            if (user == null) {
                logger.warn("门店登录失败: 用户不存在, username={}", username);
                return null;
            }

            // 验证用户角色是否为门店
            if (user.getUserType() != 2) { // 2 = SHOP角色
                logger.warn("门店登录失败: 用户角色不是门店, username={}, userType={}", username, user.getUserType());
                return null;
            }

            // 验证密码
            if (!passwordEncoder.matches(password, user.getPassword())) {
                logger.warn("门店登录失败: 密码错误, username={}", username);
                return null;
            }

            // 获取门店信息
            Shop shop = getShopByUserId(user.getId());
            if (shop == null) {
                logger.warn("门店登录失败: 门店信息不存在, username={}", username);
                return null;
            }

            logger.info("门店登录成功: username={}, shopId={}", username, shop.getId());
            return shop;
        } catch (Exception e) {
            logger.error("门店登录异常: username=" + username, e);
            return null;
        }
    }

    @Override
    @Transactional
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        try {
            User user = userService.getById(userId);
            if (user == null) {
                logger.error("修���密码失败: 用户不存在, userId={}", userId);
                return false;
            }

            // 验证原密码
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                logger.warn("修改密码失败: 原密码错误, userId={}", userId);
                return false;
            }

            // 更新密码
            user.setPassword(passwordEncoder.encode(newPassword));
            boolean result = userService.updateById(user);

            if (result) {
                logger.info("密码修改成功: userId={}", userId);
            } else {
                logger.error("密码修改失败: userId={}", userId);
            }

            return result;
        } catch (Exception e) {
            logger.error("修改密码异常: userId=" + userId, e);
            return false;
        }
    }
}