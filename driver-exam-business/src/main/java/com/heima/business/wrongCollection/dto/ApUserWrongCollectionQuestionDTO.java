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

	@SchemaProperty(name = "题目配图URL")
	private String imageUrl;

	@SchemaProperty(name = "题目内容")
	private String questionContent;

	@SchemaProperty(name = "题目格式：1-选择题，2-判断题，3-多选题")
	private Integer questionFormat;

	@SchemaProperty(name = "选项A")
	private String optionA;

	@SchemaProperty(name = "选项B")
	private String optionB;

	@SchemaProperty(name = "选项C")
	private String optionC;

	@SchemaProperty(name = "选项D")
	private String optionD;

	@SchemaProperty(name = "正确答案：单选:1/2/3/4,多选:1,2,3")
	private String correctAnswer;

	@SchemaProperty(name = "题目解析")
	private String questionExplain;

	@SchemaProperty(name = "难度等级：1-简单，2-中等，3-困难")
	private Integer difficultyLevel;

	@SchemaProperty(name = "是否VIP题目：0-免费，1-VIP")
	private Integer isVip;

	@SchemaProperty(name = "是否热门题目：0-否，1-是")
	private Integer isHot;

	@SchemaProperty(name = "题目标签，逗号分隔")
	private String tags;

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
	private LocalDateTime createTime;

	@SchemaProperty(name = "修改时间")
	private LocalDateTime updateTime;

	@SchemaProperty(name = "用户昵称")
	private String userNickname;
}
