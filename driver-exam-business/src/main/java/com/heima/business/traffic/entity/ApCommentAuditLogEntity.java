package com.heima.business.traffic.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 评论审核日志表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-11
 */
@Data
@TableName("ap_comment_audit_log")
public class ApCommentAuditLogEntity {

    /**
     * 日志ID，雪花算法生成
     */
	private Long id;
    /**
     * 评论ID
     */
	private Long commentId;
    /**
     * 用户ID
     */
	private Long userId;
    /**
     * 审核状态：0-待审核，1-审核通过，2-审核不通过
     */
	private Integer auditStatus;
    /**
     * 审核人ID
     */
	private Long auditUserId;
    /**
     * 审核时间
     */
	private Date auditTime;
    /**
     * 审核备注
     */
	private String auditRemark;
    /**
     * 是否删除：0-未删除，1-已删除
     */
	private Integer isDeleted;
    /**
     * 创建时间
     */
	private Date createTime;
    /**
     * 修改时间
     */
	private Date updateTime;
}