package com.heima.business.question.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 驾考题目表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-01
 */
@Data
public class ApDriveQuestionExcel {
    @ExcelProperty(value = "题目ID，雪花算法生成")
    private Long id;
    @ExcelProperty(value = "科目：1-科目一，4-科目四")
    private Integer subject;
    @ExcelProperty(value = "题型代码：traffic_signs-交通标志，traffic_rules-交通法规等")
    private String questionType;
    @ExcelProperty(value = "题型名称")
    private String questionTypeName;
    @ExcelProperty(value = "题目内容")
    private String questionContent;
    @ExcelProperty(value = "题目格式：1-选择题，2-判断题，3-多选题")
    private Integer questionFormat;
    @ExcelProperty(value = "选项A")
    private String optionA;
    @ExcelProperty(value = "选项B")
    private String optionB;
    @ExcelProperty(value = "选项C")
    private String optionC;
    @ExcelProperty(value = "选项D")
    private String optionD;
    @ExcelProperty(value = "正确答案：单选:1/2/3/4,多选:1,2,3")
    private String correctAnswer;
    @ExcelProperty(value = "题目解析")
    private String questionExplain;
    @ExcelProperty(value = "题目配图URL")
    private String imageUrl;
    @ExcelProperty(value = "题目视频URL")
    private String videoUrl;
    @ExcelProperty(value = "题目音频URL")
    private String audioUrl;
    @ExcelProperty(value = "难度等级：1-简单，2-中等，3-困难")
    private Integer difficultyLevel;
    @ExcelProperty(value = "是否VIP题目：0-免费，1-VIP")
    private Integer isVip;
    @ExcelProperty(value = "是否热门题目：0-否，1-是")
    private Integer isHot;
    @ExcelProperty(value = "顺序刷题排序")
    private Integer sequenceOrder;
    @ExcelProperty(value = "题目权重")
    private Integer weight;
    @ExcelProperty(value = "题目标签，逗号分隔")
    private String tags;
    @ExcelProperty(value = "知识点，逗号分隔")
    private String knowledgePoints;
    @ExcelProperty(value = "浏览次数")
    private Long viewCount;
    @ExcelProperty(value = "错误次数")
    private Long errorCount;
    @ExcelProperty(value = "正确次数")
    private Long correctCount;
    @ExcelProperty(value = "正确率")
    private BigDecimal correctRate;
    @ExcelProperty(value = "平均答题时间(秒)")
    private BigDecimal avgAnswerTime;
    @ExcelProperty(value = "题目来源：official-官方，user-用户贡献")
    private String source;
    @ExcelProperty(value = "题目状态：0-下架，1-可用，2-审核中")
    private Integer status;
    @ExcelProperty(value = "创建者ID")
    private Long creatorId;
    @ExcelProperty(value = "审核者ID")
    private Long auditorId;
    @ExcelProperty(value = "审核时间")
    private Date auditTime;
    @ExcelProperty(value = "是否删除：0-未删除，1-已删除")
    private Integer isDeleted;
    @ExcelProperty(value = "创建时间")
    private Date gmtCreate;
    @ExcelProperty(value = "修改时间")
    private Date gmtModified;

}