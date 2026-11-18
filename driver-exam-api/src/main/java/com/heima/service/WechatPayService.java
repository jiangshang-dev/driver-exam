package com.heima.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.business.auth.dao.ApUserOauthDao;
import com.heima.business.auth.entity.ApUserOauthEntity;
import com.heima.business.mall.entity.ApMallOrderEntity;
import com.heima.common.exception.RenException;
import com.heima.config.WechatPayProperties;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 微信小程序 JSAPI 支付，使用商户平台 v2 统一下单。
 */
@Service
public class WechatPayService {

    @Resource
    private WechatPayProperties properties;
    @Resource
    private ApUserOauthDao oauthDao;

    public boolean ready() {
        return properties.ready();
    }

    public Map<String, String> jsapi(ApMallOrderEntity order) {
        if (!properties.ready()) {
            throw new RenException("微信支付未配置");
        }
        if (order.getCashAmount() == null || order.getCashAmount() <= 0) {
            throw new RenException("订单金额不正确");
        }
        String openId = findOpenId(order.getUserId());
        if (StrUtil.isBlank(openId)) {
            throw new RenException("没有找到微信 openId，请重新登录后再支付");
        }
        String nonce = nonce();
        Map<String, String> params = new TreeMap<>();
        params.put("appid", properties.getAppId());
        params.put("mch_id", properties.getMchId());
        params.put("nonce_str", nonce);
        params.put("body", "驾考通-" + order.getGoodsName());
        params.put("out_trade_no", order.getOrderNo());
        params.put("total_fee", String.valueOf(order.getCashAmount()));
        params.put("spbill_create_ip", "127.0.0.1");
        params.put("notify_url", properties.getNotifyUrl());
        params.put("trade_type", "JSAPI");
        params.put("openid", openId);
        params.put("sign", sign(params));

        String response = HttpRequest.post("https://api.mch.weixin.qq.com/pay/unifiedorder")
                .body(mapToXml(params))
                .timeout(10000)
                .execute()
                .body();
        Map<String, Object> result = xmlToMap(response);
        if (!"SUCCESS".equals(String.valueOf(result.get("return_code"))) || !"SUCCESS".equals(String.valueOf(result.get("result_code")))) {
            throw new RenException("微信下单失败：" + result.getOrDefault("err_code_des", result.get("return_msg")));
        }
        String prepayId = String.valueOf(result.get("prepay_id"));
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String payNonce = nonce();
        Map<String, String> paySignData = new TreeMap<>();
        paySignData.put("appId", properties.getAppId());
        paySignData.put("timeStamp", timeStamp);
        paySignData.put("nonceStr", payNonce);
        paySignData.put("package", "prepay_id=" + prepayId);
        paySignData.put("signType", "MD5");
        Map<String, String> pay = new HashMap<>();
        pay.put("timeStamp", timeStamp);
        pay.put("nonceStr", payNonce);
        pay.put("package", "prepay_id=" + prepayId);
        pay.put("signType", "MD5");
        pay.put("paySign", sign(paySignData));
        return pay;
    }

    public boolean verifyNotify(String xml) {
        Map<String, Object> data = xmlToMap(xml);
        if (!"SUCCESS".equals(String.valueOf(data.get("return_code"))) || !"SUCCESS".equals(String.valueOf(data.get("result_code")))) {
            return false;
        }
        String sign = String.valueOf(data.get("sign"));
        Map<String, String> signed = new TreeMap<>();
        data.forEach((key, value) -> {
            if (value != null && !"sign".equals(key)) {
                signed.put(key, String.valueOf(value));
            }
        });
        return sign.equals(sign(signed));
    }

    public String notifyOrderNo(String xml) {
        return String.valueOf(xmlToMap(xml).get("out_trade_no"));
    }

    private String findOpenId(Long userId) {
        ApUserOauthEntity oauth = oauthDao.selectOne(new QueryWrapper<ApUserOauthEntity>()
                .eq("user_id", userId)
                .eq("oauth_type", "wechat_mini")
                .last("LIMIT 1"));
        return oauth == null ? null : oauth.getOauthId();
    }

    private String sign(Map<String, String> data) {
        String plain = data.entrySet().stream()
                .filter(entry -> StrUtil.isNotBlank(entry.getValue()) && !"sign".equals(entry.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
        return SecureUtil.md5(plain + "&key=" + properties.getApiKey()).toUpperCase();
    }

    private String mapToXml(Map<String, String> data) {
        StringBuilder xml = new StringBuilder("<xml>");
        data.forEach((key, value) -> xml.append("<").append(key).append("><![CDATA[").append(value).append("]]></").append(key).append(">"));
        xml.append("</xml>");
        return xml.toString();
    }

    private Map<String, Object> xmlToMap(String xml) {
        Document document = XmlUtil.parseXml(xml);
        return XmlUtil.xmlToMap(document.getDocumentElement());
    }

    private String nonce() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
