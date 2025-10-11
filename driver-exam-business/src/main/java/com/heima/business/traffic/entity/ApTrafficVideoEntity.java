package com.heima.business.traffic.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 交规视频表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-07
 */
@Data
@TableName("ap_traffic_video")
public class ApTrafficVideoEntity {

    /**
     * 视频ID，雪花算法生成
     */
	private Long id;
    /**
     * 视频标题
     */
	private String videoTitle;
    /**
     * 视频描述
     */
	private String videoDesc;
    /**
     * 封面图片URL
     */
	private String coverImage;
    /**
     * 视频URL
     */
	private String videoUrl;
    /**
     * 阿里云视频ID
     */
	private String aliyunVideoId;
    /**
     * 播放凭证
     */
	private String playAuth;
    /**
     * 视频时长（秒）
     */
	private Integer videoDuration;
    /**
     * 视频大小（字节）
     */
	private Long videoSize;
    /**
     * 视频格式
     */
	private String videoFormat;
    /**
     * 分辨率
     */
	private String resolution;
    /**
     * 视频分类
     */
	private String category;
    /**
     * 标签，逗号分隔
     */
	private String tags;
    /**
     * 讲师名称
     */
	private String instructor;
    /**
     * 难度等级：1-初级，2-中级，3-高级
     */
	private Integer difficultyLevel;
    /**
     * 是否VIP视频：0-免费，1-VIP
     */
	private Integer isVip;
    /**
     * 是否热门：0-否，1-是
     */
	private Integer isHot;
    /**
     * 是否可下载：0-否，1-是
     */
	private Integer isDownloadable;
    /**
     * 观看次数
     */
	private Long viewCount;
    /**
     * 点赞次数
     */
	private Long likeCount;
    /**
     * 分享次数
     */
	private Long shareCount;
    /**
     * 评论数
     */
	private Long commentCount;
    /**
     * 下载次数
     */
	private Long downloadCount;
    /**
     * 评分(0-5)
     */
	private BigDecimal rating;
    /**
     * 评分人数
     */
	private Integer ratingCount;
    /**
     * 排序权重
     */
	private Integer sortOrder;
    /**
     * 视频状态：0-下架，1-正常，2-审核中，3-转码中
     */
	private Integer status;
    /**
     * 上传用户ID
     */
	private Long uploadUserId;
    /**
     * 审核用户ID
     */
	private Long auditUserId;
    /**
     * 审核时间
     */
	private Date auditTime;
    /**
     * 发布时间
     */
	private Date publishTime;
    /**
     * 是否删除：0-未删除，1-已删除
     */
	private Integer isDeleted;
    /**
     * 创建时间
     */
	private Date gmtCreate;
    /**
     * 修改时间
     */
	private Date gmtModified;

    /**
     * 是否点赞
     */
    @TableField(exist = false)
    private Boolean isLike;
}