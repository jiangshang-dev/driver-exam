package com.heima.business.traffic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * 用户点赞记录表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-11
 */
@Data
@Schema(name = "用户点赞记录表")
public class ApUserLikeRecordDTO implements Serializable {
    private static final long serialVersionUID = 1L;

	@SchemaProperty(name = "点赞ID，雪花算法生成")
	private Long id;

	@SchemaProperty(name = "用户ID")
	private Long userId;

	@SchemaProperty(name = "点赞对象类型：comment-评论，video-视频")
	private String targetType;

	@SchemaProperty(name = "点赞对象ID")
	private Long targetId;

	@SchemaProperty(name = "是否点赞：0-取消点赞，1-点赞")
	private Integer isLike;

	@SchemaProperty(name = "是否删除：0-未删除，1-已删除")
	private Integer isDeleted;

	@SchemaProperty(name = "创建时间")
	private Date createTime;

	@SchemaProperty(name = "修改时间")
	private Date updateTime;


}
