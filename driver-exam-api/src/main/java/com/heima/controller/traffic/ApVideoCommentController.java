package com.heima.controller.traffic;

import java.util.List;
import java.util.Map;

import cn.hutool.json.JSONUtil;
import com.heima.business.auth.entity.ApUserEntity;
import com.heima.business.auth.service.user.ApUserService;
import com.heima.business.common.aliyun.GreenTextScan;
import com.heima.business.constants.MqContants;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.heima.business.traffic.dto.ApVideoCommentDTO;
import com.heima.business.traffic.service.ApVideoCommentService;
import com.heima.common.constant.Constant;
import com.heima.common.page.PageData;
import com.heima.common.utils.JwtUtils;
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

/**
 * 视频评论表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-12
 */
@RestController
@RequestMapping("apvideocomment")
@Tag(name = "视频评论表")
public class ApVideoCommentController {

    @Resource
    private ApVideoCommentService apVideoCommentService;

    @Resource
    private ApUserService apUserService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", in = ParameterIn.QUERY, required = true, ref = "int"),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", in = ParameterIn.QUERY, required = true, ref = "int"),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段", in = ParameterIn.QUERY, ref = "String"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)", in = ParameterIn.QUERY, ref = "String")
    })
    public Result<PageData<ApVideoCommentDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        PageData<ApVideoCommentDTO> page = apVideoCommentService.page(params);

        return new Result<PageData<ApVideoCommentDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    public Result<ApVideoCommentDTO> get(@PathVariable("id") Long id) {
        ApVideoCommentDTO data = apVideoCommentService.get(id);

        return new Result<ApVideoCommentDTO>().ok(data);
    }

    @GetMapping("byVideo/{videoId}")
    @Operation(summary = "根据视频ID获取审核通过的评论")
    public Result<List<ApVideoCommentDTO>> getCommentsByVideoId(@PathVariable("videoId") Long videoId, @RequestHeader("Authorization") String token) {
        List<ApVideoCommentDTO> comments = apVideoCommentService.getApprovedCommentsByVideoId(videoId, token);
        return new Result<List<ApVideoCommentDTO>>().ok(comments);
    }

    @PostMapping
    @Operation(summary = "保存")
    public Result save(@RequestBody ApVideoCommentDTO dto, @RequestHeader("Authorization") String token) {
        // 效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        Long userId = JwtUtils.getUserId(token);
        ApUserEntity apUser = apUserService.getById(userId);
        AssertUtils.isNull(apUser, "用户不存在");
        if (!apUser.getStatus()) {
            return new Result<>().error("用户被禁用");
        }
        Boolean commentPermission = apUser.getCommentPermission();
        if (!commentPermission) {
            return new Result<>().error("违规过多,评论已禁用");
        }

        dto.setUserId(userId);
        dto.setAuditStatus(0); // 待审核
        apVideoCommentService.save(dto);

        String message = JSONUtil.toJsonStr(dto);
        rabbitTemplate.convertAndSend(MqContants.DRIVER_EXCHANGE, MqContants.DRIVER_INSERT_KEY, message);
        return new Result();
    }

    @PostMapping("reply")
    @Operation(summary = "回复评论")
    public Result<ApVideoCommentDTO> reply(@RequestBody Map<String, Object> params, @RequestHeader("Authorization") String token) {
        Long parentId = Long.valueOf(params.get("parentId").toString());
        String replyContent = params.get("replyContent").toString();
        Long userId = JwtUtils.getUserId(token);

        ApVideoCommentDTO reply = apVideoCommentService.replyToComment(parentId, replyContent, userId);

        ApUserEntity apUser = apUserService.getById(userId);
        Boolean commentPermission = apUser.getCommentPermission();
        if (!commentPermission) {
            return new Result<ApVideoCommentDTO>().error("违规过多,功能已禁用");
        }

        String message = JSONUtil.toJsonStr(reply);
        rabbitTemplate.convertAndSend(MqContants.DRIVER_EXCHANGE, MqContants.DRIVER_INSERT_KEY, message);
        return new Result<ApVideoCommentDTO>().ok(reply);
    }

    @PutMapping
    @Operation(summary = "修改")
    public Result update(@RequestBody ApVideoCommentDTO dto) {
        // 效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        apVideoCommentService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    public Result delete(@RequestBody Long[] ids) {
        // 效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        apVideoCommentService.delete(ids);

        return new Result();
    }

    @GetMapping("liked/{id}")
    @Operation(summary = "点赞")
    public Result liked(@PathVariable("id") String id, @RequestHeader("Authorization") String token){
        boolean flag = apVideoCommentService.liked(id, token);
        return new Result().ok(flag);
    }

}
