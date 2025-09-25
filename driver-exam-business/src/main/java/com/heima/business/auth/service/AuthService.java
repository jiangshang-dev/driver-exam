package com.heima.business.auth.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.heima.business.auth.dto.LoginResult;
import com.heima.business.auth.dto.WechatMiniLoginRequest;
import com.heima.business.auth.dto.WechatUserInfo;
import com.heima.business.auth.entity.ApUserEntity;
import com.heima.business.auth.entity.ApUserOauthEntity;
import com.heima.business.auth.provider.WechatAuthProvider;
import com.heima.common.utils.JwtUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务
 */
@Slf4j
@Service
public class AuthService {
    
    @Resource
    private WechatAuthProvider wechatAuthProvider;
    
    @Resource
    private UserService userService;
    
    @Resource
    private OauthService oauthService;
    
    /**
     * 微信小程序登录
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginResult wechatMiniLogin(WechatMiniLoginRequest request) {
        try {
            log.info("微信小程序登录请求: {}", JSONUtil.toJsonStr(request));
            
            // 1. 获取微信用户基本信息
            WechatUserInfo wechatUser = wechatAuthProvider.getUserInfoByCode(request.getCode());
            
            // 2. 解密手机号(如果提供了加密数据)
            String phoneNumber = null;
            if (StrUtil.isNotBlank(request.getEncryptedData()) && 
                StrUtil.isNotBlank(request.getIv()) && 
                StrUtil.isNotBlank(wechatUser.getSessionKey())) {
                phoneNumber = wechatAuthProvider.decryptPhoneNumber(
                    request.getEncryptedData(), 
                    request.getIv(), 
                    wechatUser.getSessionKey()
                );
                wechatUser.setPhoneNumber(phoneNumber);
            }
            
            // 3. 查找或创建用户
            ApUserEntity user = findOrCreateUser(wechatUser);
            
            // 4. 更新登录时间
            userService.updateLastLoginTime(user.getId());
            
            // 5. 生成JWT token
            String token = JwtUtils.generateToken(user.getId());
            
            // 6. 构建返回结果
            LoginResult result = new LoginResult();
            result.setId(user.getId());
            result.setAccount(user.getUsername());
            result.setNickname(user.getRealName());
            result.setAvatar(user.getAvatar());
            result.setMobile(user.getPhone());
            result.setToken(token);
            result.setVipLevel(user.getVipLevel()); // 默认非VIP
            result.setLevel(1);    // 默认等级
            
            log.info("微信小程序登录成功: userId={}, openId={}", user.getId(), wechatUser.getOpenId());
            return result;
            
        } catch (Exception e) {
            log.error("微信小程序登录失败", e);
            throw new RuntimeException("登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 查找或创建用户
     */
    private ApUserEntity findOrCreateUser(WechatUserInfo wechatUser) {
        // 1. 先根据微信openId查找是否已绑定
        ApUserOauthEntity oauth = oauthService.findByOauthId("wechat_mini", wechatUser.getOpenId());
        
        if (oauth != null) {
            // 已绑定，直接返回用户信息
            ApUserEntity user = userService.getById(oauth.getUserId());
            
            // 如果这次登录获取到了手机号，但用户表中没有，则更新手机号
            if (StrUtil.isNotBlank(wechatUser.getPhoneNumber()) && 
                StrUtil.isBlank(user.getPhone())) {
                ApUserEntity updateUser = new ApUserEntity();
                updateUser.setId(user.getId());
                updateUser.setPhone(wechatUser.getPhoneNumber());
                updateUser.setUpdateTime(LocalDateTime.now());
                updateUser.setVipLevel(user.getVipLevel());
                userService.updateById(updateUser);
                user.setPhone(wechatUser.getPhoneNumber());
            }
            
            return user;
        }
        
        // 2. 如果有手机号，尝试根据手机号查找现有用户
        ApUserEntity existUser = null;
        if (StrUtil.isNotBlank(wechatUser.getPhoneNumber())) {
            existUser = userService.findByPhone(wechatUser.getPhoneNumber());
        }

        ApUserEntity user;
        if (existUser != null) {
            // 找到现有用户，直接绑定微信
            user = existUser;
        } else {
            // 创建新用户
            user = createNewUser(wechatUser);
        }
        
        // 3. 创建OAuth绑定
        createOauthBinding(user.getId(), wechatUser);
        
        return user;
    }
    
    /**
     * 创建新用户
     */
    private ApUserEntity createNewUser(WechatUserInfo wechatUser) {
        ApUserEntity user = new ApUserEntity();
        user.setUsername("wx_" + wechatUser.getOpenId());
        user.setRealName(StrUtil.isNotBlank(wechatUser.getNickname()) ? wechatUser.getNickname() : "微信用户");
        user.setAvatar(wechatUser.getHeadImgUrl());
        user.setPhone(wechatUser.getPhoneNumber());
        user.setGender(wechatUser.getSex());
        user.setVipLevel(0); // 默认非VIP
        user.setStatus(1); // 正常状态
        
        userService.save(user);
        log.info("创建新用户成功: userId={}, openId={}", user.getId(), wechatUser.getOpenId());
        
        return user;
    }
    
    /**
     * 创建OAuth绑定
     */
    private void createOauthBinding(Long userId, WechatUserInfo wechatUser) {
        ApUserOauthEntity oauth = new ApUserOauthEntity();
        oauth.setUserId(userId);
        oauth.setOauthType("wechat_mini");
        oauth.setOauthId(wechatUser.getOpenId());
        oauth.setUnionId(wechatUser.getUnionId());
        oauth.setOauthInfo(JSONUtil.toJsonStr(wechatUser));
        
        oauthService.save(oauth);
        log.info("创建OAuth绑定成功: userId={}, openId={}", userId, wechatUser.getOpenId());
    }
}