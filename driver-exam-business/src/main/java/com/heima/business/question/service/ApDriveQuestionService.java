package com.heima.business.question.service;

import com.heima.business.question.dto.ApDriveQuestionDTO;
import com.heima.business.question.entity.ApDriveQuestionEntity;
import com.heima.business.question.enums.ExamTypeEnums;
import com.heima.business.question.enums.SubjectEnums;
import com.heima.common.service.CrudService;
import jakarta.validation.constraints.NotBlank;

/**
 * 驾考题目表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-01
 */
public interface ApDriveQuestionService extends CrudService<ApDriveQuestionEntity, ApDriveQuestionDTO> {

    void syncQuestion(SubjectEnums subject, ExamTypeEnums model, String testType);

}