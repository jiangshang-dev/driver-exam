package com.heima.modules.traffic;

import com.heima.business.traffic.dto.ApVideoCommentDTO;
import com.heima.business.traffic.service.ApVideoCommentService;
import com.heima.common.annotation.LogOperation;
import com.heima.common.constant.Constant;
import com.heima.common.page.PageData;
import com.heima.common.utils.Result;
import com.heima.common.validator.AssertUtils;
import com.heima.common.validator.ValidatorUtils;
import com.heima.common.validator.group.AddGroup;
import com.heima.common.validator.group.DefaultGroup;
import com.heima.common.validator.group.UpdateGroup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import java.util.Map;


/**
 * 视频评论表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-12
 */
@RestController
@RequestMapping("/apvideocomment")
@Tag(name="视频评论表")
public class ApVideoCommentController {
    @Autowired
    private ApVideoCommentService apVideoCommentService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
        @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", in = ParameterIn.QUERY, required = true, ref="int") ,
        @Parameter(name = Constant.LIMIT, description = "每页显示记录数", in = ParameterIn.QUERY,required = true, ref="int") ,
        @Parameter(name = Constant.ORDER_FIELD, description = "排序字段", in = ParameterIn.QUERY, ref="String") ,
        @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)", in = ParameterIn.QUERY, ref="String")
    })
    @RequiresPermissions("apvideocomment:page")
    public Result<PageData<ApVideoCommentDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<ApVideoCommentDTO> page = apVideoCommentService.page(params);

        return new Result<PageData<ApVideoCommentDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @RequiresPermissions("apvideocomment:info")
    public Result<ApVideoCommentDTO> get(@PathVariable("id") Long id){
        ApVideoCommentDTO data = apVideoCommentService.get(id);

        return new Result<ApVideoCommentDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @RequiresPermissions("apvideocomment:save")
    public Result save(@RequestBody ApVideoCommentDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        apVideoCommentService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @RequiresPermissions("apvideocomment:update")
    public Result update(@RequestBody ApVideoCommentDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        apVideoCommentService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @RequiresPermissions("apvideocomment:delete")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        apVideoCommentService.delete(ids);

        return new Result();
    }

}
