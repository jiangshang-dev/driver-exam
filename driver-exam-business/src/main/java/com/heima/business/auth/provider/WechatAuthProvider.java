package com.heima.business.auth.provider;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.heima.business.auth.dto.WechatUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信授权提供者
 */
@Slf4j
@Component
public class WechatAuthProvider {
    
    @Value("${auth.wechat.app-id}")
    private String appId;
    
    @Value("${auth.wechat.app-secret}")
    private String appSecret;
    
    private static final String CODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";
    
    /**
     * 通过code获取微信用户信息
     */
    public WechatUserInfo getUserInfoByCode(String code) {
        try {
            // 1. 调用微信接口获取session_key和openid
            Map<String, Object> params = new HashMap<>();
            params.put("appid", appId);
            params.put("secret", appSecret);
            params.put("js_code", code);
            params.put("grant_type", "authorization_code");
            
            String response = HttpUtil.get(CODE2SESSION_URL, params);
            log.info("微信code2session响应: {}", response);
            
            JSONObject jsonResponse = JSONUtil.parseObj(response);
            
            if (jsonResponse.containsKey("errcode")) {
                log.error("微信登录失败: {}", jsonResponse.getStr("errmsg"));
                throw new RuntimeException("微信登录失败: " + jsonResponse.getStr("errmsg"));
            }
            
            WechatUserInfo userInfo = new WechatUserInfo();
            userInfo.setOpenId(jsonResponse.getStr("openid"));
            userInfo.setUnionId(jsonResponse.getStr("unionid"));
            userInfo.setSessionKey(jsonResponse.getStr("session_key"));
            
            return userInfo;
            
        } catch (Exception e) {
            log.error("获取微信用户信息失败", e);
            throw new RuntimeException("获取微信用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 解密微信手机号
     */
    public String decryptPhoneNumber(String encryptedData, String iv, String sessionKey) {
        try {
            byte[] sessionKeyBytes = Base64.getDecoder().decode(sessionKey);
            byte[] ivBytes = Base64.getDecoder().decode(iv);
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
            
            SecretKeySpec keySpec = new SecretKeySpec(sessionKeyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            String decryptedData = new String(decryptedBytes, StandardCharsets.UTF_8);
            
            log.info("解密后的手机号数据: {}", decryptedData);
            
            JSONObject phoneInfo = JSONUtil.parseObj(decryptedData);
            return phoneInfo.getStr("phoneNumber");
            
        } catch (Exception e) {
            log.error("解密微信手机号失败", e);
            throw new RuntimeException("解密手机号失败: " + e.getMessage());
        }
    }
}
