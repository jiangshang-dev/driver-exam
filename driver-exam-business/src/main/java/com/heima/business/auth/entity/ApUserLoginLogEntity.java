package com.heima.business.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 用户登录日志表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-09-25
 */
@Data
@TableName("ap_user_login_log")
public class ApUserLoginLogEntity {

    /**
     * 主键ID
     */
	private Long id;
    /**
     * 用户ID
     */
	private Long userId;
    /**
     * 登录时间
     */
	private LocalDateTime loginTime;
    /**
     * 登录IP地址
     */
	private String loginIp;
    /**
     * IP地址归属地（省市区）
     */
	private String ipLocation;
    /**
     * 经度
     */
	private BigDecimal longitude;
    /**
     * 纬度
     */
	private BigDecimal latitude;
    /**
     * 设备类型（mobile/desktop/tablet/miniprogram）
     */
	private String deviceType;
    /**
     * 设备品牌（iPhone/Huawei/Xiaomi等）
     */
	private String deviceBrand;
    /**
     * 设备型号（iPhone 13/P50 Pro等）
     */
	private String deviceModel;
    /**
     * 操作系统名称（iOS/Android/Windows）
     */
	private String osName;
    /**
     * 操作系统版本（15.0/12.0等）
     */
	private String osVersion;
    /**
     * 浏览器名称（Safari/Chrome等）
     */
	private String browserName;
    /**
     * 浏览器版本
     */
	private String browserVersion;
    /**
     * 平台（mp-weixin/app-android/app-ios/h5）
     */
	private String platform;
    /**
     * 登录方式（wechat/phone/sms/password）
     */
	private String loginType;
    /**
     * 用户代理字符串（完整的User-Agent）
     */
	private String userAgent;
    /**
     * 会话ID
     */
	private String sessionId;
    /**
     * 登录状态（1:成功 0:失败）
     */
	private Integer loginStatus;
    /**
     * 登录失败原因
     */
	private String failReason;
    /**
     * 是否风险登录（1:是 0:否）
     */
	private Integer isRiskLogin;
    /**
     * 风险等级（0:正常 1:低风险 2:中风险 3:高风险）
     */
	private Integer riskLevel;
    /**
     * 风险描述（具体的风险原因说明）
     */
	private String riskDesc;
    /**
     * 设备指纹（用于设备识别）
     */
	private String deviceFingerprint;
    /**
     * 网络类型（wifi/4g/5g等）
     */
	private String networkType;
    /**
     * 退出登录时间
     */
	private Date logoutTime;
    /**
     * 在线时长（秒）
     */
	private Integer onlineDuration;
    /**
     * 客户端版本号
     */
	private String clientVersion;
    /**
     * 创建时间
     */
	private LocalDateTime createTime;
    /**
     * 更新时间
     */
	private LocalDateTime updateTime;
}