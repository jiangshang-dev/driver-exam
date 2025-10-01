package com.heima.business.auth.service.user.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.business.auth.dao.ApUserDao;
import com.heima.business.auth.dto.ApUserDTO;
import com.heima.business.auth.entity.ApUserEntity;
import com.heima.business.auth.service.user.ApUserService;
import com.heima.common.service.impl.CrudServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户服务
 */
@Slf4j
@Service
public class ApUserServiceImpl extends CrudServiceImpl<ApUserDao, ApUserEntity, ApUserDTO> implements ApUserService {
    
    @Resource
    private ApUserDao authUserDao;

    @Override
    public QueryWrapper<ApUserEntity> getWrapper(Map<String, Object> params){
        String id = (String)params.get("id");

        QueryWrapper<ApUserEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(id), "id", id);

        return wrapper;
    }

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
     *
     * @return
     */
    @Override
    public boolean updateById(ApUserEntity user) {
        user.setUpdateTime(LocalDateTime.now());
        authUserDao.updateById(user);
        return false;
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