package com.heima.business.study.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 顺序练习进度。只在切题后异步写入，题库列表接口不查询这张表。
 */
@Data
@TableName("ap_practice_progress")
public class ApPracticeProgressEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private Integer subject;

    private String examType;

    private String questionType;

    private Integer lastIndex;

    private Integer answeredCount;

    private Integer correctCount;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;
}
