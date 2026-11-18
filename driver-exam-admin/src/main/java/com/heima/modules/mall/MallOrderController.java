package com.heima.modules.mall;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.business.mall.dao.ApMallOrderDao;
import com.heima.business.mall.entity.ApMallOrderEntity;
import com.heima.business.mall.service.MallService;
import com.heima.common.annotation.LogOperation;
import com.heima.common.page.PageData;
import com.heima.common.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商城订单管理
 */
@RestController
@RequestMapping("/mall/order")
@Tag(name = "商城订单管理")
public class MallOrderController {

    @Resource
    private ApMallOrderDao orderDao;

    @Resource
    private MallService mallService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @RequiresPermissions("mall:order:page")
    public Result<PageData<ApMallOrderEntity>> page(@RequestParam Map<String, Object> params) {
        long page = parseLong(params.get("page"), 1);
        long limit = parseLong(params.get("limit"), 10);
        String orderNo = params.get("orderNo") == null ? null : params.get("orderNo").toString();
        String userId = params.get("userId") == null ? null : params.get("userId").toString();
        String status = params.get("status") == null ? null : params.get("status").toString();
        String orderType = params.get("orderType") == null ? null : params.get("orderType").toString();
        String goodsName = params.get("goodsName") == null ? null : params.get("goodsName").toString();

        QueryWrapper<ApMallOrderEntity> wrapper = new QueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(orderNo), "order_no", orderNo);
        wrapper.eq(StrUtil.isNotBlank(userId), "user_id", userId);
        wrapper.eq(StrUtil.isNotBlank(status), "status", status);
        wrapper.eq(StrUtil.isNotBlank(orderType), "order_type", orderType);
        wrapper.like(StrUtil.isNotBlank(goodsName), "goods_name", goodsName);
        wrapper.orderByDesc("gmt_create");

        Page<ApMallOrderEntity> result = orderDao.selectPage(new Page<>(page, limit), wrapper);
        return new Result<PageData<ApMallOrderEntity>>().ok(new PageData<>(result.getRecords(), result.getTotal()));
    }

    @GetMapping("{orderNo}")
    @Operation(summary = "信息")
    @RequiresPermissions("mall:order:info")
    public Result<ApMallOrderEntity> get(@PathVariable("orderNo") String orderNo) {
        return new Result<ApMallOrderEntity>().ok(orderDao.selectOne(new QueryWrapper<ApMallOrderEntity>().eq("order_no", orderNo).last("LIMIT 1")));
    }

    @PutMapping("ship")
    @Operation(summary = "发货")
    @LogOperation("发货")
    @RequiresPermissions("mall:order:ship")
    public Result ship(@RequestBody Map<String, Object> params) {
        String orderNo = params.get("orderNo") == null ? null : params.get("orderNo").toString();
        String expressCompany = params.get("expressCompany") == null ? null : params.get("expressCompany").toString();
        String expressNo = params.get("expressNo") == null ? null : params.get("expressNo").toString();
        if (StrUtil.isBlank(orderNo)) {
            return new Result().error("订单号不能为空");
        }
        mallService.ship(orderNo, expressCompany, expressNo);
        return new Result();
    }

    @PutMapping("paid")
    @Operation(summary = "标记已支付")
    @LogOperation("标记已支付")
    @RequiresPermissions("mall:order:paid")
    public Result paid(@RequestBody Map<String, Object> params) {
        String orderNo = params.get("orderNo") == null ? null : params.get("orderNo").toString();
        if (StrUtil.isBlank(orderNo)) {
            return new Result().error("订单号不能为空");
        }
        mallService.markPaid(orderNo);
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
