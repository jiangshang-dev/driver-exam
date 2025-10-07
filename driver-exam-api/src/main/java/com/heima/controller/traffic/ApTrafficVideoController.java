package com.heima.controller.traffic;

import com.heima.business.traffic.dto.ApTrafficVideoDTO;
import com.heima.business.traffic.service.ApTrafficVideoService;
import com.heima.common.constant.Constant;
import com.heima.common.page.PageData;
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
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 交规视频表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-07
 */
@RestController
@RequestMapping("/aptrafficvideo")
@Tag(name="交规视频表")
public class ApTrafficVideoController {
    @Resource
    private ApTrafficVideoService apTrafficVideoService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
        @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", in = ParameterIn.QUERY, required = true, ref="int") ,
        @Parameter(name = Constant.LIMIT, description = "每页显示记录数", in = ParameterIn.QUERY,required = true, ref="int") ,
        @Parameter(name = Constant.ORDER_FIELD, description = "排序字段", in = ParameterIn.QUERY, ref="String") ,
        @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)", in = ParameterIn.QUERY, ref="String")
    })
    public Result<PageData<ApTrafficVideoDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<ApTrafficVideoDTO> page = apTrafficVideoService.page(params);

        return new Result<PageData<ApTrafficVideoDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    public Result<ApTrafficVideoDTO> get(@PathVariable("id") Long id){
        ApTrafficVideoDTO data = apTrafficVideoService.get(id);

        return new Result<ApTrafficVideoDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    public Result save(@RequestBody ApTrafficVideoDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        apTrafficVideoService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    public Result update(@RequestBody ApTrafficVideoDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        apTrafficVideoService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        apTrafficVideoService.delete(ids);

        return new Result();
    }

}
