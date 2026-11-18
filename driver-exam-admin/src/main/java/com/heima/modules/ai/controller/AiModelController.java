package com.heima.modules.ai.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.ai.dao.AiModelDao;
import com.heima.ai.entity.AiModelEntity;
import com.heima.ai.service.AiModelService;
import com.heima.common.annotation.LogOperation;
import com.heima.common.page.PageData;
import com.heima.common.utils.Result;
import com.heima.common.validator.AssertUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

/**
 * AI 模型配置管理
 */
@RestController
@RequestMapping("/ai/model")
@Tag(name = "AI模型配置管理")
public class AiModelController {

    @Resource
    private AiModelDao aiModelDao;

    @Resource
    private AiModelService aiModelService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @RequiresPermissions("ai:model:page")
    public Result<PageData<AiModelEntity>> page(@RequestParam Map<String, Object> params) {
        long page = parseLong(params.get("page"), 1);
        long limit = parseLong(params.get("limit"), 10);
        String modelName = params.get("modelName") == null ? null : params.get("modelName").toString();
        String modelType = params.get("modelType") == null ? null : params.get("modelType").toString();
        String isActive = params.get("isActive") == null ? null : params.get("isActive").toString();

        QueryWrapper<AiModelEntity> wrapper = new QueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(modelName), "model_name", modelName);
        wrapper.eq(StrUtil.isNotBlank(modelType), "model_type", modelType);
        wrapper.eq(StrUtil.isNotBlank(isActive), "is_active", isActive);
        wrapper.orderByAsc("model_type").orderByAsc("sort_order").orderByDesc("gmt_create");

        Page<AiModelEntity> result = aiModelDao.selectPage(new Page<>(page, limit), wrapper);
        return new Result<PageData<AiModelEntity>>().ok(new PageData<>(result.getRecords(), result.getTotal()));
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @RequiresPermissions("ai:model:info")
    public Result<AiModelEntity> get(@PathVariable("id") Long id) {
        return new Result<AiModelEntity>().ok(aiModelService.getById(id));
    }

    @GetMapping("active")
    @Operation(summary = "获取已启用模型")
    public Result<AiModelEntity> active(@RequestParam(value = "modelType", defaultValue = "LLM") String modelType) {
        return new Result<AiModelEntity>().ok(aiModelService.getActive(modelType));
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @RequiresPermissions("ai:model:save")
    public Result<Long> save(@RequestBody AiModelEntity entity) {
        validate(entity);
        aiModelService.save(entity);
        return new Result<Long>().ok(entity.getId());
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @RequiresPermissions("ai:model:update")
    public Result update(@RequestBody AiModelEntity entity) {
        AssertUtils.isNull(entity.getId(), "id");
        validate(entity);
        aiModelService.update(entity);
        return new Result();
    }

    @PutMapping("activate/{id}")
    @Operation(summary = "启用")
    @LogOperation("启用")
    @RequiresPermissions("ai:model:activate")
    public Result activate(@PathVariable("id") Long id) {
        aiModelService.activate(id);
        return new Result();
    }

    @PostMapping("test/{id}")
    @Operation(summary = "测试可用")
    @LogOperation("测试可用")
    @RequiresPermissions("ai:model:test")
    public Result<String> test(@PathVariable("id") Long id) {
        return new Result<String>().ok(aiModelService.test(id));
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @RequiresPermissions("ai:model:delete")
    public Result delete(@RequestBody Long[] ids) {
        AssertUtils.isArrayEmpty(ids, "id");
        aiModelService.delete(Arrays.asList(ids));
        return new Result();
    }

    private void validate(AiModelEntity entity) {
        if (StrUtil.isBlank(entity.getModelName())) {
            throw new com.heima.common.exception.RenException("模型名称不能为空");
        }
        if (StrUtil.isBlank(entity.getModelType())) {
            throw new com.heima.common.exception.RenException("模型类型不能为空");
        }
        if (StrUtil.isBlank(entity.getBaseUrl())) {
            throw new com.heima.common.exception.RenException("api地址不能为空");
        }
    }

    private static long parseLong(Object value, long defaultValue) {
        try {
            return value == null || StrUtil.isBlank(value.toString()) ? defaultValue : Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
