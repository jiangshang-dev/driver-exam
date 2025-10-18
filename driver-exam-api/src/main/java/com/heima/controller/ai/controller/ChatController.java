package com.heima.controller.ai.controller;

import cn.hutool.core.util.StrUtil;
import com.heima.ai.aiservice.ConsultantService;
import com.heima.business.auth.entity.ApUserEntity;
import com.heima.business.auth.service.user.ApUserService;
import com.heima.business.common.aliyun.GreenTextScan;
import com.heima.business.traffic.entity.ApVideoCommentEntity;
import com.heima.common.utils.JwtUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/ai")
public class ChatController {
    @Resource
    private ConsultantService consultantService;

    @Resource
    private ApUserService apUserService;

    @Resource
    private GreenTextScan greenTextScan;

    @PostMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public Flux<String> chat(@RequestBody Map<String, String> params, @RequestHeader("Authorization") String token){
        //登录用户信息
        Long userId = JwtUtils.getUserId(token);
        if (userId == null) {
            return Flux.just("您还未登录，请登录");
        }

        String message = params.get("message");
        String memoryId = params.get("memoryId");
        if (StrUtil.isBlank(message)) {
            return Flux.just("请输入内容");
        }

        ApUserEntity user = apUserService.getById(userId);
        if (user != null) {
            if (!user.getAichatPermission()) {
                return Flux.just("违规次数过多，已禁用AI功能");
            }
        }

        try {
            Map textScan = greenTextScan.greeTextScan(message);
            log.info("textScan：{}", textScan);
            if (textScan.get("suggestion").equals("pass")) {
                return consultantService.chat(message, memoryId);
            } else {
                // 如果违规5次，禁用AI功能
                Integer violationAichatCount = user.getViolationAichatCount();
                if (violationAichatCount >= 5) {
                    user.setAichatPermission(false);
                } else {
                    user.setViolationAichatCount(violationAichatCount + 1);
                }
                apUserService.updateById(user);
                return Flux.just("您输入的内容含有违规内容，根据相关法律法规不予显示");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Flux.just("系统异常");
        }
    }

}