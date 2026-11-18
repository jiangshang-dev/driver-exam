package com.heima.business.study.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考试记录
 */
@Data
@TableName("ap_exam_record")
public class ApExamRecordEntity {
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    private String userNickname;
    private Integer subject;
    private Integer totalQuestions;
    private Integer answeredQuestions;
    private Integer correctCount;
    private Integer wrongCount;
    private BigDecimal score;
    private BigDecimal passScore;
    private Integer isPass;
    private Integer examDuration;
    private Integer usedTime;
    private Integer remainingTime;
    private Integer examStatus;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private Integer isDeleted;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
}
