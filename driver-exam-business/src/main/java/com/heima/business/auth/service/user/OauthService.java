package com.heima.business.auth.service.user;

import com.heima.business.auth.entity.ApUserOauthEntity;

public interface OauthService {
    public ApUserOauthEntity findByOauthId(String oauthType, String oauthId);
    public void save(ApUserOauthEntity oauth);
    public ApUserOauthEntity findByUserIdAndType(Long userId, String oauthType);
}
