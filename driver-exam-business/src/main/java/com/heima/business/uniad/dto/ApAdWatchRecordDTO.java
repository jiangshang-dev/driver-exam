package com.heima.business.uniad.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;


/**
 * 广告观看记录表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-08
 */
@Data
@Schema(name = "广告观看记录表")
public class ApAdWatchRecordDTO implements Serializable {
    private static final long serialVersionUID = 1L;

	@SchemaProperty(name = "记录ID，雪花算法生成")
	private Long id;

	@SchemaProperty(name = "用户ID")
	private Long userId;

	@SchemaProperty(name = "用户昵称（冗余）")
	private String userNickname;

	@SchemaProperty(name = "广告ID")
	private String adId;

	@SchemaProperty(name = "广告平台：uni-ad/腾讯广告/穿山甲等")
	private String adPlatform;

	@SchemaProperty(name = "广告类型：rewarded_video-激励视频，banner-横幅等")
	private String adType;

	@SchemaProperty(name = "广告位标识")
	private String adPosition;

	@SchemaProperty(name = "观看日期")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime watchDate;

	@SchemaProperty(name = "奖励类型：vip_days-会员天数，points-积分等")
	private String rewardType;

	@SchemaProperty(name = "奖励数量")
	private Integer rewardAmount;

	@SchemaProperty(name = "奖励发放状态：0-未发放，1-已发放，2-发放失败")
	private Integer rewardStatus;

	@SchemaProperty(name = "设备ID")
	private String deviceId;

	@SchemaProperty(name = "平台类型：mp-weixin/app-plus/h5")
	private String platformType;

	@SchemaProperty(name = "IP地址")
	private String ipAddress;

	@SchemaProperty(name = "用户代理")
	private String userAgent;

	@SchemaProperty(name = "是否删除：0-未删除，1-已删除")
	private Integer isDeleted;

	@SchemaProperty(name = "创建时间")
	private Date createTime;

	@SchemaProperty(name = "修改时间")
	private Date updateTime;


}
