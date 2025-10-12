package com.heima.business.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("ap_user")
public class ApUserEntity {
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /** 用户名 */
    private String username;
    
    /** 密码 */
    private String password;
    
    /** 真实姓名 */
    private String realName;
    
    /** 头像 */
    private String avatar;
    
    /** 手机号 */
    private String phone;
    
    /** 邮箱 */
    private String email;

    /** 会员等级 0:普通用户 1:VIP用户 */
    private Integer vipLevel;

    /** 积分 */
    private Integer integral;

    /** 评论权限：0-禁止评论，1-允许评论 */
    private Boolean commentPermission;

    /** 违规评论次数 */
    private Integer violationCommentCount;

    /** 会员到期时间 */
    private LocalDateTime vipExpireTime;

    /** 性别 0:未知 1:男 2:女 */
    private Integer gender;
    
    /** 状态 0:禁用 1:正常 */
    private Boolean status;
    
    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 最后积分更新时间 */
    public LocalDate lastIntegralUpdate;
}