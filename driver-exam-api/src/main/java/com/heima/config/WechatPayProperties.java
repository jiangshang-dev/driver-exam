package com.heima.config;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wechat.pay")
public class WechatPayProperties {
    private boolean enabled;
    private String appId;
    private String mchId;
    private String apiKey;
    private String notifyUrl;

    public boolean ready() {
        return enabled && StrUtil.isAllNotBlank(appId, mchId, apiKey, notifyUrl);
    }
}
