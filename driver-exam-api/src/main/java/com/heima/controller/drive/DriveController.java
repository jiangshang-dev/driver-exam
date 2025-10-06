package com.heima.controller.drive;

import com.heima.business.question.entity.ApDriveQuestionEntity;
import com.heima.business.question.service.ApDriveQuestionService;
import com.heima.common.utils.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/drive")
public class DriveController {

    @Resource
    private ApDriveQuestionService driveQuestionService;

    /**
     * 获取驾考题库顺序列表
     * @param subject 科目
     * @param driveType 测试类型
     */
    @GetMapping("/driveTopicList")
    public Result driveTopicList(@RequestParam(value = "subject", defaultValue = "1") String subject,
                                 @RequestParam(value = "questionType", defaultValue = "") String questionType) {
        List<ApDriveQuestionEntity> driveTopicList = driveQuestionService.driveQuestionList(subject, questionType);
        return new Result<List<ApDriveQuestionEntity>>().ok(driveTopicList);
    }
}
