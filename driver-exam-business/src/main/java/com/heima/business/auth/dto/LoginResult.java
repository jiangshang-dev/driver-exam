package com.heima.business.auth.dto;

import lombok.Data;

/**
 * 登录响应结果
 */
@Data
public class LoginResult {
    
    /** 用户ID */
    private Long id;
    
    /** 头像 */
    private String avatar;
    
    /** 账户名 */
    private String account;
    
    /** 昵称 */
    private String nickname;
    
    /** 手机号 */
    private String mobile;
    
    /** 登录凭证 */
    private String token;
    
    /** VIP等级 */
    private Integer vipLevel;
    
    /** 用户等级 */
    private Integer level;
}