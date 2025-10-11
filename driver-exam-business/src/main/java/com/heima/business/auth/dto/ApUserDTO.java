package com.heima.business.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;


/**
 * 用户表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-01
 */
@Data
@Schema(name = "用户表")
public class ApUserDTO implements Serializable {
    private static final long serialVersionUID = 1L;

	@SchemaProperty(name = "")
	private Long id;

	@SchemaProperty(name = "用户名")
	private String username;

	@SchemaProperty(name = "密码")
	private String password;

	@SchemaProperty(name = "真实姓名")
	private String realName;

	@SchemaProperty(name = "头像")
	private String avatar;

	@SchemaProperty(name = "手机号")
	private String phone;

	@SchemaProperty(name = "邮箱")
	private String email;

	@SchemaProperty(name = "性别 0:未知 1:男 2:女")
	private Integer gender;

	@SchemaProperty(name = "积分")
	private Integer integral;

	@SchemaProperty(name = "会员到期时间")
	private Date vipExpireTime;

	/** 最后积分更新时间 */
	@SchemaProperty(name = "最后积分更新时间")
	public Date lastIntegralUpdate;

	@SchemaProperty(name = "状态 0:禁用 1:正常")
	private Integer status;

	@SchemaProperty(name = "是否为VIP")
	private Integer vipLevel;

	@SchemaProperty(name = "最后登录时间")
	private Date lastLoginTime;

	@SchemaProperty(name = "创建时间")
	private Date createTime;

	@SchemaProperty(name = "更新时间")
	private Date updateTime;


}
