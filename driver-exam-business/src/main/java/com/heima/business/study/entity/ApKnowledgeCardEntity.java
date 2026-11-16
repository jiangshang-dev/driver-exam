package com.heima.business.study.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考点速记
 */
@Data
@TableName("ap_knowledge_card")
public class ApKnowledgeCardEntity {

    private Long id;

    private Integer subject;

    private String category;

    private String title;

    private String content;

    private Integer sortOrder;

    @TableLogic
    private Integer isDeleted;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;
}
