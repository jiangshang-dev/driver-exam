package com.heima.business.question.entity;

import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;
import com.heima.business.enums.ExamTypeEnums;
import com.heima.business.enums.SubjectEnums;

import lombok.Data;

/**
 * 驾考题目表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-01
 */
@Data
@TableName("ap_drive_question")
public class ApDriveQuestionEntity {

    /**
     * 题目ID，雪花算法生成
     */
	private Long id;
    /**
     * 科目：1-科目一，4-科目四
     */
	private SubjectEnums subject;

    /**
     * 考试类型：c1-C1级，c2-C2级，c3-C3级
     */
    private ExamTypeEnums examType;
    /**
     * 题型代码：traffic_signs-交通标志，traffic_rules-交通法规等
     */
	private String questionType;
    /**
     * 题型名称
     */
	private String questionTypeName;
    /**
     * 题目内容
     */
	private String questionContent;
    /**
     * 题目格式：1-选择题，2-判断题，3-多选题
     */
	private Integer questionFormat;
    /**
     * 选项A
     */
	private String optionA;
    /**
     * 选项B
     */
	private String optionB;
    /**
     * 选项C
     */
	private String optionC;
    /**
     * 选项D
     */
	private String optionD;
    /**
     * 正确答案：单选:1/2/3/4,多选:1,2,3
     */
	private String correctAnswer;
    /**
     * 题目解析
     */
	private String questionExplain;
    /**
     * 题目配图URL
     */
	private String imageUrl;
    /**
     * 题目视频URL
     */
	private String videoUrl;
    /**
     * 题目音频URL
     */
	private String audioUrl;
    /**
     * 难度等级：1-简单，2-中等，3-困难
     */
	private Integer difficultyLevel;
    /**
     * 是否VIP题目：0-免费，1-VIP
     */
	private Integer isVip;
    /**
     * 是否热门题目：0-否，1-是
     */
	private Integer isHot;
    /**
     * 顺序刷题排序
     */
	private Integer sequenceOrder;
    /**
     * 题目权重
     */
	private Integer weight;
    /**
     * 题目标签，逗号分隔
     */
	private String tags;
    /**
     * 知识点，逗号分隔
     */
	private String knowledgePoints;
    /**
     * 浏览次数
     */
	private Long viewCount;
    /**
     * 错误次数
     */
	private Long errorCount;
    /**
     * 正确次数
     */
	private Long correctCount;
    /**
     * 正确率
     */
	private BigDecimal correctRate;
    /**
     * 平均答题时间(秒)
     */
	private BigDecimal avgAnswerTime;
    /**
     * 题目来源：official-官方，user-用户贡献
     */
	private String source;
    /**
     * 题目状态：0-下架，1-可用，2-审核中
     */
	private Integer status;
    /**
     * 创建者ID
     */
	private Long creatorId;
    /**
     * 审核者ID
     */
	private Long auditorId;
    /**
     * 审核时间
     */
	private Date auditTime;
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