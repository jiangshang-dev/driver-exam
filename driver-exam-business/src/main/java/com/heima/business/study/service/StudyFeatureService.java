package com.heima.business.study.service;

import com.heima.business.study.entity.ApExamPlanEntity;
import com.heima.business.study.entity.ApKnowledgeCardEntity;
import com.heima.business.study.entity.ApPracticeProgressEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface StudyFeatureService {

    Map<String, Object> summary(Long userId);

    Map<String, Object> checkin(Long userId, Integer subject, String examType, Integer questionCount, Integer correctCount);

    ApExamPlanEntity savePlan(Long userId, Integer subject, String examType, LocalDate examDate, Integer targetScore, Integer dailyGoal, String remark);

    ApExamPlanEntity getPlan(Long userId);

    List<ApKnowledgeCardEntity> knowledgeList(Integer subject);

    ApPracticeProgressEntity saveProgress(Long userId, Integer subject, String examType, String questionType, Integer lastIndex, Integer answeredCount, Integer correctCount);

    ApPracticeProgressEntity getProgress(Long userId, Integer subject, String examType, String questionType);
}
