package com.heima.business.traffic.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 用户点赞记录表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-11
 */
@Data
@TableName("ap_user_like_record")
public class ApUserLikeRecordEntity {

    /**
     * 点赞ID，雪花算法生成
     */
	private Long id;
    /**
     * 用户ID
     */
	private Long userId;
    /**
     * 点赞对象类型：comment-评论，video-视频
     */
	private String targetType;
    /**
     * 点赞对象ID
     */
	private Long targetId;
    /**
     * 是否点赞：0-取消点赞，1-点赞
     */
	private Boolean isLike;
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