package com.heima.business.question.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.heima.business.enums.ExamTypeEnums;
import com.heima.business.enums.SubjectEnums;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;

/**
 * 驾考题目表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-01
 */
@Data
@Schema(name = "驾考题目表")
public class ApDriveQuestionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

	@SchemaProperty(name = "题目ID")
	private Long id;

	@SchemaProperty(name = "科目：1-科目一，4-科目四")
	private SubjectEnums subject;

	/**
	 * 考试类型：c1-C1级，c2-C2级，c3-C3级
	 */
	private ExamTypeEnums examType;

	@SchemaProperty(name = "题型代码：traffic_signs-交通标志，traffic_rules-交通法规等")
	private String questionType;

	@SchemaProperty(name = "题型名称")
	private String questionTypeName;

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

	@SchemaProperty(name = "题目配图URL")
	private String imageUrl;

	@SchemaProperty(name = "题目视频URL")
	private String videoUrl;

	@SchemaProperty(name = "题目音频URL")
	private String audioUrl;

	@SchemaProperty(name = "难度等级：1-简单，2-中等，3-困难")
	private Integer difficultyLevel;

	@SchemaProperty(name = "是否VIP题目：0-免费，1-VIP")
	private Integer isVip;

	@SchemaProperty(name = "是否热门题目：0-否，1-是")
	private Integer isHot;

	@SchemaProperty(name = "顺序刷题排序")
	private Integer sequenceOrder;

	@SchemaProperty(name = "题目权重")
	private Integer weight;

	@SchemaProperty(name = "题目标签，逗号分隔")
	private String tags;

	@SchemaProperty(name = "知识点，逗号分隔")
	private String knowledgePoints;

	@SchemaProperty(name = "浏览次数")
	private Long viewCount;

	@SchemaProperty(name = "错误次数")
	private Long errorCount;

	@SchemaProperty(name = "正确次数")
	private Long correctCount;

	@SchemaProperty(name = "正确率")
	private BigDecimal correctRate;

	@SchemaProperty(name = "平均答题时间(秒)")
	private BigDecimal avgAnswerTime;

	@SchemaProperty(name = "题目来源：official-官方，user-用户贡献")
	private String source;

	@SchemaProperty(name = "题目状态：0-下架，1-可用，2-审核中")
	private Integer status;

	@SchemaProperty(name = "创建者ID")
	private Long creatorId;

	@SchemaProperty(name = "审核者ID")
	private Long auditorId;

	@SchemaProperty(name = "审核时间")
	private Date auditTime;

	@SchemaProperty(name = "是否删除：0-未删除，1-已删除")
	private Integer isDeleted;

	@SchemaProperty(name = "创建时间")
	private Date gmtCreate;

	@SchemaProperty(name = "修改时间")
	private Date gmtModified;


}
