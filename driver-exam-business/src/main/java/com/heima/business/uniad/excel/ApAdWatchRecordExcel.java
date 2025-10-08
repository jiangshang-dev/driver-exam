package com.heima.business.uniad.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.util.Date;

/**
 * 广告观看记录表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-08
 */
@Data
public class ApAdWatchRecordExcel {
    @ExcelProperty(value = "记录ID，雪花算法生成")
    private Long id;
    @ExcelProperty(value = "用户ID")
    private Long userId;
    @ExcelProperty(value = "用户昵称（冗余）")
    private String userNickname;
    @ExcelProperty(value = "广告ID")
    private String adId;
    @ExcelProperty(value = "广告平台：uni-ad/腾讯广告/穿山甲等")
    private String adPlatform;
    @ExcelProperty(value = "广告类型：rewarded_video-激励视频，banner-横幅等")
    private String adType;
    @ExcelProperty(value = "广告位标识")
    private String adPosition;
    @ExcelProperty(value = "观看日期")
    private Date watchDate;
    @ExcelProperty(value = "奖励类型：vip_days-会员天数，points-积分等")
    private String rewardType;
    @ExcelProperty(value = "奖励数量")
    private Integer rewardAmount;
    @ExcelProperty(value = "奖励发放状态：0-未发放，1-已发放，2-发放失败")
    private Integer rewardStatus;
    @ExcelProperty(value = "设备ID")
    private String deviceId;
    @ExcelProperty(value = "平台类型：mp-weixin/app-plus/h5")
    private String platformType;
    @ExcelProperty(value = "IP地址")
    private String ipAddress;
    @ExcelProperty(value = "用户代理")
    private String userAgent;
    @ExcelProperty(value = "是否删除：0-未删除，1-已删除")
    private Integer isDeleted;
    @ExcelProperty(value = "创建时间")
    private Date createTime;
    @ExcelProperty(value = "修改时间")
    private Date updateTime;

}