package com.heima.business.mq;

import cn.hutool.json.JSONUtil;
import com.heima.business.auth.entity.ApUserEntity;
import com.heima.business.auth.service.user.ApUserService;
import com.heima.business.common.aliyun.GreenTextScan;
import com.heima.business.constants.MqContants;
import com.heima.business.traffic.dto.ApVideoCommentDTO;
import com.heima.business.traffic.entity.ApVideoCommentEntity;
import com.heima.business.traffic.service.ApVideoCommentService;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DriverListener {

    @Resource
    private GreenTextScan greenTextScan;

    @Resource
    private ApVideoCommentService apVideoCommentService;

    @Resource
    private ApUserService apUserService;

    /**
     * 监听增加或修改消息
     */
    @RabbitListener(queues = MqContants.DRIVER_INSERT_QUEUE)
    public void listenerHotelInsertOrUpdate(String message) {
        ApVideoCommentDTO dto = JSONUtil.toBean(message, ApVideoCommentDTO.class);

        String commentContent = dto.getCommentContent();

        try {
            // 1.文本审核
            Map textScan = greenTextScan.greeTextScan(commentContent);
            System.out.println(textScan);

            ApVideoCommentEntity videoComment = new ApVideoCommentEntity();
            videoComment.setId(dto.getId());
            videoComment.setAuditRemark("阿里云内容文本自动审核");
            if (textScan.get("suggestion").equals("pass")) {
                videoComment.setAuditStatus(1);
                apVideoCommentService.updateById(videoComment);
            } else {
                // 1.如果文本审核不通过修改评论表状态
                videoComment.setAuditStatus(2);
                apVideoCommentService.updateById(videoComment);
                // 2.如果文本审核不通过，记录违规次数
                ApUserEntity user = apUserService.getById(dto.getUserId());
                user.setViolationCommentCount(user.getViolationCommentCount() + 1);
                // 3.保存
                apUserService.updateById(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 4.如果违规超过5次，则禁用评论功能
        Long userId = dto.getUserId();

        ApUserEntity user = apUserService.getById(userId);
        Integer violationCommentCount = user.getViolationCommentCount();
        if (violationCommentCount >= 5) {
            user.setCommentPermission(false);
            apUserService.updateById(user);
        }
    }

    /**
     * 监听删除消息
     */
    @RabbitListener(queues = MqContants.DRIVER_DELETE_QUEUE)
    public void listenerHotelDelete(String message) {

    }

}
