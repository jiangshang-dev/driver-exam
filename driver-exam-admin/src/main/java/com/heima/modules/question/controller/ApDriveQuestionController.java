package com.heima.modules.question.controller;

import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.heima.business.question.dto.ApDriveQuestionDTO;
import com.heima.business.enums.ExamTypeEnums;
import com.heima.business.enums.SubjectEnums;
import com.heima.business.question.excel.ApDriveQuestionExcel;
import com.heima.business.question.service.ApDriveQuestionService;
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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 驾考题目表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-01
 */
@RestController
@RequestMapping("/apdrivequestion")
@Tag(name="驾考题目表")
public class ApDriveQuestionController {
    @Resource
    private ApDriveQuestionService apDriveQuestionService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
        @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", in = ParameterIn.QUERY, required = true, ref="int") ,
        @Parameter(name = Constant.LIMIT, description = "每页显示记录数", in = ParameterIn.QUERY,required = true, ref="int") ,
        @Parameter(name = Constant.ORDER_FIELD, description = "排序字段", in = ParameterIn.QUERY, ref="String") ,
        @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)", in = ParameterIn.QUERY, ref="String")
    })
    @RequiresPermissions("apdrivequestion:page")
    public Result<PageData<ApDriveQuestionDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<ApDriveQuestionDTO> page = apDriveQuestionService.page(params);

        return new Result<PageData<ApDriveQuestionDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @RequiresPermissions("apdrivequestion:info")
    public Result<ApDriveQuestionDTO> get(@PathVariable("id") Long id){
        ApDriveQuestionDTO data = apDriveQuestionService.get(id);

        return new Result<ApDriveQuestionDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @RequiresPermissions("apdrivequestion:save")
    public Result save(@RequestBody ApDriveQuestionDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        apDriveQuestionService.save(dto);

        return new Result();
    }

    // 添加一个预处理方法
    private ApDriveQuestionDTO preprocessDto(ApDriveQuestionDTO dto) {
        if (dto != null && dto.getQuestionExplain() != null) {
            String explain = dto.getQuestionExplain();
            // 转义常见的控制字符
            explain = explain.replace("\n", "\\n")
                             .replace("\r", "\\r")
                             .replace("\t", "\\t")
                             .replace("\"", "\\\"");
            dto.setQuestionExplain(explain);
        }
        return dto;
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @RequiresPermissions("apdrivequestion:update")
    public Result update(@RequestBody ApDriveQuestionDTO dto){
        // 预处理DTO
        dto = preprocessDto(dto);
        
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        apDriveQuestionService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @RequiresPermissions("apdrivequestion:delete")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        apDriveQuestionService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @RequiresPermissions("apdrivequestion:export")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<ApDriveQuestionDTO> list = apDriveQuestionService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, "驾考题目表", list, ApDriveQuestionExcel.class);
    }

    @GetMapping("syncQuestion")
    @Operation(summary = "同步试题")
    @LogOperation("同步试题")
    @RequiresPermissions("apdrivequestion:sync")
    public Result syncQuestion(@RequestParam(value = "subject", defaultValue = "ONE") SubjectEnums subject,
                             @RequestParam(value = "model", defaultValue = "C1") ExamTypeEnums model,
                             @RequestParam(value = "testType", defaultValue = "order") String testType) {
        // 手动验证参数
        if (subject == null) {
            return new Result().error("学科不允许为空！");
        }
        if (model == null) {
            return new Result().error("驾考类型不允许为空！");
        }
        if (testType == null || testType.trim().isEmpty()) {
            return new Result().error("测试类型不允许为空！");
        }
        
        apDriveQuestionService.syncQuestion(subject, model, testType);
        return new Result().ok("同步成功");
    }

}
