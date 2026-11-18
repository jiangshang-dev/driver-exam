package com.heima.modules.mall;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.business.mall.dao.ApPointsLogDao;
import com.heima.business.mall.entity.ApPointsLogEntity;
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
 * 积分记录管理
 */
@RestController
@RequestMapping("/mall/pointsLog")
@Tag(name = "积分记录管理")
public class MallPointsLogController {

    @Resource
    private ApPointsLogDao pointsLogDao;

    @GetMapping("page")
    @Operation(summary = "分页")
    @RequiresPermissions("mall:pointslog:page")
    public Result<PageData<ApPointsLogEntity>> page(@RequestParam Map<String, Object> params) {
        long page = parseLong(params.get("page"), 1);
        long limit = parseLong(params.get("limit"), 10);
        String userId = params.get("userId") == null ? null : params.get("userId").toString();
        String sceneCode = params.get("sceneCode") == null ? null : params.get("sceneCode").toString();
        String bizId = params.get("bizId") == null ? null : params.get("bizId").toString();

        QueryWrapper<ApPointsLogEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StrUtil.isNotBlank(userId), "user_id", userId);
        wrapper.eq(StrUtil.isNotBlank(sceneCode), "scene_code", sceneCode);
        wrapper.like(StrUtil.isNotBlank(bizId), "biz_id", bizId);
        wrapper.orderByDesc("gmt_create");

        Page<ApPointsLogEntity> result = pointsLogDao.selectPage(new Page<>(page, limit), wrapper);
        return new Result<PageData<ApPointsLogEntity>>().ok(new PageData<>(result.getRecords(), result.getTotal()));
    }

    private static long parseLong(Object value, long defaultValue) {
        try {
            return value == null || StrUtil.isBlank(value.toString()) ? defaultValue : Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
