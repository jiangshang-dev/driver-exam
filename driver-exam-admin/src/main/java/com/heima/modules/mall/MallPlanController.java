package com.heima.modules.mall;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.business.mall.dao.ApMemberPlanDao;
import com.heima.business.mall.entity.ApMemberPlanEntity;
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
 * 会员套餐管理
 */
@RestController
@RequestMapping("/mall/plan")
@Tag(name = "会员套餐管理")
public class MallPlanController {

    @Resource
    private ApMemberPlanDao planDao;

    @GetMapping("page")
    @Operation(summary = "分页")
    @RequiresPermissions("mall:plan:page")
    public Result<PageData<ApMemberPlanEntity>> page(@RequestParam Map<String, Object> params) {
        long page = parseLong(params.get("page"), 1);
        long limit = parseLong(params.get("limit"), 10);
        String planName = params.get("planName") == null ? null : params.get("planName").toString();
        String status = params.get("status") == null ? null : params.get("status").toString();

        QueryWrapper<ApMemberPlanEntity> wrapper = new QueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(planName), "plan_name", planName);
        wrapper.eq(StrUtil.isNotBlank(status), "status", status);
        wrapper.orderByAsc("sort_order").orderByDesc("id");

        Page<ApMemberPlanEntity> result = planDao.selectPage(new Page<>(page, limit), wrapper);
        return new Result<PageData<ApMemberPlanEntity>>().ok(new PageData<>(result.getRecords(), result.getTotal()));
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @RequiresPermissions("mall:plan:info")
    public Result<ApMemberPlanEntity> get(@PathVariable("id") Long id) {
        return new Result<ApMemberPlanEntity>().ok(planDao.selectById(id));
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @RequiresPermissions("mall:plan:save")
    public Result save(@RequestBody ApMemberPlanEntity entity) {
        entity.setId(null);
        entity.setGmtCreate(LocalDateTime.now());
        planDao.insert(entity);
        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @RequiresPermissions("mall:plan:update")
    public Result update(@RequestBody ApMemberPlanEntity entity) {
        AssertUtils.isNull(entity.getId(), "id");
        planDao.updateById(entity);
        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @RequiresPermissions("mall:plan:delete")
    public Result delete(@RequestBody Long[] ids) {
        AssertUtils.isArrayEmpty(ids, "id");
        planDao.deleteBatchIds(Arrays.asList(ids));
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
