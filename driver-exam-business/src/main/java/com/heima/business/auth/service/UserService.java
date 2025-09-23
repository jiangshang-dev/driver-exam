package com.heima.business.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.business.auth.dao.ApUserDao;
import com.heima.business.auth.dao.ApUserOauthDao;
import com.heima.business.auth.entity.ApUserEntity;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户服务
 */
@Slf4j
@Service
public class UserService {
    
    @Resource
    private ApUserDao authUserDao;
    
    @Resource
    private ApUserOauthDao userOauthDao;
    
    /**
     * 保存用户
     */
    public void save(ApUserEntity user) {
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        authUserDao.insert(user);
    }
    
    /**
     * 更新用户
     */
    public void updateById(ApUserEntity user) {
        user.setUpdateTime(LocalDateTime.now());
        authUserDao.updateById(user);
    }
    
    /**
     * 根据ID获取用户
     */
    public ApUserEntity getById(Long id) {
        return authUserDao.selectById(id);
    }
    
    /**
     * 更新最后登录时间
     */
    public void updateLastLoginTime(Long userId) {
        ApUserEntity user = new ApUserEntity();
        user.setId(userId);
        user.setLastLoginTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        authUserDao.updateById(user);
    }
    
    /**
     * 根据手机号查找用户
     */
    public ApUserEntity findByPhone(String phone) {
        return authUserDao.selectOne(new LambdaQueryWrapper<ApUserEntity>()
                .eq(ApUserEntity::getPhone, phone)
                .eq(ApUserEntity::getStatus, 1));
    }
}