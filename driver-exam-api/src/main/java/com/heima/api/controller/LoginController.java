package com.heima.api.controller;

import com.heima.business.auth.dto.LoginResult;
import com.heima.business.auth.dto.WechatMiniLoginRequest;
import com.heima.business.auth.service.AuthService;
import com.heima.common.utils.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录认证接口
 */
@Slf4j
@RestController
@RequestMapping("/login")
public class LoginController {

    @Resource
    private AuthService authService;
    
    /**
     * 微信小程序登录
     */
    @PostMapping("/wxMin")
    public Result<Map<String, Object>> wechatMiniLogin(@RequestBody WechatMiniLoginRequest request) {
        try {
            log.info("接收到微信小程序登录请求: code={}, type={}", request.getCode(), request.getType());

            // 调用真正的微信登录服务
            LoginResult loginResult = authService.wechatMiniLogin(request);

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("id", loginResult.getId());
            result.put("account", loginResult.getAccount());
            result.put("nickname", loginResult.getNickname());
            result.put("mobile", loginResult.getMobile());
            result.put("token", loginResult.getToken());
            result.put("vipLevel", loginResult.getVipLevel());
            result.put("level", loginResult.getLevel());
            result.put("avatar", loginResult.getAvatar());
            
            log.info("微信小程序登录成功: userId={}, nickname={}", loginResult.getId(), loginResult.getNickname());
            return new Result<Map<String, Object>>().ok(result);
            
        } catch (Exception e) {
            log.error("微信小程序登录失败", e);
            return new Result<Map<String, Object>>().error("登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 微信小程序简单登录（用于测试）
     */
    @PostMapping("/wxMin/simple")
    public Result<Map<String, Object>> wechatMiniSimpleLogin(@RequestParam String phoneNumber) {
        try {
            log.info("接收到微信小程序简单登录请求: phoneNumber={}", phoneNumber);
            
            // 模拟登录结果（保留用于测试）
            Map<String, Object> result = new HashMap<>();
            result.put("id", 1L);
            result.put("account", "test_user");
            result.put("nickname", "测试用户");
            result.put("mobile", phoneNumber);
            result.put("token", "test_token_" + System.currentTimeMillis());
            result.put("vipLevel", 0);
            result.put("level", 1);
            result.put("avatar", "https://thirdwx.qlogo.cn/mmopen/default_avatar.png");
            
            return new Result<Map<String, Object>>().ok(result);
            
        } catch (Exception e) {
            log.error("微信小程序简单登录失败", e);
            return new Result<Map<String, Object>>().error("登录失败: " + e.getMessage());
        }
    }
}