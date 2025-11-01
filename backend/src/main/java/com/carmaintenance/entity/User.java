package com.carmaintenance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("users")
public class User {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("email")
    private String email;

    @TableField("phone")
    private String phone;

    @TableField("user_type")
    private Integer userType; // 1-车主, 2-门店, 3-管理员

    @TableField("real_name")
    private String realName;

    @TableField("avatar_url")
    private String avatarUrl;

    @TableField("status")
    private Integer status; // 0-禁用, 1-正常

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public enum UserType {
        CUSTOMER(1, "车主"),
        SHOP(2, "门店"),
        ADMIN(3, "管理员");

        private final Integer code;
        private final String desc;

        UserType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static UserType fromCode(Integer code) {
            for (UserType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown user type code: " + code);
        }
    }
}