package com.heima.vod.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.vod.entity.VideoInfoEntity;
import com.heima.vod.service.AliyunVodService;
import com.heima.vod.service.VideoInfoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 视频状态同步任务
 * 定期检查上传中的视频状态，防止因回调丢失导致状态不一致
 */
@Slf4j
@Component
public class VideoStatusSyncTask {

    @Resource
    private VideoInfoService videoInfoService;

    @Resource
    private AliyunVodService aliyunVodService;

    /**
     * 每30分钟检查一次上传中的视频状态
     * 对于上传超过24小时仍然处于Uploading或Processing状态的视频，主动查询阿里云获取最新状态
     */
    @Scheduled(fixedDelay = 30 * 60 * 1000)
    public void syncUploadingVideoStatus() {
        try {
            log.info("开始执行视频状态同步任务");

            // 查询状态为Uploading或Processing的视频
            QueryWrapper<VideoInfoEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("status", "Uploading", "Processing");
            
            // 只查询创建时间超过1小时的视频，避免刚上传的视频被误处理
            queryWrapper.lt("create_time", System.currentTimeMillis() - 60 * 60 * 1000L);
            
            List<VideoInfoEntity> videoList = videoInfoService.list(queryWrapper);
            
            if (videoList.isEmpty()) {
                log.info("没有需要同步状态的视频");
                return;
            }
            
            log.info("需要同步状态的视频数量: {}", videoList.size());
            
            // 遍历检查每个视频的状态
            for (VideoInfoEntity videoInfo : videoList) {
                try {
                    // 调用阿里云接口获取视频信息
                    com.aliyuncs.vod.model.v20170321.GetVideoInfoResponse response = 
                        aliyunVodService.getVideoInfo(videoInfo.getVideoId());
                    
                    // 获取视频状态
                    String vodStatus = response.getVideo().getStatus();
                    log.info("视频 {} 在阿里云的状态: {}", videoInfo.getVideoId(), vodStatus);
                    
                    // 根据阿里云状态更新本地状态
                    String localStatus = mapVodStatusToLocalStatus(vodStatus);
                    if (!localStatus.equals(videoInfo.getStatus())) {
                        videoInfo.setStatus(localStatus);
                        videoInfoService.updateByVideoId(videoInfo);
                        log.info("更新视频 {} 状态: {} -> {}", 
                            videoInfo.getVideoId(), videoInfo.getStatus(), localStatus);
                    }
                } catch (Exception e) {
                    log.error("查询视频 {} 状态异常", videoInfo.getVideoId(), e);
                }
            }
            
            log.info("视频状态同步任务执行完成");
        } catch (Exception e) {
            log.error("执行视频状态同步任务异常", e);
        }
    }
    
    /**
     * 将阿里云VOD状态映射为本地状态
     * @param vodStatus 阿里云VOD状态
     * @return 本地状态
     */
    private String mapVodStatusToLocalStatus(String vodStatus) {
        switch (vodStatus) {
            case "Uploading":
                return "Uploading";
            case "Processing":
                return "Processing";
            case "Normal":
                return "Normal";
            case "Blocked":
                return "Blocked";
            default:
                return "Failed";
        }
    }
}