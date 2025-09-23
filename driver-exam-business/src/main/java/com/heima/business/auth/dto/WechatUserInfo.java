package com.heima.business.auth.dto;

import lombok.Data;

/**
 * 微信用户信息
 */
@Data
public class WechatUserInfo {
    
    /** 微信OpenID */
    private String openId;
    
    /** 微信UnionID */
    private String unionId;
    
    /** 昵称 */
    private String nickname;
    
    /** 头像 */
    private String headImgUrl;
    
    /** 性别 */
    private Integer sex;
    
    /** 手机号 */
    private String phoneNumber;
    
    /** 会话密钥 */
    private String sessionKey;
}