package com.heima.business.traffic.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 视频评论表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-12
 */
@Data
@TableName("ap_video_comment")
public class ApVideoCommentEntity {

    /**
     * 评论ID，雪花算法生成
     */
	private Long id;
    /**
     * 视频ID
     */
	private Long videoId;
    /**
     * 用户ID
     */
	private Long userId;
    /**
     * 父评论ID（回复功能）
     */
	private Long parentCommentId;
    /**
     * 评论内容
     */
	private String commentContent;
    /**
     * 点赞数
     */
	private Long likeCount;
    /**
     * 回复数
     */
	private Long replyCount;
    /**
     * 是否热门评论：0-否，1-是
     */
	private Integer isHot;
    /**
     * 审核状态：0-待审核，1-审核通过，2-审核不通过
     */
	private Integer auditStatus;
    /**
     * 审核时间
     */
	private Date auditTime;
    /**
     * 审核人ID
     */
	private Long auditUserId;
    /**
     * 审核备注
     */
	private String auditRemark;

    @TableField(exist = false)
	private String phone;
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
}