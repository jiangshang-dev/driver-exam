package com.heima.business.study.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学习打卡
 */
@Data
@TableName("ap_study_checkin")
public class ApStudyCheckinEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private LocalDate checkinDate;

    private Integer subject;

    private String examType;

    private Integer questionCount;

    private Integer correctCount;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;
}
