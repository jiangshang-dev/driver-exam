package com.heima.business.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.business.auth.dao.ApUserOauthDao;
import com.heima.business.auth.entity.ApUserOauthEntity;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 第三方登录绑定服务
 */
@Slf4j
@Service
public class OauthService {
    
    @Resource
    private ApUserOauthDao userOauthDao;
    
    /**
     * 根据第三方ID查找绑定关系
     */
    public ApUserOauthEntity findByOauthId(String oauthType, String oauthId) {
        return userOauthDao.selectOne(new LambdaQueryWrapper<ApUserOauthEntity>()
                .eq(ApUserOauthEntity::getOauthType, oauthType)
                .eq(ApUserOauthEntity::getOauthId, oauthId));
    }
    
    /**
     * 保存OAuth绑定
     */
    public void save(ApUserOauthEntity oauth) {
        oauth.setCreateTime(LocalDateTime.now());
        userOauthDao.insert(oauth);
    }
    
    /**
     * 根据用户ID和类型查找绑定
     */
    public ApUserOauthEntity findByUserIdAndType(Long userId, String oauthType) {
        return userOauthDao.selectOne(new LambdaQueryWrapper<ApUserOauthEntity>()
                .eq(ApUserOauthEntity::getUserId, userId)
                .eq(ApUserOauthEntity::getOauthType, oauthType));
    }
}