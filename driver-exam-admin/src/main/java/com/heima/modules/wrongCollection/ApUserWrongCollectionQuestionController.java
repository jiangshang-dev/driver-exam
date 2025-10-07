package com.heima.modules.wrongCollection;

import com.heima.business.wrongCollection.dto.ApUserWrongCollectionQuestionDTO;
import com.heima.business.wrongCollection.excel.ApUserWrongCollectionQuestionExcel;
import com.heima.business.wrongCollection.service.ApUserWrongCollectionQuestionService;
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
 * 用户收藏_错题本表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-06
 */
@RestController
@RequestMapping("/apuserwrongcollectionquestion")
@Tag(name="用户收藏_错题本表")
public class ApUserWrongCollectionQuestionController {
    @Resource
    private ApUserWrongCollectionQuestionService apUserWrongCollectionQuestionService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
        @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", in = ParameterIn.QUERY, required = true, ref="int") ,
        @Parameter(name = Constant.LIMIT, description = "每页显示记录数", in = ParameterIn.QUERY,required = true, ref="int") ,
        @Parameter(name = Constant.ORDER_FIELD, description = "排序字段", in = ParameterIn.QUERY, ref="String") ,
        @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)", in = ParameterIn.QUERY, ref="String")
    })
    @RequiresPermissions("apuserwrongcollectionquestion:page")
    public Result<PageData<ApUserWrongCollectionQuestionDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<ApUserWrongCollectionQuestionDTO> page = apUserWrongCollectionQuestionService.page(params);

        return new Result<PageData<ApUserWrongCollectionQuestionDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @RequiresPermissions("apuserwrongcollectionquestion:info")
    public Result<ApUserWrongCollectionQuestionDTO> get(@PathVariable("id") Long id){
        ApUserWrongCollectionQuestionDTO data = apUserWrongCollectionQuestionService.get(id);

        return new Result<ApUserWrongCollectionQuestionDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @RequiresPermissions("apuserwrongcollectionquestion:save")
    public Result save(@RequestBody ApUserWrongCollectionQuestionDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        apUserWrongCollectionQuestionService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @RequiresPermissions("apuserwrongcollectionquestion:update")
    public Result update(@RequestBody ApUserWrongCollectionQuestionDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        apUserWrongCollectionQuestionService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @RequiresPermissions("apuserwrongcollectionquestion:delete")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        apUserWrongCollectionQuestionService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @RequiresPermissions("apuserwrongcollectionquestion:export")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<ApUserWrongCollectionQuestionDTO> list = apUserWrongCollectionQuestionService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, "用户收藏_错题本表", list, ApUserWrongCollectionQuestionExcel.class);
    }

}
