package com.heima.business.traffic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * 视频评论表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-12
 */
@Data
@Schema(name = "视频评论表")
public class ApVideoCommentDTO implements Serializable {
    private static final long serialVersionUID = 1L;

	@SchemaProperty(name = "评论ID，雪花算法生成")
	private Long id;

	@SchemaProperty(name = "视频ID")
	private Long videoId;

	@SchemaProperty(name = "用户ID")
	private Long userId;

	@SchemaProperty(name = "手机号")
	private String phone;

	@SchemaProperty(name = "父评论ID（回复功能）")
	private Long parentCommentId;

	@SchemaProperty(name = "评论内容")
	private String commentContent;

	@SchemaProperty(name = "点赞数")
	private Long likeCount;

	@SchemaProperty(name = "回复数")
	private Long replyCount;

	@SchemaProperty(name = "是否热门评论：0-否，1-是")
	private Integer isHot;

	@SchemaProperty(name = "审核状态：0-待审核，1-审核通过，2-审核不通过")
	private Integer auditStatus;

	@SchemaProperty(name = "审核时间")
	private Date auditTime;

	@SchemaProperty(name = "审核人ID")
	private Long auditUserId;

	@SchemaProperty(name = "审核备注")
	private String auditRemark;

	@SchemaProperty(name = "是否删除：0-未删除，1-已删除")
	private Integer isDeleted;

	@SchemaProperty(name = "创建时间")
	private Date gmtCreate;

	@SchemaProperty(name = "修改时间")
	private Date gmtModified;


}
