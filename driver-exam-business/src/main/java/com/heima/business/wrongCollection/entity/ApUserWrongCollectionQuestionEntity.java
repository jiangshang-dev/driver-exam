package com.heima.business.wrongCollection.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.heima.business.enums.RecordTypeEnums;
import lombok.Data;

import java.util.Date;

/**
 * 用户收藏_错题本表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-06
 */
@Data
@TableName("ap_user_wrong_collection_question")
public class ApUserWrongCollectionQuestionEntity {

    /**
     * 错题ID，雪花算法生成
     */
	private Long id;
    /**
     * 用户ID
     */
	private Long userId;
    /**
     * 题目ID
     */
	private Long questionId;
    /**
     * 学科 科目一
     */
	private Integer subject;

    /**
     * 记录类型：0-收藏，1-错题
     */
    private RecordTypeEnums recordType;
    /**
     * 错误次数
     */
	private Integer wrongCount;
    /**
     * 最后一次错误答案
     */
	private Integer lastWrongAnswer;
    /**
     * 是否已掌握：0-未掌握，1-已掌握
     */
	private Integer isMastered;
    /**
     * 掌握时间
     */
	private Date masteredTime;
    /**
     * 首次错误时间
     */
	private Date firstWrongTime;
    /**
     * 最后错误时间
     */
	private Date lastWrongTime;
    /**
     * 是否删除：0-未删除，1-已删除
     */
	private Integer isDeleted;
    /**
     * 创建时间
     */
	private Date createTime;
    /**
     * 修改时间
     */
	private Date updateTime;
}