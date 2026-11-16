package com.heima.business.study.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.business.study.dao.ApExamPlanDao;
import com.heima.business.study.dao.ApKnowledgeCardDao;
import com.heima.business.study.dao.ApPracticeProgressDao;
import com.heima.business.study.dao.ApStudyCheckinDao;
import com.heima.business.study.entity.ApExamPlanEntity;
import com.heima.business.study.entity.ApKnowledgeCardEntity;
import com.heima.business.study.entity.ApPracticeProgressEntity;
import com.heima.business.study.entity.ApStudyCheckinEntity;
import com.heima.business.study.service.StudyFeatureService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class StudyFeatureServiceImpl implements StudyFeatureService {

    @Resource
    private ApStudyCheckinDao checkinDao;
    @Resource
    private ApExamPlanDao planDao;
    @Resource
    private ApKnowledgeCardDao knowledgeCardDao;
    @Resource
    private ApPracticeProgressDao progressDao;

    @Override
    public Map<String, Object> summary(Long userId) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("streak", 0);
        summary.put("checkedToday", false);
        summary.put("todayCount", 0);
        summary.put("todayCorrect", 0);
        summary.put("plan", null);
        if (userId == null) {
            return summary;
        }

        LocalDate today = LocalDate.now();
        List<ApStudyCheckinEntity> todayRecords = checkinDao.selectList(new QueryWrapper<ApStudyCheckinEntity>()
                .eq("user_id", userId)
                .eq("checkin_date", today));
        int todayCount = 0;
        int todayCorrect = 0;
        for (ApStudyCheckinEntity record : todayRecords) {
            todayCount += record.getQuestionCount() == null ? 0 : record.getQuestionCount();
            todayCorrect += record.getCorrectCount() == null ? 0 : record.getCorrectCount();
        }
        summary.put("checkedToday", !todayRecords.isEmpty());
        summary.put("todayCount", todayCount);
        summary.put("todayCorrect", todayCorrect);
        summary.put("streak", calcStreak(userId));

        ApExamPlanEntity plan = getPlan(userId);
        if (plan != null) {
            Map<String, Object> planView = new HashMap<>();
            planView.put("id", plan.getId());
            planView.put("subject", plan.getSubject());
            planView.put("examType", plan.getExamType());
            planView.put("examDate", plan.getExamDate());
            planView.put("targetScore", plan.getTargetScore());
            planView.put("dailyGoal", plan.getDailyGoal());
            planView.put("remark", plan.getRemark());
            long daysLeft = plan.getExamDate() == null ? 0 : ChronoUnit.DAYS.between(today, plan.getExamDate());
            planView.put("daysLeft", Math.max(daysLeft, 0));
            summary.put("plan", planView);
        }
        return summary;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> checkin(Long userId, Integer subject, String examType, Integer questionCount, Integer correctCount) {
        LocalDate today = LocalDate.now();
        String model = normalizeExamType(examType);
        int subjectCode = subject == null ? 1 : subject;
        int questions = questionCount == null ? 0 : Math.max(questionCount, 0);
        int correct = correctCount == null ? 0 : Math.max(correctCount, 0);

        ApStudyCheckinEntity record = checkinDao.selectOne(new QueryWrapper<ApStudyCheckinEntity>()
                .eq("user_id", userId)
                .eq("checkin_date", today)
                .eq("subject", subjectCode)
                .eq("exam_type", model)
                .last("LIMIT 1"));
        LocalDateTime now = LocalDateTime.now();
        if (record == null) {
            record = new ApStudyCheckinEntity();
            record.setUserId(userId);
            record.setCheckinDate(today);
            record.setSubject(subjectCode);
            record.setExamType(model);
            record.setQuestionCount(questions);
            record.setCorrectCount(correct);
            record.setGmtCreate(now);
            record.setGmtModified(now);
            checkinDao.insert(record);
        } else {
            record.setQuestionCount((record.getQuestionCount() == null ? 0 : record.getQuestionCount()) + questions);
            record.setCorrectCount((record.getCorrectCount() == null ? 0 : record.getCorrectCount()) + correct);
            record.setGmtModified(now);
            checkinDao.updateById(record);
        }
        return summary(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApExamPlanEntity savePlan(Long userId, Integer subject, String examType, LocalDate examDate, Integer targetScore, Integer dailyGoal, String remark) {
        ApExamPlanEntity plan = planDao.selectOne(new QueryWrapper<ApExamPlanEntity>()
                .eq("user_id", userId)
                .orderByDesc("gmt_modified")
                .last("LIMIT 1"));
        LocalDateTime now = LocalDateTime.now();
        if (plan == null) {
            plan = new ApExamPlanEntity();
            plan.setUserId(userId);
            plan.setGmtCreate(now);
        }
        plan.setSubject(subject == null ? 1 : subject);
        plan.setExamType(normalizeExamType(examType));
        plan.setExamDate(examDate);
        plan.setTargetScore(targetScore == null ? 90 : targetScore);
        plan.setDailyGoal(dailyGoal == null ? 20 : dailyGoal);
        plan.setRemark(remark);
        plan.setGmtModified(now);
        if (plan.getId() == null) {
            planDao.insert(plan);
        } else {
            planDao.updateById(plan);
        }
        return plan;
    }

    @Override
    public ApExamPlanEntity getPlan(Long userId) {
        if (userId == null) {
            return null;
        }
        return planDao.selectOne(new QueryWrapper<ApExamPlanEntity>()
                .eq("user_id", userId)
                .orderByDesc("gmt_modified")
                .last("LIMIT 1"));
    }

    @Override
    public List<ApKnowledgeCardEntity> knowledgeList(Integer subject) {
        QueryWrapper<ApKnowledgeCardEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(subject != null, "subject", subject);
        wrapper.orderByAsc("sort_order").orderByAsc("id");
        return knowledgeCardDao.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApPracticeProgressEntity saveProgress(Long userId, Integer subject, String examType, String questionType, Integer lastIndex, Integer answeredCount, Integer correctCount) {
        String model = normalizeExamType(examType);
        String type = questionType == null ? "" : questionType;
        ApPracticeProgressEntity progress = progressDao.selectOne(new QueryWrapper<ApPracticeProgressEntity>()
                .eq("user_id", userId)
                .eq("subject", subject)
                .eq("exam_type", model)
                .eq("question_type", type)
                .last("LIMIT 1"));
        LocalDateTime now = LocalDateTime.now();
        if (progress == null) {
            progress = new ApPracticeProgressEntity();
            progress.setUserId(userId);
            progress.setSubject(subject);
            progress.setExamType(model);
            progress.setQuestionType(type);
            progress.setGmtCreate(now);
        }
        progress.setLastIndex(lastIndex == null ? 0 : Math.max(lastIndex, 0));
        progress.setAnsweredCount(answeredCount == null ? 0 : Math.max(answeredCount, 0));
        progress.setCorrectCount(correctCount == null ? 0 : Math.max(correctCount, 0));
        progress.setGmtModified(now);
        if (progress.getId() == null) {
            progressDao.insert(progress);
        } else {
            progressDao.updateById(progress);
        }
        return progress;
    }

    @Override
    public ApPracticeProgressEntity getProgress(Long userId, Integer subject, String examType, String questionType) {
        if (userId == null) {
            return null;
        }
        return progressDao.selectOne(new QueryWrapper<ApPracticeProgressEntity>()
                .eq("user_id", userId)
                .eq("subject", subject)
                .eq("exam_type", normalizeExamType(examType))
                .eq("question_type", questionType == null ? "" : questionType)
                .last("LIMIT 1"));
    }

    private int calcStreak(Long userId) {
        List<ApStudyCheckinEntity> records = checkinDao.selectList(new QueryWrapper<ApStudyCheckinEntity>()
                .select("checkin_date")
                .eq("user_id", userId)
                .orderByDesc("checkin_date")
                .last("LIMIT 400"));
        Set<LocalDate> days = new LinkedHashSet<>();
        for (ApStudyCheckinEntity record : records) {
            if (record.getCheckinDate() != null) {
                days.add(record.getCheckinDate());
            }
        }
        if (days.isEmpty()) {
            return 0;
        }
        LocalDate cursor = LocalDate.now();
        if (!days.contains(cursor)) {
            cursor = cursor.minusDays(1);
        }
        int streak = 0;
        while (days.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private String normalizeExamType(String examType) {
        if (StringUtils.isBlank(examType)) {
            return "c1";
        }
        return examType.toLowerCase();
    }
}
