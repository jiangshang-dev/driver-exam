package com.heima.modules.user.controller;

import com.heima.business.auth.dto.ApUserDTO;
import com.heima.business.auth.service.user.ApUserService;
import com.heima.common.annotation.LogOperation;
import com.heima.common.constant.Constant;
import com.heima.common.page.PageData;
import com.heima.common.utils.ExcelUtils;
import com.heima.common.utils.Result;
import com.heima.common.validator.AssertUtils;
import com.heima.common.validator.ValidatorUtils;
import com.heima.common.validator.group.AddGroup;
import com.heima.common.validator.group.DefaultGroup;
import com.heima.common.validator.group.UpdateGroup;
import com.heima.modules.user.excel.ApUserExcel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 用户表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-01
 */
@RestController
@RequestMapping("/apuser")
@Tag(name="用户表")
public class ApUserController {
    @Resource
    private ApUserService apUserService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
        @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", in = ParameterIn.QUERY, required = true, ref="int") ,
        @Parameter(name = Constant.LIMIT, description = "每页显示记录数", in = ParameterIn.QUERY,required = true, ref="int") ,
        @Parameter(name = Constant.ORDER_FIELD, description = "排序字段", in = ParameterIn.QUERY, ref="String") ,
        @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)", in = ParameterIn.QUERY, ref="String")
    })
    @RequiresPermissions("apuser:page")
    public Result<PageData<ApUserDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<ApUserDTO> page = apUserService.pages(params);

        return new Result<PageData<ApUserDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @RequiresPermissions("apuser:info")
    public Result<ApUserDTO> get(@PathVariable("id") Long id){
        ApUserDTO data = apUserService.get(id);

        return new Result<ApUserDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @RequiresPermissions("apuser:save")
    public Result save(@RequestBody ApUserDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        apUserService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @RequiresPermissions("apuser:update")
    public Result update(@RequestBody ApUserDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        apUserService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @RequiresPermissions("apuser:delete")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        apUserService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @RequiresPermissions("apuser:export")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<ApUserDTO> list = apUserService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, "用户表", list, ApUserExcel.class);
    }

}
