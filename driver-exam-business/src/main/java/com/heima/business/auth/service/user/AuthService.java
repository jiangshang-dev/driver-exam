package com.heima.business.auth.service.user;

import com.heima.business.auth.dto.LoginResult;
import com.heima.business.auth.dto.WechatMiniLoginRequest;

public interface AuthService {
    LoginResult wechatMiniLogin(WechatMiniLoginRequest request);
}
