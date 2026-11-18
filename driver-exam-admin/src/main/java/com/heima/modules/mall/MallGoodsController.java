package com.heima.modules.mall;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.business.mall.dao.ApMallGoodsDao;
import com.heima.business.mall.entity.ApMallGoodsEntity;
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
 * 积分商城商品管理
 */
@RestController
@RequestMapping("/mall/goods")
@Tag(name = "积分商城商品管理")
public class MallGoodsController {

    @Resource
    private ApMallGoodsDao goodsDao;

    @GetMapping("page")
    @Operation(summary = "分页")
    @RequiresPermissions("mall:goods:page")
    public Result<PageData<ApMallGoodsEntity>> page(@RequestParam Map<String, Object> params) {
        long page = parseLong(params.get("page"), 1);
        long limit = parseLong(params.get("limit"), 10);
        String goodsName = getStr(params, "goodsName");
        String goodsType = getStr(params, "goodsType");
        String status = getStr(params, "status");

        QueryWrapper<ApMallGoodsEntity> wrapper = new QueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(goodsName), "goods_name", goodsName);
        wrapper.eq(StrUtil.isNotBlank(goodsType), "goods_type", goodsType);
        wrapper.eq(StrUtil.isNotBlank(status), "status", status);
        wrapper.orderByAsc("sort_order").orderByDesc("id");

        Page<ApMallGoodsEntity> result = goodsDao.selectPage(new Page<>(page, limit), wrapper);
        return new Result<PageData<ApMallGoodsEntity>>().ok(new PageData<>(result.getRecords(), result.getTotal()));
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @RequiresPermissions("mall:goods:info")
    public Result<ApMallGoodsEntity> get(@PathVariable("id") Long id) {
        return new Result<ApMallGoodsEntity>().ok(goodsDao.selectById(id));
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @RequiresPermissions("mall:goods:save")
    public Result save(@RequestBody ApMallGoodsEntity entity) {
        entity.setId(null);
        entity.setGmtCreate(LocalDateTime.now());
        goodsDao.insert(entity);
        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @RequiresPermissions("mall:goods:update")
    public Result update(@RequestBody ApMallGoodsEntity entity) {
        AssertUtils.isNull(entity.getId(), "id");
        goodsDao.updateById(entity);
        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @RequiresPermissions("mall:goods:delete")
    public Result delete(@RequestBody Long[] ids) {
        AssertUtils.isArrayEmpty(ids, "id");
        goodsDao.deleteBatchIds(Arrays.asList(ids));
        return new Result();
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
