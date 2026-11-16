package com.heima.business.study.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考试计划
 */
@Data
@TableName("ap_exam_plan")
public class ApExamPlanEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private String examType;

    private Integer subject;

    private LocalDate examDate;

    private Integer targetScore;

    private Integer dailyGoal;

    private String remark;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;
}
