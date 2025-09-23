package com.heima.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * JWT工具类（简化版）
 */
@Slf4j
public class JwtUtils {
    
    /**
     * 生成token（简化实现）
     */
    public static String generateToken(Long userId) {
        // 简化实现：使用UUID + userId作为token
        return "token_" + userId + "_" + UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 解析token获取用户ID
     */
    public static Long getUserId(String token) {
        try {
            if (token != null && token.startsWith("token_")) {
                String[] parts = token.split("_");
                if (parts.length >= 2) {
                    return Long.valueOf(parts[1]);
                }
            }
            return null;
        } catch (Exception e) {
            log.error("解析token失败", e);
            return null;
        }
    }
    
    /**
     * 验证token是否有效
     */
    public static boolean validateToken(String token) {
        return token != null && token.startsWith("token_") && getUserId(token) != null;
    }
}