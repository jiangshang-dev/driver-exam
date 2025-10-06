package com.heima.business.wrongCollection.dto;

import com.heima.business.enums.RecordTypeEnums;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;


/**
 * 用户收藏_错题本表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-06
 */
@Data
@Schema(name = "用户收藏_错题本表")
public class ApUserWrongCollectionQuestionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

	@SchemaProperty(name = "错题ID，雪花算法生成")
	private Long id;

	@SchemaProperty(name = "用户ID")
	private Long userId;

	@SchemaProperty(name = "题目ID")
	private Long questionId;

	/**
	 * 学科 科目一
	 */
	private Integer subject;

	@SchemaProperty(name = "记录类型：0-收藏，1-错题")
	private RecordTypeEnums recordType;

	@SchemaProperty(name = "错误次数")
	private Integer wrongCount;

	@SchemaProperty(name = "最后一次错误答案")
	private Integer lastWrongAnswer;

	@SchemaProperty(name = "是否已掌握：0-未掌握，1-已掌握")
	private Integer isMastered;

	@SchemaProperty(name = "掌握时间")
	private LocalDateTime masteredTime;

	@SchemaProperty(name = "首次错误时间")
	private LocalDateTime firstWrongTime;

	@SchemaProperty(name = "最后错误时间")
	private LocalDateTime lastWrongTime;

	@SchemaProperty(name = "是否删除：0-未删除，1-已删除")
	private Integer isDeleted;

	@SchemaProperty(name = "创建时间")
	private LocalDateTime gmtCreate;

	@SchemaProperty(name = "修改时间")
	private LocalDateTime gmtModified;


}
