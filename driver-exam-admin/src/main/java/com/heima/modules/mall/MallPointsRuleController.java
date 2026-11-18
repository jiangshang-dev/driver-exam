package com.heima.modules.mall;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.business.mall.dao.ApPointsRuleDao;
import com.heima.business.mall.entity.ApPointsRuleEntity;
import com.heima.common.annotation.LogOperation;
import com.heima.common.page.PageData;
import com.heima.common.utils.Result;
import com.heima.common.validator.AssertUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

/**
 * 积分规则管理
 */
@RestController
@RequestMapping("/mall/pointsRule")
@Tag(name = "积分规则管理")
public class MallPointsRuleController {

    @Resource
    private ApPointsRuleDao pointsRuleDao;

    @GetMapping("page")
    @Operation(summary = "分页")
    @RequiresPermissions("mall:pointsrule:page")
    public Result<PageData<ApPointsRuleEntity>> page(@RequestParam Map<String, Object> params) {
        long page = parseLong(params.get("page"), 1);
        long limit = parseLong(params.get("limit"), 10);
        String sceneName = params.get("sceneName") == null ? null : params.get("sceneName").toString();
        String sceneCode = params.get("sceneCode") == null ? null : params.get("sceneCode").toString();
        String enabled = params.get("enabled") == null ? null : params.get("enabled").toString();

        QueryWrapper<ApPointsRuleEntity> wrapper = new QueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(sceneName), "scene_name", sceneName);
        wrapper.like(StrUtil.isNotBlank(sceneCode), "scene_code", sceneCode);
        wrapper.eq(StrUtil.isNotBlank(enabled), "enabled", enabled);
        wrapper.orderByDesc("id");

        Page<ApPointsRuleEntity> result = pointsRuleDao.selectPage(new Page<>(page, limit), wrapper);
        return new Result<PageData<ApPointsRuleEntity>>().ok(new PageData<>(result.getRecords(), result.getTotal()));
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @RequiresPermissions("mall:pointsrule:info")
    public Result<ApPointsRuleEntity> get(@PathVariable("id") Long id) {
        return new Result<ApPointsRuleEntity>().ok(pointsRuleDao.selectById(id));
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @RequiresPermissions("mall:pointsrule:save")
    public Result save(@RequestBody ApPointsRuleEntity entity) {
        entity.setId(null);
        entity.setGmtCreate(LocalDateTime.now());
        pointsRuleDao.insert(entity);
        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @RequiresPermissions("mall:pointsrule:update")
    public Result update(@RequestBody ApPointsRuleEntity entity) {
        AssertUtils.isNull(entity.getId(), "id");
        pointsRuleDao.updateById(entity);
        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @RequiresPermissions("mall:pointsrule:delete")
    public Result delete(@RequestBody Long[] ids) {
        AssertUtils.isArrayEmpty(ids, "id");
        pointsRuleDao.deleteBatchIds(Arrays.asList(ids));
        return new Result();
    }

    private static long parseLong(Object value, long defaultValue) {
        try {
            return value == null || StrUtil.isBlank(value.toString()) ? defaultValue : Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
