package com.heima.controller.uniad;

import com.heima.business.auth.dto.LoginResult;
import com.heima.business.uniad.dto.ApAdWatchRecordDTO;
import com.heima.business.uniad.service.ApAdWatchRecordService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 广告观看记录表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-08
 */
@RestController
@RequestMapping("/apadwatchrecord")
@Tag(name="广告观看记录表")
public class ApAdWatchRecordController {
    @Autowired
    private ApAdWatchRecordService apAdWatchRecordService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
        @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", in = ParameterIn.QUERY, required = true, ref="int") ,
        @Parameter(name = Constant.LIMIT, description = "每页显示记录数", in = ParameterIn.QUERY,required = true, ref="int") ,
        @Parameter(name = Constant.ORDER_FIELD, description = "排序字段", in = ParameterIn.QUERY, ref="String") ,
        @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)", in = ParameterIn.QUERY, ref="String")
    })
    public Result<PageData<ApAdWatchRecordDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<ApAdWatchRecordDTO> page = apAdWatchRecordService.page(params);

        return new Result<PageData<ApAdWatchRecordDTO>>().ok(page);
    }

    @GetMapping("adWatchRecord")
    @Operation(summary = "信息")
    public Result<Map<String, Object>> adWatchRecord(@RequestHeader("Authorization") String token){
        LoginResult loginResult = apAdWatchRecordService.adWatchRecord(token);
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("id", loginResult.getId());
        result.put("account", loginResult.getAccount());
        result.put("nickname", loginResult.getNickname());
        result.put("mobile", loginResult.getMobile());
        result.put("token", loginResult.getToken());
        result.put("vipLevel", loginResult.getVipLevel());
        result.put("vipExpireTime", loginResult.getVipExpireTime());
        result.put("level", loginResult.getLevel());
        result.put("avatar", loginResult.getAvatar());
        result.put("integral", loginResult.getIntegral());
        return new Result<Map<String, Object>>().ok(result);
    }

    @PostMapping
    @Operation(summary = "保存")
    public Result save(@RequestBody ApAdWatchRecordDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        apAdWatchRecordService.save(dto);

        return new Result();
    }

}
