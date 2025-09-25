package com.heima.business.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.business.auth.dao.ApUserDao;
import com.heima.business.auth.entity.ApUserEntity;
import com.heima.business.auth.service.ApUserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户服务
 */
@Slf4j
@Service
public class ApUserServiceImpl implements ApUserService {
    
    @Resource
    private ApUserDao authUserDao;

    /**
     * 保存用户
     */
    @Override
    public void save(ApUserEntity user) {
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        authUserDao.insert(user);
    }
    
    /**
     * 更新用户
     */
    @Override
    public void updateById(ApUserEntity user) {
        user.setUpdateTime(LocalDateTime.now());
        authUserDao.updateById(user);
    }
    
    /**
     * 根据ID获取用户
     */
    @Override
    public ApUserEntity getById(Long id) {
        return authUserDao.selectById(id);
    }
    
    /**
     * 更新最后登录时间
     */
    @Override
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
    @Override
    public ApUserEntity findByPhone(String phone) {
        return authUserDao.selectOne(new LambdaQueryWrapper<ApUserEntity>()
                .eq(ApUserEntity::getPhone, phone)
                .eq(ApUserEntity::getStatus, 1));
    }
}