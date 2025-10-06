package com.heima.business.wrongCollection.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 用户收藏_错题本表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-06
 */
@Data
public class ApUserWrongCollectionQuestionExcel {
    @ExcelProperty(value = "错题ID，雪花算法生成")
    private Long id;
    @ExcelProperty(value = "用户ID")
    private Long userId;
    @ExcelProperty(value = "用户昵称（冗余）")
    private String userNickname;
    @ExcelProperty(value = "题目ID")
    private Long questionId;
    @ExcelProperty(value = "科目（冗余）")
    private Integer subject;
    @ExcelProperty(value = "题型代码（冗余）")
    private String questionType;
    @ExcelProperty(value = "题型名称（冗余）")
    private String questionTypeName;
    @ExcelProperty(value = "题目内容（冗余）")
    private String questionContent;
    @ExcelProperty(value = "题目配图URL（冗余）")
    private String questionImageUrl;
    @ExcelProperty(value = "正确答案（冗余）")
    private Integer correctAnswer;
    @ExcelProperty(value = "错误次数")
    private Integer wrongCount;
    @ExcelProperty(value = "最后一次错误答案")
    private Integer lastWrongAnswer;
    @ExcelProperty(value = "是否已掌握：0-未掌握，1-已掌握")
    private Integer isMastered;
    @ExcelProperty(value = "掌握时间")
    private Date masteredTime;
    @ExcelProperty(value = "首次错误时间")
    private Date firstWrongTime;
    @ExcelProperty(value = "最后错误时间")
    private Date lastWrongTime;
    @ExcelProperty(value = "是否删除：0-未删除，1-已删除")
    private Integer isDeleted;
    @ExcelProperty(value = "创建时间")
    private Date gmtCreate;
    @ExcelProperty(value = "修改时间")
    private Date gmtModified;

}