package com.heima.controller.study;

import com.heima.business.study.entity.ApExamPlanEntity;
import com.heima.business.study.entity.ApKnowledgeCardEntity;
import com.heima.business.study.entity.ApPracticeProgressEntity;
import com.heima.business.study.service.StudyFeatureService;
import com.heima.common.utils.JwtUtils;
import com.heima.common.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * 学习打卡、考试计划和考点速记。不参与题库全量加载。
 */
@RestController
@RequestMapping("/study")
@Tag(name = "学习计划")
public class StudyController {

    @Resource
    private StudyFeatureService studyFeatureService;

    @GetMapping("/summary")
    @Operation(summary = "学习概览")
    public Result<Map<String, Object>> summary(@RequestHeader(value = "Authorization", required = false) String token) {
        return new Result<Map<String, Object>>().ok(studyFeatureService.summary(userId(token)));
    }

    @PostMapping("/checkin")
    @Operation(summary = "今日打卡")
    public Result<Map<String, Object>> checkin(@RequestHeader(value = "Authorization", required = false) String token,
                                               @RequestBody CheckinRequest request) {
        Long userId = userId(token);
        if (userId == null) {
            return new Result<Map<String, Object>>().error("请先登录");
        }
        return new Result<Map<String, Object>>().ok(studyFeatureService.checkin(
                userId,
                request.getSubject(),
                request.getExamType(),
                request.getQuestionCount(),
                request.getCorrectCount()
        ));
    }

    @PostMapping("/plan")
    @Operation(summary = "保存考试计划")
    public Result<ApExamPlanEntity> savePlan(@RequestHeader(value = "Authorization", required = false) String token,
                                             @RequestBody PlanRequest request) {
        Long userId = userId(token);
        if (userId == null) {
            return new Result<ApExamPlanEntity>().error("请先登录");
        }
        if (StringUtils.isBlank(request.getExamDate())) {
            return new Result<ApExamPlanEntity>().error("请选择考试日期");
        }
        LocalDate examDate;
        try {
            examDate = LocalDate.parse(request.getExamDate());
        } catch (DateTimeParseException ex) {
            return new Result<ApExamPlanEntity>().error("考试日期格式应为 yyyy-MM-dd");
        }
        ApExamPlanEntity plan = studyFeatureService.savePlan(
                userId,
                request.getSubject(),
                request.getExamType(),
                examDate,
                request.getTargetScore(),
                request.getDailyGoal(),
                request.getRemark()
        );
        return new Result<ApExamPlanEntity>().ok(plan);
    }

    @GetMapping("/plan")
    @Operation(summary = "考试计划")
    public Result<ApExamPlanEntity> plan(@RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = userId(token);
        if (userId == null) {
            return new Result<ApExamPlanEntity>().error("请先登录");
        }
        return new Result<ApExamPlanEntity>().ok(studyFeatureService.getPlan(userId));
    }

    @GetMapping("/knowledge")
    @Operation(summary = "考点速记")
    public Result<List<ApKnowledgeCardEntity>> knowledge(@RequestParam(value = "subject", required = false) Integer subject) {
        return new Result<List<ApKnowledgeCardEntity>>().ok(studyFeatureService.knowledgeList(subject));
    }

    @PostMapping("/progress")
    @Operation(summary = "保存练习进度")
    public Result<ApPracticeProgressEntity> saveProgress(@RequestHeader(value = "Authorization", required = false) String token,
                                                         @RequestBody ProgressRequest request) {
        Long userId = userId(token);
        if (userId == null) {
            return new Result<ApPracticeProgressEntity>().error("请先登录");
        }
        return new Result<ApPracticeProgressEntity>().ok(studyFeatureService.saveProgress(
                userId,
                request.getSubject(),
                request.getExamType(),
                request.getQuestionType(),
                request.getLastIndex(),
                request.getAnsweredCount(),
                request.getCorrectCount()
        ));
    }

    @GetMapping("/progress")
    @Operation(summary = "练习进度")
    public Result<ApPracticeProgressEntity> progress(@RequestHeader(value = "Authorization", required = false) String token,
                                                     @RequestParam(value = "subject", defaultValue = "1") Integer subject,
                                                     @RequestParam(value = "examType", defaultValue = "c1") String examType,
                                                     @RequestParam(value = "questionType", defaultValue = "") String questionType) {
        Long userId = userId(token);
        if (userId == null) {
            return new Result<ApPracticeProgressEntity>().error("请先登录");
        }
        return new Result<ApPracticeProgressEntity>().ok(studyFeatureService.getProgress(userId, subject, examType, questionType));
    }

    private Long userId(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        return JwtUtils.getUserId(token);
    }

    @Data
    public static class CheckinRequest {
        private Integer subject;
        private String examType;
        private Integer questionCount;
        private Integer correctCount;
    }

    @Data
    public static class PlanRequest {
        private Integer subject;
        private String examType;
        private String examDate;
        private Integer targetScore;
        private Integer dailyGoal;
        private String remark;
    }

    @Data
    public static class ProgressRequest {
        private Integer subject;
        private String examType;
        private String questionType;
        private Integer lastIndex;
        private Integer answeredCount;
        private Integer correctCount;
    }
}
