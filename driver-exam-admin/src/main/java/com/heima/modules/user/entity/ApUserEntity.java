package com.heima.modules.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 用户表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-01
 */
@Data
@TableName("ap_user")
public class ApUserEntity {

    /**
     * 
     */
	private Long id;
    /**
     * 用户名
     */
	private String username;
    /**
     * 密码
     */
	private String password;
    /**
     * 真实姓名
     */
	private String realName;
    /**
     * 头像
     */
	private String avatar;
    /**
     * 手机号
     */
	private String phone;
    /**
     * 邮箱
     */
	private String email;
    /**
     * 性别 0:未知 1:男 2:女
     */
	private Integer gender;
    /**
     * 状态 0:禁用 1:正常
     */
	private Integer status;
    /**
     * 是否为VIP
     */
	private Integer vipLevel;
    /**
     * 最后登录时间
     */
	private Date lastLoginTime;
    /**
     * 创建时间
     */
	private Date createTime;
    /**
     * 更新时间
     */
	private Date updateTime;
}