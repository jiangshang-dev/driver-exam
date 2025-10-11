package com.heima.business.traffic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

import java.math.BigDecimal;

/**
 * 交规视频表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-07
 */
@Data
@Schema(name = "交规视频表")
public class ApTrafficVideoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

	@SchemaProperty(name = "视频ID，雪花算法生成")
	private Long id;

	@SchemaProperty(name = "视频标题")
	private String videoTitle;

	@SchemaProperty(name = "视频描述")
	private String videoDesc;

	@SchemaProperty(name = "封面图片URL")
	private String coverImage;

	@SchemaProperty(name = "视频URL")
	private String videoUrl;

	@SchemaProperty(name = "阿里云视频ID")
	private String aliyunVideoId;

	@SchemaProperty(name = "播放凭证")
	private String playAuth;

	@SchemaProperty(name = "视频时长（秒）")
	private Integer videoDuration;

	@SchemaProperty(name = "视频大小（字节）")
	private Long videoSize;

	@SchemaProperty(name = "视频格式")
	private String videoFormat;

	@SchemaProperty(name = "分辨率")
	private String resolution;

	@SchemaProperty(name = "视频分类")
	private String category;

	@SchemaProperty(name = "标签，逗号分隔")
	private String tags;

	@SchemaProperty(name = "讲师名称")
	private String instructor;

	@SchemaProperty(name = "难度等级：1-初级，2-中级，3-高级")
	private Integer difficultyLevel;

	@SchemaProperty(name = "是否VIP视频：0-免费，1-VIP")
	private Integer isVip;

	@SchemaProperty(name = "是否热门：0-否，1-是")
	private Integer isHot;

	@SchemaProperty(name = "是否可下载：0-否，1-是")
	private Integer isDownloadable;

	@SchemaProperty(name = "观看次数")
	private Long viewCount;

	@SchemaProperty(name = "点赞次数")
	private Long likeCount;

	@SchemaProperty(name = "分享次数")
	private Long shareCount;

	@SchemaProperty(name = "评论数")
	private Long commentCount;

	@SchemaProperty(name = "下载次数")
	private Long downloadCount;

	@SchemaProperty(name = "评分(0-5)")
	private BigDecimal rating;

	@SchemaProperty(name = "评分人数")
	private Integer ratingCount;

	@SchemaProperty(name = "排序权重")
	private Integer sortOrder;

	@SchemaProperty(name = "视频状态：0-下架，1-正常，2-审核中，3-转码中")
	private Integer status;

	@SchemaProperty(name = "上传用户ID")
	private Long uploadUserId;

	@SchemaProperty(name = "审核用户ID")
	private Long auditUserId;

	@SchemaProperty(name = "审核时间")
	private Date auditTime;

	@SchemaProperty(name = "发布时间")
	private Date publishTime;

	@SchemaProperty(name = "是否删除：0-未删除，1-已删除")
	private Integer isDeleted;

	@SchemaProperty(name = "创建时间")
	private Date gmtCreate;

	@SchemaProperty(name = "修改时间")
	private Date gmtModified;

	@SchemaProperty(name = "是否点赞")
	private Boolean isLike;
}
