package com.carmaintenance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carmaintenance.dto.UserLoginDTO;
import com.carmaintenance.dto.UserRegisterDTO;
import com.carmaintenance.entity.User;
import com.carmaintenance.exception.BusinessException;
import com.carmaintenance.repository.UserRepository;
import com.carmaintenance.service.UserService;
import com.carmaintenance.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户Service实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserRepository, User> implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public String register(UserRegisterDTO registerDTO) {
        logger.info("用户注册尝试: username={}, email={}", registerDTO.getUsername(), registerDTO.getEmail());
        
        // 验证密码确认
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            logger.warn("用户注册失败: 密码不一致, username={}", registerDTO.getUsername());
            throw new BusinessException(400, "两次输入的密码不一致");
        }

        // 检查用户名是否已存在
        if (findByUsername(registerDTO.getUsername()) != null) {
            logger.warn("用户注册失败: 用户名已存在, username={}", registerDTO.getUsername());
            throw new BusinessException(400, "用户名已存在");
        }

        // 检查邮箱是否已存在
        if (findByEmail(registerDTO.getEmail()) != null) {
            logger.warn("用户注册失败: 邮箱已被注册, email={}", registerDTO.getEmail());
            throw new BusinessException(400, "邮箱已被注册");
        }

        // 创建用户对象
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword())); // 使用BCrypt加密
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        user.setRealName(registerDTO.getRealName());
        user.setUserType(registerDTO.getUserType());
        user.setStatus(1); // 默认正常状态

        // 保存用户
        if (save(user)) {
            logger.info("用户注册成功: username={}, email={}, userId={}", user.getUsername(), user.getEmail(), user.getId());
            return "注册成功";
        } else {
            logger.error("用户注册失败: 数据库保存失败, username={}", registerDTO.getUsername());
            throw new BusinessException(500, "注册失败");
        }
    }

    @Override
    public String login(UserLoginDTO loginDTO) {
        logger.info("用户登录尝试: {}", loginDTO.getUsername());
        
        // 支持用户名或邮箱登录
        User user = findByUsername(loginDTO.getUsername());
        if (user == null) {
            user = findByEmail(loginDTO.getUsername());
        }

        if (user == null) {
            logger.warn("用户登录失败: 用户不存在, username={}", loginDTO.getUsername());
            throw new BusinessException(401, "用户不存在");
        }

        // 验证密码（使用BCrypt验证）
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            logger.warn("用户登录失败: 密码错误, username={}, userId={}", user.getUsername(), user.getId());
            throw new BusinessException(401, "密码错误");
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            logger.warn("用户登录失败: 账户已被禁用, username={}, userId={}", user.getUsername(), user.getId());
            throw new BusinessException(403, "账户已被禁用");
        }

        // 生成JWT Token
        logger.info("用户登录成功: username={}, userId={}", user.getUsername(), user.getId());
        return jwtUtils.generateToken(user.getId(), user.getUsername(), user.getUserType());
    }

    @Override
    public User findByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        return getOne(wrapper);
    }

    @Override
    public User findByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("email", email);
        return getOne(wrapper);
    }

    @Override
    public Page<User> getUserPage(Page<User> page, Integer userType, String keyword) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();

        // 按用户类型过滤
        if (userType != null) {
            wrapper.eq("user_type", userType);
        }

        // 按关键词搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("username", keyword)
                    .or().like("real_name", keyword)
                    .or().like("email", keyword)
                    .or().like("phone", keyword));
        }

        wrapper.orderByDesc("create_time");

        return page(page, wrapper);
    }

    @Override
    @Transactional
    public boolean updateUserStatus(Long userId, Integer status) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        user.setStatus(status);
        return updateById(user);
    }

    @Override
    @Transactional
    public boolean resetPassword(String email, String newPassword) {
        User user = findByEmail(email);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        user.setPassword(passwordEncoder.encode(newPassword)); // 使用BCrypt加密
        return updateById(user);
    }
}