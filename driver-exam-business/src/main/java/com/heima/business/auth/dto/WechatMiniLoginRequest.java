package com.heima.business.auth.dto;

import lombok.Data;

/**
 * 微信小程序登录请求
 */
@Data
public class WechatMiniLoginRequest {
    
    /** 微信登录code */
    private String code;
    
    /** 手机号加密数据 */
    private String encryptedData;
    
    /** 加密算法初始向量 */
    private String iv;
    
    /** 手机号授权code */
    private String codeev;
    
    /** 应用类型 */
    private String type;
}