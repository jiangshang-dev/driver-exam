package com.heima.business.auth.service;

import com.heima.business.auth.entity.ApUserEntity;

public interface ApUserService {
    public void save(ApUserEntity user);
    public void updateById(ApUserEntity user);
    public ApUserEntity getById(Long id);
    public void updateLastLoginTime(Long userId);
    public ApUserEntity findByPhone(String phone);
}
