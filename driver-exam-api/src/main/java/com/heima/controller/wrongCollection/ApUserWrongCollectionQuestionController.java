package com.heima.controller.wrongCollection;

import java.time.LocalDateTime;
import java.util.Map;

import com.heima.common.constant.Constant;
import com.heima.common.page.PageData;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.business.enums.RecordTypeEnums;
import com.heima.business.wrongCollection.dto.ApUserWrongCollectionQuestionDTO;
import com.heima.business.wrongCollection.service.ApUserWrongCollectionQuestionService;
import com.heima.common.utils.Result;
import com.heima.common.validator.ValidatorUtils;
import com.heima.common.validator.group.AddGroup;
import com.heima.common.validator.group.DefaultGroup;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;

/**
 * 用户收藏_错题本表
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0 2025-10-06
 */
@RestController
@RequestMapping("/userWrongCollectionQuestion")
@Tag(name = "用户收藏_错题本表")
public class ApUserWrongCollectionQuestionController {
    @Resource
    private ApUserWrongCollectionQuestionService apUserWrongCollectionQuestionService;

    @GetMapping("myCollectPage")
    @Operation(summary = "我的收藏-分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", in = ParameterIn.QUERY, required = true, ref = "int"),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", in = ParameterIn.QUERY, required = true, ref = "int"),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段", in = ParameterIn.QUERY, ref = "String"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)", in = ParameterIn.QUERY, ref = "String")
    })
    public Result<PageData<ApUserWrongCollectionQuestionDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        PageData<ApUserWrongCollectionQuestionDTO> page = apUserWrongCollectionQuestionService.page(params);

        return new Result<PageData<ApUserWrongCollectionQuestionDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    public Result<ApUserWrongCollectionQuestionDTO> get(@PathVariable("id") Long id) {
        ApUserWrongCollectionQuestionDTO data = apUserWrongCollectionQuestionService.get(id);

        return new Result<ApUserWrongCollectionQuestionDTO>().ok(data);
    }

    @GetMapping("isCollected")
    @Operation(summary = "判断是否已收藏")
    public Result<Boolean> isCollected(@RequestParam("userId") Long userId, @RequestParam("questionId") Long questionId) {
        // 检查是否已存在该题目的收藏记录
        ApUserWrongCollectionQuestionDTO existingDto = apUserWrongCollectionQuestionService.getOne(
                new QueryWrapper<com.heima.business.wrongCollection.entity.ApUserWrongCollectionQuestionEntity>()
                        .eq("user_id", userId)
                        .eq("question_id", questionId)
                        .eq("record_type", RecordTypeEnums.COLLECT.getValue())
        );

        // 如果存在记录则表示已收藏，否则未收藏
        boolean isCollected = existingDto != null;
        return new Result<Boolean>().ok(isCollected);
    }

    @PostMapping("collect")
    @Operation(summary = "收藏/取消收藏")
    public Result collect(@RequestBody ApUserWrongCollectionQuestionDTO dto) {
        // 效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        // 检查是否已存在该题目的收藏记录
        ApUserWrongCollectionQuestionDTO existingDto = apUserWrongCollectionQuestionService.getOne(
                new QueryWrapper<com.heima.business.wrongCollection.entity.ApUserWrongCollectionQuestionEntity>()
                        .eq("user_id", dto.getUserId())
                        .eq("subject", dto.getSubject())
                        .eq("question_id", dto.getQuestionId())
                        .eq("record_type", RecordTypeEnums.COLLECT.getValue())
        );

        if (existingDto != null) {
            // 如果已收藏，则取消收藏（删除记录）
            apUserWrongCollectionQuestionService.delete(new Long[]{existingDto.getId()});
            return new Result().ok("取消收藏成功");
        } else {
            // 如果未收藏，则添加收藏
            dto.setRecordType(RecordTypeEnums.COLLECT);
            dto.setFirstWrongTime(null);
            dto.setLastWrongTime(null);
            dto.setMasteredTime(LocalDateTime.now());
            dto.setWrongCount(0);
            dto.setLastWrongAnswer(null);
            apUserWrongCollectionQuestionService.save(dto);
            return new Result().ok("收藏成功");
        }
    }

    @PostMapping("wrong")
    @Operation(summary = "错题")
    public Result wrong(@RequestBody ApUserWrongCollectionQuestionDTO dto) {
        // 效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        // 检查是否已存在该题目的错题记录
        ApUserWrongCollectionQuestionDTO existingDto = apUserWrongCollectionQuestionService.getOne(
                new QueryWrapper<com.heima.business.wrongCollection.entity.ApUserWrongCollectionQuestionEntity>()
                        .eq("user_id", dto.getUserId())
                        .eq("subject", dto.getSubject())
                        .eq("question_id", dto.getQuestionId())
                        .eq("record_type", RecordTypeEnums.WRONG.getValue())
        );

        if (existingDto != null) {
            // 如果已存在，则更新错题记录
            existingDto.setLastWrongTime(LocalDateTime.now());
            if (existingDto.getWrongCount() != null) {
                existingDto.setWrongCount(existingDto.getWrongCount() + 1);
            } else {
                existingDto.setWrongCount(1);
            }
            existingDto.setLastWrongAnswer(dto.getLastWrongAnswer());
            apUserWrongCollectionQuestionService.update(existingDto);
        } else {
            // 如果不存在，则新增错题记录
            dto.setRecordType(RecordTypeEnums.WRONG);
            dto.setFirstWrongTime(LocalDateTime.now());
            dto.setLastWrongTime(LocalDateTime.now());
            dto.setMasteredTime(null);
            dto.setWrongCount(1);
            apUserWrongCollectionQuestionService.save(dto);
        }
        return new Result();
    }

}