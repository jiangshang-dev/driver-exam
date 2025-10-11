package com.heima.modules.uniad;

import com.heima.business.uniad.dto.ApAdWatchRecordDTO;
import com.heima.business.uniad.excel.ApAdWatchRecordExcel;
import com.heima.business.uniad.service.ApAdWatchRecordService;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


/**
 * 广告观看记录表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-08
 */
@RestController
@RequestMapping("apadwatchrecord")
@Tag(name="广告观看记录表")
public class ApAdWatchRecordController {
    @Resource
    private ApAdWatchRecordService apAdWatchRecordService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
        @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", in = ParameterIn.QUERY, required = true, ref="int") ,
        @Parameter(name = Constant.LIMIT, description = "每页显示记录数", in = ParameterIn.QUERY,required = true, ref="int") ,
        @Parameter(name = Constant.ORDER_FIELD, description = "排序字段", in = ParameterIn.QUERY, ref="String") ,
        @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)", in = ParameterIn.QUERY, ref="String")
    })
    @RequiresPermissions("apadwatchrecord:page")
    public Result<PageData<ApAdWatchRecordDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<ApAdWatchRecordDTO> page = apAdWatchRecordService.page(params);

        return new Result<PageData<ApAdWatchRecordDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @RequiresPermissions("demo:apadwatchrecord:info")
    public Result<ApAdWatchRecordDTO> get(@PathVariable("id") Long id){
        ApAdWatchRecordDTO data = apAdWatchRecordService.get(id);

        return new Result<ApAdWatchRecordDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @RequiresPermissions("demo:apadwatchrecord:save")
    public Result save(@RequestBody ApAdWatchRecordDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        apAdWatchRecordService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @RequiresPermissions("demo:apadwatchrecord:update")
    public Result update(@RequestBody ApAdWatchRecordDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        apAdWatchRecordService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @RequiresPermissions("demo:apadwatchrecord:delete")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        apAdWatchRecordService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @RequiresPermissions("demo:apadwatchrecord:export")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<ApAdWatchRecordDTO> list = apAdWatchRecordService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, "广告观看记录表", list, ApAdWatchRecordExcel.class);
    }

}
