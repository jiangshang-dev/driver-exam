package com.heima.business.uniad.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 广告观看记录表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-08
 */
@Data
@TableName("ap_ad_watch_record")
public class ApAdWatchRecordEntity {

    /**
     * 记录ID，雪花算法生成
     */
	private Long id;
    /**
     * 用户ID
     */
	private Long userId;
    /**
     * 用户昵称（冗余）
     */
	private String userNickname;
    /**
     * 广告ID
     */
	private String adId;
    /**
     * 广告平台：uni-ad/腾讯广告/穿山甲等
     */
	private String adPlatform;
    /**
     * 广告类型：rewarded_video-激励视频，banner-横幅等
     */
	private String adType;
    /**
     * 广告位标识
     */
	private String adPosition;
    /**
     * 观看日期
     */
	private LocalDateTime watchDate;
    /**
     * 奖励类型：vip_days-会员天数，points-积分等
     */
	private String rewardType;
    /**
     * 奖励数量
     */
	private Integer rewardAmount;
    /**
     * 奖励发放状态：0-未发放，1-已发放，2-发放失败
     */
	private Integer rewardStatus;
    /**
     * 设备ID
     */
	private String deviceId;
    /**
     * 平台类型：mp-weixin/app-plus/h5
     */
	private String platformType;
    /**
     * IP地址
     */
	private String ipAddress;
    /**
     * 用户代理
     */
	private String userAgent;
    /**
     * 是否删除：0-未删除，1-已删除
     */
	private Integer isDeleted;
    /**
     * 创建时间
     */
	private LocalDateTime createTime;
    /**
     * 修改时间
     */
	private LocalDateTime updateTime;
}