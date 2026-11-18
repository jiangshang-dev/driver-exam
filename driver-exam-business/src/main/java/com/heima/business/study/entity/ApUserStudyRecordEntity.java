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
 * 用户学习记录
 */
@Data
@TableName("ap_user_study_record")
public class ApUserStudyRecordEntity {
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    private LocalDateTime studyDate;
    private Integer subject;
    private String studyType;
    private Integer totalQuestions;
    private Integer correctCount;
    private Integer wrongCount;
    private BigDecimal accuracyRate;
    private Integer studyTime;
    private Integer pointsEarned;
    private Integer isDeleted;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
}
