package com.heima.vod.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.vod.dao.VodConfigDao;
import com.heima.vod.entity.VodConfigEntity;
import com.heima.vod.service.VodConfigService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 阿里云VOD配置服务实现类
 */
@Service
public class VodConfigServiceImpl extends ServiceImpl<VodConfigDao, VodConfigEntity> implements VodConfigService {
    
    @Override
    public VodConfigEntity getEnabledConfig() {
        QueryWrapper<VodConfigEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("enable", true);
        return this.getOne(queryWrapper);
    }
    
    @Override
    @Cacheable(value = "vodConfig", key = "'enabled'")
    public VodConfigEntity getEnabledConfigWithCache() {
        return getEnabledConfig();
    }
}