package com.heima.modules.learning;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.business.study.dao.ApUserStudyRecordDao;
import com.heima.business.study.entity.ApUserStudyRecordEntity;
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
 * 学习记录管理
 */
@RestController
@RequestMapping("/learning/studyRecord")
@Tag(name = "学习记录管理")
public class StudyRecordController {

    @Resource
    private ApUserStudyRecordDao studyRecordDao;

    @GetMapping("page")
    @Operation(summary = "分页")
    @RequiresPermissions("study:record:page")
    public Result<PageData<ApUserStudyRecordEntity>> page(@RequestParam Map<String, Object> params) {
        long page = parseLong(params.get("page"), 1);
        long limit = parseLong(params.get("limit"), 10);
        String userId = getStr(params, "userId");
        String subject = getStr(params, "subject");
        String studyType = getStr(params, "studyType");
        String startDate = getStr(params, "startDate");
        String endDate = getStr(params, "endDate");

        QueryWrapper<ApUserStudyRecordEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(userId), "user_id", userId);
        wrapper.eq(StrUtil.isNotBlank(subject), "subject", subject);
        wrapper.eq(StrUtil.isNotBlank(studyType), "study_type", studyType);
        wrapper.ge(StrUtil.isNotBlank(startDate), "study_date", startDate);
        wrapper.le(StrUtil.isNotBlank(endDate), "study_date", endDate + " 23:59:59");
        wrapper.orderByDesc("study_date");

        Page<ApUserStudyRecordEntity> result = studyRecordDao.selectPage(new Page<>(page, limit), wrapper);
        return new Result<PageData<ApUserStudyRecordEntity>>().ok(new PageData<>(result.getRecords(), result.getTotal()));
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
