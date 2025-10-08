package com.heima.controller.auth;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.heima.common.utils.JwtUtils;
import org.springframework.web.bind.annotation.*;

import com.heima.annotation.Log;
import com.heima.business.auth.dto.LoginResult;
import com.heima.business.auth.dto.WechatMiniLoginRequest;
import com.heima.business.auth.entity.ApUserEntity;
import com.heima.business.auth.service.user.ApUserService;
import com.heima.business.auth.service.user.AuthService;
import com.heima.common.utils.Result;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 登录认证接口
 */
@Slf4j
@RestController
@RequestMapping("/login")
public class LoginController {

    @Resource
    private AuthService authService;

    @Resource
    private ApUserService userService;

    /**
     * 微信小程序登录
     */
    @Log(type = "wechat", description = "微信小程序登录", isLogin = true)
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
            result.put("vipExpireTime", loginResult.getVipExpireTime());
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
    @Log(type = "wechat_simple", description = "微信小程序简单登录（测试）", isLogin = true)
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

    /**
     * 检查会员状态
     */
    @GetMapping("/checkVipStatus")
    public Result<Map<String, Object>> checkVipStatus(@RequestHeader(value = "Authorization") String token) {
        try {
            log.info("接收到会员状态检查请求: token={}", token);

            Long userId = JwtUtils.getUserId(token);
            if (userId == null) {
                return new Result<Map<String, Object>>().error("用户未登录");
            }
            // 获取用户最新信息
            ApUserEntity user = userService.getById(userId);

            if (user == null) {
                return new Result<Map<String, Object>>().error("用户不存在");
            }

            // 检查会员是否过期
            boolean isVip = user.getVipExpireTime() != null
                    && user.getVipExpireTime().isAfter(LocalDateTime.now());

            // 如果会员已过期，更新用户信息
            if (!isVip && user.getVipLevel() > 0) {
                ApUserEntity updateUser = new ApUserEntity();
                updateUser.setId(user.getId());
                updateUser.setVipLevel(0);
                updateUser.setUpdateTime(LocalDateTime.now());
                userService.updateById(updateUser);

                // 更新返回的用户信息
                user.setVipLevel(0);
            }

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("id", user.getId());
            result.put("vipLevel", user.getVipLevel());
            result.put("vipExpireTime", user.getVipExpireTime());

            log.info("会员状态检查完成: userId={}, isVip={}", userId, isVip);
            return new Result<Map<String, Object>>().ok(result);

        } catch (Exception e) {
            log.error("会员状态检查失败", e);
            return new Result<Map<String, Object>>().error("检查失败: " + e.getMessage());
        }
    }
}
