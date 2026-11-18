package com.heima.modules.learning;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.business.study.dao.ApExamRecordDao;
import com.heima.business.study.entity.ApExamRecordEntity;
import com.heima.common.page.PageData;
import com.heima.common.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 考试记录管理
 */
@RestController
@RequestMapping("/learning/examRecord")
@Tag(name = "考试记录管理")
public class ExamRecordController {

    @Resource
    private ApExamRecordDao examRecordDao;

    @GetMapping("page")
    @Operation(summary = "分页")
    @RequiresPermissions("exam:record:page")
    public Result<PageData<ApExamRecordEntity>> page(@RequestParam Map<String, Object> params) {
        long page = parseLong(params.get("page"), 1);
        long limit = parseLong(params.get("limit"), 10);
        String userId = getStr(params, "userId");
        String userNickname = getStr(params, "userNickname");
        String subject = getStr(params, "subject");
        String examStatus = getStr(params, "examStatus");
        String isPass = getStr(params, "isPass");

        QueryWrapper<ApExamRecordEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(userId), "user_id", userId);
        wrapper.like(StrUtil.isNotBlank(userNickname), "user_nickname", userNickname);
        wrapper.eq(StrUtil.isNotBlank(subject), "subject", subject);
        wrapper.eq(StrUtil.isNotBlank(examStatus), "exam_status", examStatus);
        wrapper.eq(StrUtil.isNotBlank(isPass), "is_pass", isPass);
        wrapper.orderByDesc("gmt_create");

        Page<ApExamRecordEntity> result = examRecordDao.selectPage(new Page<>(page, limit), wrapper);
        return new Result<PageData<ApExamRecordEntity>>().ok(new PageData<>(result.getRecords(), result.getTotal()));
    }

    private static String getStr(Map<String, Object> params, String key) {
        Object value = params.get(key);
        return value == null ? null : value.toString();
    }

    private static long parseLong(Object value, long defaultValue) {
        try {
            return value == null || StrUtil.isBlank(value.toString()) ? defaultValue : Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
