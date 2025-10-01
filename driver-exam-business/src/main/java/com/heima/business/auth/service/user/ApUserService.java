package com.heima.business.auth.service.user;

import com.heima.business.auth.dto.ApUserDTO;
import com.heima.business.auth.entity.ApUserEntity;
import com.heima.common.service.CrudService;

public interface ApUserService extends CrudService<ApUserEntity, ApUserDTO> {
    public void save(ApUserEntity user);
    public boolean updateById(ApUserEntity user);
    public ApUserEntity getById(Long id);
    public void updateLastLoginTime(Long userId);
    public ApUserEntity findByPhone(String phone);
}
