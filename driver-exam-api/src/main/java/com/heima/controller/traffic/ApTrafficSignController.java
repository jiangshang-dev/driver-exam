package com.heima.controller.traffic;

import com.heima.business.traffic.dto.ApTrafficSignDTO;
import com.heima.business.traffic.service.ApTrafficSignService;
import com.heima.common.constant.Constant;
import com.heima.common.page.PageData;
import com.heima.common.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 交通标志表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-07
 */
@RestController
@RequestMapping("/aptrafficsign")
@Tag(name="交通标志表")
public class ApTrafficSignController {
    @Resource
    private ApTrafficSignService apTrafficSignService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
        @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", in = ParameterIn.QUERY, required = true, ref="int") ,
        @Parameter(name = Constant.LIMIT, description = "每页显示记录数", in = ParameterIn.QUERY,required = true, ref="int") ,
        @Parameter(name = Constant.ORDER_FIELD, description = "排序字段", in = ParameterIn.QUERY, ref="String") ,
        @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)", in = ParameterIn.QUERY, ref="String")
    })
    public Result<PageData<ApTrafficSignDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<ApTrafficSignDTO> page = apTrafficSignService.page(params);

        return new Result<PageData<ApTrafficSignDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    public Result<ApTrafficSignDTO> get(@PathVariable("id") Long id){
        ApTrafficSignDTO data = apTrafficSignService.get(id);

        return new Result<ApTrafficSignDTO>().ok(data);
    }

}
